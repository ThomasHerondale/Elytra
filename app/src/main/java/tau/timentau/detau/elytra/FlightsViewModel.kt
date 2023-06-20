package tau.timentau.detau.elytra

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.Airport
import tau.timentau.detau.elytra.model.Company
import tau.timentau.detau.elytra.model.Flight

class FlightsViewModel : ViewModel() {

    private val _airportsFetchStatus = MutableStatus<List<Airport>>()
    val airportsFetchStatus: ObservableStatus<List<Airport>> = _airportsFetchStatus

    private val _companyFetchStatus = MutableStatus<List<Company>>()
    val companyFetchStatus: ObservableStatus<List<Company>> = _companyFetchStatus

    private val _flightsFetchStatus = MutableStatus<List<Flight>>()
    val flightsFetchStatus: ObservableStatus<List<Flight>> = _flightsFetchStatus

    fun loadAirports() {
        performStateful(_airportsFetchStatus) {
            Repository.getAirports().await()
        }
    }

    fun loadCompanies() {
        performStateful(_companyFetchStatus) {
            Repository.getCompanies().await()
        }
    }

    fun searchFlights(
        departureApt: String,
        arrivalApt: String,
        date: String,
        minPrice: Double,
        maxPrice: Double,
        passengersCount: Int,
        economy: Boolean = false,
        business: Boolean = false,
        firstClass: Boolean = false
    ) {
        val departureAptCode = departureApt.split(" ")[0]
        val arrivalAptCode = arrivalApt.split(" ")[0]
        performStateful(_flightsFetchStatus) {
            Repository.getFlights(
                departureAptCode,
                arrivalAptCode,
                date.parseToDate(),
                minPrice to maxPrice,
                passengersCount,
                economy,
                business,
                firstClass
            ).await()
        }
    }
}