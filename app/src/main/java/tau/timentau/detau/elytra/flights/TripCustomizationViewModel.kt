package tau.timentau.detau.elytra.flights

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.model.PassengerData

class TripCustomizationViewModel : ViewModel() {

    private val _passengerData = MutableLiveData<List<PassengerData>>()
    val passengerData: LiveData<List<PassengerData>> = _passengerData

    fun initializePassengerData(passengersCount: Int, flight: Flight) {
        if (passengerData.value != null)
            throw IllegalArgumentException("Passenger data already initialized.")

        val passengerData = mutableListOf<PassengerData>()

        for (i in 0 until passengersCount) {
            passengerData.add(
                PassengerData(
                    i,
                    flight,
                    forReturn = false
                )
            )
        }

        _passengerData.value = passengerData
    }

    fun initializePassengerDataForRoundTrip(
        passengersCount: Int,
        goingFlight: Flight,
        returnFlight: Flight,
    ) {
        val passengerData = mutableListOf<PassengerData>()

        for (i in 0 until passengersCount) {
            passengerData.add(
                PassengerData(
                    i,
                    goingFlight,
                    forReturn = false
                )
            )
            passengerData.add(
                PassengerData(
                    i,
                    returnFlight,
                    forReturn = true
                )
            )
        }

        _passengerData.value = passengerData
    }

    fun addOrRemoveHandLuggage(passengerIdx: Int, selected: Boolean, forReturn: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }

        val data = newData.find { it.name == name(passengerIdx) && it.forReturn == forReturn}
            ?: throw IllegalArgumentException("Unknown passenger")

        data.handLuggage = selected

        _passengerData.value = newData
    }

    fun addOrRemoveCargoLuggage(passengerIdx: Int, selected: Boolean, forReturn: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }

        val data = newData.find { it.name == name(passengerIdx) && it.forReturn == forReturn}
            ?: throw IllegalArgumentException("Unknown passenger")

        data.cargoLuggage = selected

        _passengerData.value = newData
    }

    fun getCustomizedPrice(passengerIdx: Int, forReturn: Boolean): Double {
        val data = _passengerData.value?.find {
            it.name == name(passengerIdx) && it.forReturn == forReturn
        } ?: throw IllegalArgumentException("Unknown passenger")

        return data.price
    }

    private fun name(passengerIdx: Int) = "Passegero $passengerIdx"
}