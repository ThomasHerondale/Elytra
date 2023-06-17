package tau.timentau.detau.elytra

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.Airport

class FlightsViewModel : ViewModel() {

    private val _airportsFetchStatus = MutableStatus<List<Airport>>()
    val airportsFetchStatus: ObservableStatus<List<Airport>> = _airportsFetchStatus

    fun loadAirports() {
        performStateful(_airportsFetchStatus) {
            Repository.getAirports().await()
        }
    }
}