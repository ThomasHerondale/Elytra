package tau.timentau.detau.elytra.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class City(
    val id: Int,
    val name: String,
) : Parcelable {
    override fun toString(): String {
        return name
    }
}