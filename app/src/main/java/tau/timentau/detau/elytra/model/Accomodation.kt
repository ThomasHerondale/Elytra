package tau.timentau.detau.elytra.model

import android.graphics.Bitmap

data class Accomodation(
    val id: String,
    val name: String,
    val description: String,
    val category: AccomodationCategory,
    val city: String,
    val address: String,
    val image: Bitmap,
    val price: Double,
    val rating: Double
)