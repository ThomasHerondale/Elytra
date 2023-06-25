package tau.timentau.detau.elytra.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import tau.timentau.detau.elytra.Session
import tau.timentau.detau.elytra.database.OperationStatus
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.PaymentCircuit
import tau.timentau.detau.elytra.performStateful

class AddPaymentMethodViewModel : ViewModel() {
    private val _circuits = MutableLiveData<List<PaymentCircuit>>()

    private val _currentCircuit = MutableLiveData<PaymentCircuit?>()
    val currentCircuit: LiveData<PaymentCircuit?> = _currentCircuit

    private val _paymentMethodCreationStatus = MutableLiveData<OperationStatus>()
    val paymentMethodCreationStatus: LiveData<OperationStatus> = _paymentMethodCreationStatus

    init {
        viewModelScope.launch {
            val circuits = Repository.getPaymentCircuits().await()
            _circuits.postValue(circuits)
        }
    }

    fun updateCircuit(startDigit: Char?) {
        if (startDigit == null)
            _currentCircuit.value = null

        _currentCircuit.value = _circuits.value!!.find { it.startDigit == startDigit }
    }

    fun createPaymentMethod(
        number: String,
        expiryDate: LocalDate,
        safetyCode: String,
        ownerFullname: String
    ) {
        val circuit = currentCircuit.value ?: throw IllegalStateException("Invalid circuit")

        performStateful(_paymentMethodCreationStatus) {
            Repository.createPaymentMethod(
                Session.loggedEmail,
                number,
                circuit,
                expiryDate,
                safetyCode,
                ownerFullname
            )
        }
    }

}