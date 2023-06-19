package tau.timentau.detau.elytra.model

import kotlinx.datetime.LocalDate

data class Flight(
    val id: String,
    val company: Company,
    val departureApt: Airport,
    val arrivalApt: Airport,
    val date: LocalDate,
    val gateClosingTime: String, // todo localtime
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val price: Double
)