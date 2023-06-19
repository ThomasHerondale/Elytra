package tau.timentau.detau.elytra.model

import android.graphics.Bitmap

data class Company(
    val name: String,
    val logo: Bitmap
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Company

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}