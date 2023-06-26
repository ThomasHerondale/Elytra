package tau.timentau.detau.elytra.accomodations

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.model.City
import tau.timentau.detau.elytra.performStateful

class AccomodationsViewModel : ViewModel() {

    private val _citiesFetchStatus = MutableStatus<List<City>>()
    val citiesFetchStatus: ObservableStatus<List<City>> = _citiesFetchStatus

    inline val cities: List<City>
        get() = (citiesFetchStatus.value as Status.Success<List<City>>?)?.data
            ?: throw IllegalStateException("Cities not retrieved correctly")

    fun loadCities() {
        performStateful(_citiesFetchStatus) {
            Repository.getCities().await()
        }
    }
}