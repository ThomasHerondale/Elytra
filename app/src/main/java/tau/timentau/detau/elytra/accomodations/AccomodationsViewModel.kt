package tau.timentau.detau.elytra.accomodations

import androidx.lifecycle.ViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.model.Accomodation
import tau.timentau.detau.elytra.model.AccomodationCategory
import tau.timentau.detau.elytra.model.City
import tau.timentau.detau.elytra.performStateful
import tau.timentau.detau.elytra.toLocalDate
import java.util.Date

class AccomodationsViewModel : ViewModel() {

    private val _citiesFetchStatus = MutableStatus<List<City>>()
    val citiesFetchStatus: ObservableStatus<List<City>> = _citiesFetchStatus

    private val _accomodationsFetchStatus = MutableStatus<List<Accomodation>>()
    val accomodationFetchStatus: ObservableStatus<List<Accomodation>> = _accomodationsFetchStatus

    private var _selectedStartDate: LocalDate? = null
    // vista non nullabile
    val selectedStartDate: LocalDate
        get() = _selectedStartDate ?: throw IllegalStateException("Start date not selected")

    private var _selectedEndDate: LocalDate? = null
    val selectedEndDate: LocalDate
        get() = _selectedEndDate ?: throw IllegalStateException("End date not selected")

    private var _selectedAccomodation: Accomodation? = null
    val selectedAccomodation: Accomodation
        get() = _selectedAccomodation ?: throw IllegalStateException("Accomodation not selected")

    inline val cities: List<City>
        get() = (citiesFetchStatus.value as Status.Success<List<City>>?)?.data
            ?: throw IllegalStateException("Cities not retrieved correctly")

    inline val accomodations: List<Accomodation>
        get() = (accomodationFetchStatus.value as Status.Success<List<Accomodation>>?)?.data
            ?: throw IllegalStateException("Accomodations not retrieved correctly")

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
    
    fun setPeriod(period: Pair<Date, Date>) {
        _selectedStartDate = period.first.toLocalDate()
        _selectedEndDate = period.second.toLocalDate()
    }

    fun getStayingDuration() = selectedEndDate.minus(selectedStartDate).days

    fun getTotalPrice(hostCount: Int) =
        hostCount * getStayingDuration() * selectedAccomodation.price

    fun selectAccomodation(accomodation: Accomodation) {
        _selectedAccomodation = accomodation
    }
}