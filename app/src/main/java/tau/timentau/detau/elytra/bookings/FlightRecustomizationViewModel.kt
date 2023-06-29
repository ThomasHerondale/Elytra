package tau.timentau.detau.elytra.bookings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.model.CARGO_LUGGAGE_PRICE
import tau.timentau.detau.elytra.model.HAND_LUGGAGE_PRICE
import tau.timentau.detau.elytra.model.PassengerData
import tau.timentau.detau.elytra.model.Ticket

class FlightRecustomizationViewModel : ViewModel() {

    private val _passengerData = MutableLiveData<List<PassengerData>>()
    val passengerData: LiveData<List<PassengerData>> = _passengerData

    private var _ticket: Ticket? = null
    val ticket: Ticket
        get() = _ticket ?: throw IllegalStateException("Ticket not set")

    var addonPrice = 0.0
        private set

    fun initializePassengerData(passengerData: List<PassengerData>) {
        _passengerData.value = passengerData
    }

    fun setTicket(ticket: Ticket) {
        _ticket = ticket
    }

    fun getPassengerData(passengerIdx: Int): PassengerData {
        val data = _passengerData.value?.find {
            it.index == passengerIdx + 1
        } ?: throw IllegalArgumentException("Unknown passenger")

        return data
    }

    fun getPassengerName(passengerIdx: Int): String {
        val data = _passengerData.value?.find {
            it.index == passengerIdx + 1
        } ?: throw IllegalArgumentException("Unknown passenger")

        return data.name
    }

    fun getCustomizedPrice(passengerIdx: Int): Double {
        val data = _passengerData.value?.find {
            it.index == passengerIdx + 1
        } ?: throw IllegalArgumentException("Unknown passenger")

        return data.price
    }

    fun addOrRemoveHandLuggage(passengerIdx: Int, selected: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }

        val data = newData.find { it.index == passengerIdx + 1 }
            ?: throw IllegalArgumentException("Unknown passenger")

        data.handLuggage = selected

        _passengerData.value = newData

        if (selected)
            addonPrice += HAND_LUGGAGE_PRICE
        else
            addonPrice -= HAND_LUGGAGE_PRICE
    }

    fun addOrRemoveCargoLuggage(passengerIdx: Int, selected: Boolean) {
        val newData = mutableListOf<PassengerData>().also { it.addAll(_passengerData.value!!) }

        val data = newData.find { it.index == passengerIdx + 1 }
            ?: throw IllegalArgumentException("Unknown passenger")

        data.cargoLuggage = selected

        _passengerData.value = newData

        if (selected)
            addonPrice += CARGO_LUGGAGE_PRICE
        else
            addonPrice -= CARGO_LUGGAGE_PRICE
    }
}