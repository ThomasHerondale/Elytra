package tau.timentau.detau.elytra

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.model.Airport

class FlightsViewModel : ViewModel() {
    private val airportsFetchStatus = MutableLiveData<Status<List<Airport>>>()
}