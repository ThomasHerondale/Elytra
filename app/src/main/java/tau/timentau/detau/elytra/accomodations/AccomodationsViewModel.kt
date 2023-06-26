package tau.timentau.detau.elytra.accomodations

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.model.Accomodation
import tau.timentau.detau.elytra.model.AccomodationCategory
import tau.timentau.detau.elytra.model.City
import tau.timentau.detau.elytra.performStateful

class AccomodationsViewModel : ViewModel() {

    private val _citiesFetchStatus = MutableStatus<List<City>>()
    val citiesFetchStatus: ObservableStatus<List<City>> = _citiesFetchStatus

    private val _accomodationsFetchStatus = MutableStatus<List<Accomodation>>()
    val accomodationFetchStatus: ObservableStatus<List<Accomodation>> = _accomodationsFetchStatus

    inline val cities: List<City>
        get() = (citiesFetchStatus.value as Status.Success<List<City>>?)?.data
            ?: throw IllegalStateException("Cities not retrieved correctly")

    fun loadCities() {
        performStateful(_citiesFetchStatus) {
            Repository.getCities().await()
        }
    }

    fun getAccomodations(
        city: String,
        minPrice: Double,
        maxPrice: Double,
        rating: Int,
        selectedCategories: List<AccomodationCategory>
    ) {
        performStateful(_accomodationsFetchStatus) {
            Repository.getAccomodations(
                city,
                minPrice,
                maxPrice,
                rating,
                selectedCategories.map { it.name }
            ).await()
        }
    }
}