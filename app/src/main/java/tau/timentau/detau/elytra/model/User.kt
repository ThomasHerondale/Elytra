package tau.timentau.detau.elytra.model

import android.graphics.Bitmap
import java.util.Date

data class User(
    val email: String,
    val fullName: String,
    val birthDate: Date,
    val sex: Sex,
    val passwordLength: Int,
    val avatar: Bitmap
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return email == other.email
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }
}

enum class Sex(val stringVal: String) {
    MALE("Uomo"),
    FEMALE("Donna"),
    OTHER("Altro");
}
