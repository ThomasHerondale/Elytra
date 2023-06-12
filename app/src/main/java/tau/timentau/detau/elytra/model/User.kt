package tau.timentau.detau.elytra.model

import kotlinx.datetime.LocalDate

data class User(
    val email: String,
    val fullName: String,
    val birthDate: LocalDate,
    val sex: Sex,
    // todo avatar
)

enum class Sex(name: String) {
    MALE("Uomo"),
    FEMALE("Donna"),
    OTHER("Altro")
}
