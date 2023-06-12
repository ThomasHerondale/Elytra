package tau.timentau.detau.elytra.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.LocalDate
import tau.timentau.detau.elytra.model.Sex
import tau.timentau.detau.elytra.model.User

object Repository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun userExists(email: String, password: String): Deferred<Boolean> {
        return coroutineScope.async {
            DatabaseDAO.selectValue<Boolean>("""
                SELECT EXISTS(
                    SELECT *
                    FROM users
                    WHERE users.email = $email AND users.password = $password
                ) as boolean
            """) ?: false
        }
    }

    private class UserDTO(
        val email: String,
        val fullName: String,
        val birthDate: LocalDate,
        val sex: Sex,
        // todo avatar
        val password: String,
        val question: String,
        val answer: String
    ) {
            fun toUser() = User(email, fullName, birthDate, sex)
    }
}