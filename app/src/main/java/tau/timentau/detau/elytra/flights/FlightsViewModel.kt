package tau.timentau.detau.elytra.flights

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.OperationStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.model.Airport
import tau.timentau.detau.elytra.model.Company
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.parseToDate
import tau.timentau.detau.elytra.performStateful

class FlightsViewModel : ViewModel() {

    private val _airportsFetchStatus = MutableStatus<List<Airport>>()
    val airportsFetchStatus: ObservableStatus<List<Airport>> = _airportsFetchStatus

    private val _companyFetchStatus = MutableStatus<List<Company>>()
    val companyFetchStatus: ObservableStatus<List<Company>> = _companyFetchStatus

    private val _goingFlightsFetchStatus = MutableStatus<List<Flight>>()
    val goingFlightsFetchStatus: ObservableStatus<List<Flight>> = _goingFlightsFetchStatus

    private val _returnFlightsFetchStatus = MutableStatus<List<Flight>>()
    val returnFlightsFetchStatus: ObservableStatus<List<Flight>> = _returnFlightsFetchStatus

    private val _flightsFetchStatus = MutableLiveData<OperationStatus>()
    val flightsFetchStatus: LiveData<OperationStatus> = _flightsFetchStatus

    var selectedGoingFlight: Flight? = null
        private set
    var selectedReturnFlight: Flight? = null
        private set

    inline val goingFlightsList: List<Flight>
        get() {
            if (goingFlightsFetchStatus.value !is Status.Success<List<Flight>>)
                throw IllegalStateException("Flight list not retrieved correctly")

            return (goingFlightsFetchStatus.value as Status.Success<List<Flight>>).data
        }

    inline val returnFlightsList: List<Flight>
        get() {
            if (returnFlightsFetchStatus.value !is Status.Success<List<Flight>>)
                throw IllegalStateException("Flight list not retrieved correctly")

            return (returnFlightsFetchStatus.value as Status.Success<List<Flight>>).data
        }

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

        performStateful(_flightsFetchStatus) {
            val goingFlights = Repository.getFlights(
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

            var returnFlights: List<Flight>? = null

            // cerca anche i voli di ritorno se l'utente lo desidera
            if (roundTrip) {
                returnFlights = Repository.getFlights(
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

            _goingFlightsFetchStatus.value = Status.Success(goingFlights)

            if (roundTrip)
            // don't worry, se entriamo qui returnFlights non è sicuramente nullo ;)
                _returnFlightsFetchStatus.value = Status.success(returnFlights!!)
        }
    }

    fun selectGoingFlight(flight: Flight) {
        selectedGoingFlight = flight
    }

    fun unselectGoingFlight() {
        selectedGoingFlight = null
    }

    fun selectReturnFlight(flight: Flight) {
        selectedReturnFlight = flight
    }

    fun unselectReturnFlight() {
        selectedReturnFlight = null
    }
}