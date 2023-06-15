package tau.timentau.detau.elytra.database

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.LocalDate
import tau.timentau.detau.elytra.model.PaymentCircuit
import tau.timentau.detau.elytra.model.PaymentMethod
import tau.timentau.detau.elytra.model.Sex
import tau.timentau.detau.elytra.model.User
import tau.timentau.detau.elytra.toDateString
import java.util.Date

object Repository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun userExists(email: String, password: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email = '$email' AND users.password = '$password'
                ) as boolean
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun fetchUserData(email: String): Deferred<User> {
        return coroutineScope.async {
            val userDTO = DatabaseDAO.selectValue<UserDTO>("""
                SELECT email, fullName, birthDate, sex, avatar_images.path as avatar, 
                    password, security_questions.question as question, answer
                FROM users JOIN avatar_images on users.avatar = avatar_images.id
                    JOIN security_questions on users.question = security_questions.id
                WHERE email = '$email';
            """) ?: throw IllegalStateException("Could not retrieve data")

            val userAvatar = DatabaseDAO.getImage(userDTO.avatar)

            userDTO.toUser(userAvatar)
        }
    }

    suspend fun isMailUsed(email: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email= '$email'
                ) as boolean
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun createUser(
        email: String,
        fullName: String,
        birthDate: LocalDate,
        sex: Sex,
        password: String
    ) {
        DatabaseDAO.insert("""
            INSERT
            INTO users(email, fullName, birthDate, sex, password)
            VALUE ('$email', '$fullName', '${birthDate.toDateString()}', '$sex', '$password')
        """)
    }

    suspend fun getSecurityQuestion(email: String): Deferred<String> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<String>("""
                SELECT security_questions.question as string
                FROM users JOIN security_questions on users.question = security_questions.id
                WHERE users.email = '$email' 
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun isAnswerCorrect(email: String, answer: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email = '$email' and answer = '$answer'
                ) as boolean
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun resetPassword(email: String, newPassword: String) {
        DatabaseDAO.update("""
            UPDATE users
            SET password = '$newPassword'
            WHERE email = '$email' 
        """)
    }

    suspend fun getSecurityQuestions(): Deferred<List<String>> {
        return coroutineScope.async {
            DatabaseDAO.selectList<QuestionDTO>("""
                SELECT question
                FROM security_questions
            """).map { it.question }
        }
    }

    suspend fun isFirstAccess(email: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectPrimitiveValue<Boolean>("""
                SELECT (question IS NULL) as boolean
                FROM users
                WHERE email = '$email'
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun setupSecurityQuestion(email: String, question: String, answer: String) {
        DatabaseDAO.update("""
            UPDATE users u
            SET u.question = (
                SELECT sq.id
                FROM security_questions sq
                WHERE sq.question = '$question'
                ),
                u.answer = '$answer'
            WHERE u.email = '$email'
        """)
    }

    suspend fun getAvatars(): Deferred<List<Bitmap>> {
        return coroutineScope.async {
            val paths = DatabaseDAO.selectList<AvatarDTO>("""
                SELECT *
                FROM avatar_images
            """).map { it.path }

            val images = mutableListOf<Bitmap>()

            for (path in paths) {
                images.add(
                    DatabaseDAO.getImage(path)
                )
            }

            images
        }
    }

    suspend fun setAvatar(email: String, id: Int) {
        val dbId = id + 1 // gli indici generati partono da 1
        DatabaseDAO.update("""
            UPDATE users
            SET avatar = $dbId
            WHERE email = '$email'
        """)
    }

    suspend fun changeEmail(oldEmail: String, newEmail: String) {
        DatabaseDAO.update("""
            UPDATE users
            SET email = '$newEmail'
            WHERE email = '$oldEmail'
        """)
        println("updated")
    }

    suspend fun getPaymentMethods(email: String): Deferred<List<PaymentMethod>> {
        return coroutineScope.async {
            val paymentMethodDTOs = DatabaseDAO.selectList<PaymentMethodDTO>("""
                SELECT *
                FROM payment_methods
                WHERE userEmail = '$email'
            """)

            // ottieni i dati dei circuiti
            val circuitsDTOs = DatabaseDAO.selectList<PaymentCircuitDTO>("""
                SELECT *
                FROM payment_circuits
            """)

            val circuits = circuitsDTOs.map {
                val logo = DatabaseDAO.getImage(it.logo)
                it.toPaymentCircuit(logo)
            }

            paymentMethodDTOs.map {
                // ottieni il circuito completo della carta
                val circuit = circuits.find { circuit -> circuit.name == it.circuit } ?:
                throw IllegalArgumentException("Unknown circuit")

                it.toPaymentMethod(circuit)
            }
        }
    }

    private class UserDTO(
        val email: String,
        val fullName: String,
        val birthDate: Date,
        val sex: Sex,
        val avatar: String,
        val password: String,
        val question: String,
        val answer: String
    ) {
        fun toUser(avatar: Bitmap) =
            User(
                email,
                fullName,
                birthDate,
                sex,
                password.length,
                avatar
            )
    }

    private class QuestionDTO(
        val question: String
    )

    private class AvatarDTO(
        val id: Int,
        val path: String
    )

    private class PaymentMethodDTO(
        val number: String,
        val userEmail: String,
        val circuit: String,
        val expiryDate: Date,
        val safetyCode: String,
        val ownerFullName: String
    ) {
        fun toPaymentMethod(circuit: PaymentCircuit) =
            PaymentMethod(
                number,
                circuit,
                expiryDate,
                safetyCode,
                ownerFullName
            )
    }

    private class PaymentCircuitDTO(
        val name: String,
        val logo: String,
        val startDigit: String
    ) {
        fun toPaymentCircuit(logo: Bitmap) =
            PaymentCircuit(
                name,
                logo,
                startDigit[0]
            )
    }
}