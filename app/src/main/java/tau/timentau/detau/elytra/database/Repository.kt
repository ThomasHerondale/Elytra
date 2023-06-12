package tau.timentau.detau.elytra.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import tau.timentau.detau.elytra.model.Sex
import tau.timentau.detau.elytra.model.User
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
}