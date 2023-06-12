package tau.timentau.detau.elytra.model

import java.util.Date

data class User(
    val email: String,
    val fullName: String,
    val birthDate: Date,
    val sex: Sex,
    // todo avatar
)

enum class Sex(name: String) {
    MALE("Uomo"),
    FEMALE("Donna"),
    OTHER("Altro")
}
