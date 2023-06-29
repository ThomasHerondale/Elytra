package tau.timentau.detau.elytra.model

data class Ticket(
    val id: Int,
    val flight: Flight,
    val passengersCount: Int,
    val passengersInfo: List<PassengerData>,
    val price: Double,
    val makingDate: String,
)
