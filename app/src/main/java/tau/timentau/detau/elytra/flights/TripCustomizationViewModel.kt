package tau.timentau.detau.elytra.flights

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.model.PassengerData

class TripCustomizationViewModel : ViewModel() {
    private val passengerData = mutableListOf<PassengerData>()

    fun initializePassengerData(passengersCount: Int) {
        if (passengerData.isNotEmpty())
            throw IllegalArgumentException("Passenger data already initialized.")

        for (i in 0 until passengersCount) {
            passengerData.add(
                PassengerData("Passegero ${i + 1}")
            )
        }

        println(passengerData)
    }
}