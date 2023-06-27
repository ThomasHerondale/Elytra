package tau.timentau.detau.elytra.flights

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.model.PassengerData

class TripCustomizationViewModel : ViewModel() {

    private val _passengerData = MutableLiveData<List<PassengerData>>()
    val passengerData: LiveData<List<PassengerData>> = _passengerData

    fun updateFlightData(flight: Flight, forReturn: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(passengerData.value!!) }

        _passengerData.value = newData.map {
            // rimpiazza i dati relativi al volo di andata o di ritorno
            if (it.forReturn == forReturn)
                PassengerData(it.index, flight, forReturn)
            else
                it
        }
    }

    fun isPassengerDataInitialized(): Boolean {
        return _passengerData.value != null
    }

    fun initializePassengerData(passengersCount: Int, flight: Flight) {
        if (passengerData.value != null)
            throw IllegalArgumentException("Passenger data already initialized.")

        val passengerData = mutableListOf<PassengerData>()

        for (i in 0 until passengersCount) {
            passengerData.add(
                PassengerData(
                    i + 1,
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
                    i + 1,
                    goingFlight,
                    forReturn = false
                )
            )
            passengerData.add(
                PassengerData(
                    i + 1,
                    returnFlight,
                    forReturn = true
                )
            )
        }

        _passengerData.value = passengerData
    }

    fun getPassengerName(passengerIdx: Int): String {
        val data = _passengerData.value?.find { it.index == passengerIdx + 1}
            ?: throw IllegalArgumentException("Unknown passenger")

        return data.name
    }

    fun setPassengerName(passengerIdx: Int, newName: String) {
        viewModelScope.launch {
            // se esiste già un passegero con questo nome, esci
            _passengerData.value?.forEach {
                if (it.name == newName)
                    return@launch
            }

            val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }
            // trova l'attuale nome del passegero
            val currentName = getPassengerName(passengerIdx)

            for (passenger in newData) {
                if (passenger.name == currentName)
                    passenger.name = newName
            }

            _passengerData.postValue(newData)
            println(_passengerData.value)
        }
    }

    fun addOrRemoveHandLuggage(passengerIdx: Int, selected: Boolean, forReturn: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }

        val data = newData.find { it.index == passengerIdx + 1 && it.forReturn == forReturn}
            ?: throw IllegalArgumentException("Unknown passenger")

        data.handLuggage = selected

        _passengerData.value = newData
    }

    fun addOrRemoveCargoLuggage(passengerIdx: Int, selected: Boolean, forReturn: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }

        val data = newData.find { it.index == passengerIdx + 1 && it.forReturn == forReturn}
            ?: throw IllegalArgumentException("Unknown passenger")

        data.cargoLuggage = selected

        _passengerData.value = newData
    }

    fun getCustomizedPrice(passengerIdx: Int, forReturn: Boolean): Double {
        val data = _passengerData.value?.find {
            it.index == passengerIdx + 1 && it.forReturn == forReturn
        } ?: throw IllegalArgumentException("Unknown passenger")

        return data.price
    }

    fun getPassengerData(passengerIdx: Int, forReturn: Boolean): PassengerData {
        val data = _passengerData.value?.find {
            it.index == passengerIdx + 1 && it.forReturn == forReturn
        } ?: throw IllegalArgumentException("Unknown passenger")

        return data
    }

    fun getFlightTotalPrice(forReturn: Boolean): Double {
        return _passengerData.value?.sumOf {
            if (it.forReturn == forReturn)
                it.price
            else
                0.0
        } ?: throw IllegalStateException("Passenger data not initialized")
    }

    fun getTripTotalPrice() =
        getFlightTotalPrice(false) + getFlightTotalPrice(true)
}