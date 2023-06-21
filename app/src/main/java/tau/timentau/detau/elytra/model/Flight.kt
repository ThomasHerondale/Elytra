package tau.timentau.detau.elytra.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Flight(
    val id: String,
    val company: Company,
    val departureApt: Airport,
    val arrivalApt: Airport,
    val date: String,
    val gateClosingTime: String, // todo localtime
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val price: Double,
    val serviceClass: ServiceClass
) : Parcelable