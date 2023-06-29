package tau.timentau.detau.elytra.bookings

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.Session
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.Ticket
import tau.timentau.detau.elytra.performStateful

class BookingsViewModel : ViewModel() {

    private val _ticketsFetchStatus = MutableStatus<List<Ticket>>()
    val ticketsFetchStatus: ObservableStatus<List<Ticket>> = _ticketsFetchStatus

    fun loadTickets() {
        performStateful(_ticketsFetchStatus) {
            Repository.getTickets(Session.loggedEmail).await()
        }
    }
}