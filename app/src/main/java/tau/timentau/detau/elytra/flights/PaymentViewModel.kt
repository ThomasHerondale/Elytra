package tau.timentau.detau.elytra.flights

import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.Session
import tau.timentau.detau.elytra.database.MutableStatus
import tau.timentau.detau.elytra.database.ObservableStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.PaymentMethod
import tau.timentau.detau.elytra.performStateful

class PaymentViewModel : ViewModel() {

    private val _paymentMethodsFetchStatus = MutableStatus<List<PaymentMethod>>()
    val paymentMethodsFetchStatus: ObservableStatus<List<PaymentMethod>> =
        _paymentMethodsFetchStatus

    fun fetchPaymentMethods() {
        performStateful(_paymentMethodsFetchStatus) {
            Repository.getPaymentMethods(Session.loggedEmail).await()
        }
    }

    fun reloadPaymentMethods() {
        fetchPaymentMethods()
    }
}