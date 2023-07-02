package tau.timentau.detau.elytra.discover

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.Session
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.model.City
import tau.timentau.detau.elytra.performStateful

class DiscoverViewModel : ViewModel() {

    private val _mostFamousDestinationsFetchStatus = MutableStatus<List<Pair<City, Bitmap>>>()
    val mostFamousDestinationsFetchStatus: ObservableStatus<List<Pair<City, Bitmap>>> =
        _mostFamousDestinationsFetchStatus

    private val _futureDestinationsFetchStatus = MutableStatus<List<Pair<City, Bitmap>>>()
    val futureDestinationsFetchStatus: ObservableStatus<List<Pair<City, Bitmap>>> =
        _futureDestinationsFetchStatus

    inline val mostFamousDestinations: List<Pair<City, Bitmap>>
        get() =
            (mostFamousDestinationsFetchStatus.value as Status.Success<List<Pair<City, Bitmap>>>)
                .data

    inline val futureDestinations: List<Pair<City, Bitmap>>
        get() =
            (futureDestinationsFetchStatus.value as Status.Success<List<Pair<City, Bitmap>>>)
                .data

    fun getMostFamousDestinations() {
        performStateful(_mostFamousDestinationsFetchStatus) {
            Repository.getMostFamousDestinations().await()
        }
    }

    fun getFutureDestinations() {
        performStateful(_futureDestinationsFetchStatus) {
            Repository.getFutureDestinations(Session.loggedEmail).await()
        }
    }
}