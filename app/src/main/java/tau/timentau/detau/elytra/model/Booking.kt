package tau.timentau.detau.elytra.model

data class Booking(
    val id: Int,
    val accomodation: Accomodation,
    val checkInDate: String,
    val checkOutDate: String,
    val hostCount: Int,
    val nightCount: Int,
    val price: Double,
)
