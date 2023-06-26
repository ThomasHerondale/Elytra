package tau.timentau.detau.elytra.database

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.LocalDate
import tau.timentau.detau.elytra.model.Accomodation
import tau.timentau.detau.elytra.model.AccomodationCategory
import tau.timentau.detau.elytra.model.City
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
            DatabaseDAO.selectValue<Boolean>("""
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

            userDTO.toUser()
        }
    }

    suspend fun isMailUsed(email: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
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
            DatabaseDAO.selectValue<String>("""
                SELECT security_questions.question as string
                FROM users JOIN security_questions on users.question = security_questions.id
                WHERE users.email = '$email' 
            """) ?: throw IllegalStateException("Could not retrieve data")
        }
    }

    suspend fun isAnswerCorrect(email: String, answer: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
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
            DatabaseDAO.selectValue<Boolean>("""
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

            val circuits = getPaymentCircuits().await()


            paymentMethodDTOs.map {
                // ottieni il circuito completo della carta
                val circuit = circuits.find { circuit -> circuit.name == it.circuit } ?:
                throw IllegalArgumentException("Unknown circuit")

                it.toPaymentMethod(circuit)
            }
        }
    }

    suspend fun getPaymentCircuits(): Deferred<List<PaymentCircuit>> {
        return coroutineScope.async {
            // ottieni i dati dei circuiti
            val circuitsDTOs = DatabaseDAO.selectList<PaymentCircuitDTO>(
                """
                    SELECT *
                    FROM payment_circuits
                """
            )

            val circuits = circuitsDTOs.map {
                val logo = DatabaseDAO.getImage(it.logo)
                it.toPaymentCircuit(logo)
            }

            circuits
        }
    }

    suspend fun getCities(): Deferred<List<City>> {
        return coroutineScope.async {
            DatabaseDAO.selectList<City>("""
                SELECT *
                FROM cities
            """)
        }
    }

    suspend fun getAccomodations(
        city: String,
        minPrice: Double,
        maxPrice: Double,
        rating: Int,
        selectedCategories: List<String>
    ): Deferred<List<Accomodation>> {

        val categoriesSectionBuilder = StringBuilder()
        selectedCategories.forEach {categoriesSectionBuilder.append("'$it', ")}

        val categoriesStr =
            if (categoriesSectionBuilder.isEmpty())
                ""
            else
                "AND a.category IN (${categoriesSectionBuilder.removeSuffix(", ")})"

        return coroutineScope.async {
            val accomodationDTOs = DatabaseDAO.selectList<AccomodationDTO>("""
                SELECT a.id, a.name, a.description, a.category, c.name as city, 
                    a.address, a.image, a.price, a.rating
                FROM accomodations a JOIN cities c on a.city = c.id
                WHERE c.name = '$city' AND
                    a.price BETWEEN $minPrice AND $maxPrice AND
                    a.rating >= $rating $categoriesStr
            """)

            val accomodations = mutableListOf<Accomodation>()
            accomodationDTOs.forEach {
                val image = DatabaseDAO.getImage(it.image)
                accomodations.add(it.toAccomodation(image))
            }

            accomodations
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
        fun toUser() = User(email, fullName, birthDate, sex)
    }

    private class QuestionDTO(
        val question: String
    )

    private class AvatarDTO(
        val id: Int,
        val path: String
    )

    private data class AccomodationDTO(
        val id: String,
        val name: String,
        val description: String,
        val category: AccomodationCategory,
        val city: String,
        val address: String,
        val image: String,
        val price: Double,
        val rating: Double
    ) {
        fun toAccomodation(image: Bitmap) =
            Accomodation(id, name, description, category, city, address, image, price, rating)
    }

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