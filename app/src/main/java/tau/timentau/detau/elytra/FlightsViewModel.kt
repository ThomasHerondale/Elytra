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

    private val _goingFlightsFetchStatus = MutableStatus<List<Flight>>()
    val goingFlightsFetchStatus: ObservableStatus<List<Flight>> = _goingFlightsFetchStatus

    private val _returnFlightsFetchStatus = MutableStatus<List<Flight>>()
    val returnFlightsFetchStatus: ObservableStatus<List<Flight>> = _returnFlightsFetchStatus

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
        goingDate: String,
        returnDate: String?,
        minPrice: Double,
        maxPrice: Double,
        passengersCount: Int,
        economy: Boolean = false,
        business: Boolean = false,
        firstClass: Boolean = false,
        selectedCompanies: List<String>,
        roundTrip: Boolean
    ) {
        val departureAptCode = departureApt.split(" ")[0]
        val arrivalAptCode = arrivalApt.split(" ")[0]
        performStateful(_goingFlightsFetchStatus) {
            Repository.getFlights(
                departureAptCode,
                arrivalAptCode,
                goingDate.parseToDate(),
                minPrice to maxPrice,
                passengersCount,
                economy,
                business,
                firstClass,
                selectedCompanies
            ).await()
        }

        // cerca anche i voli di ritorno se l'utente lo desidera
        if (roundTrip) {
            performStateful(_returnFlightsFetchStatus) {
                Repository.getFlights(
                    arrivalAptCode,
                    departureAptCode, // scambia le destinazioni
                    // se è selezionata andata e ritorno, deve esserci una data
                    returnDate!!.parseToDate(),
                    minPrice to maxPrice,
                    passengersCount,
                    economy,
                    business,
                    firstClass,
                    selectedCompanies
                ).await()
            }
        }
    }
}