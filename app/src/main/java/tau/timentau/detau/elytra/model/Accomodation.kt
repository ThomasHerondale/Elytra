package tau.timentau.detau.elytra.model

data class Accomodation(
    val id: String,
    val name: String,
    val description: String,
    val category: AccomodationCategory,
    val city: String,
    val address: String,
    val price: Double,
    val rating: Double
)