package tau.timentau.detau.elytra.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.PaymentCircuit

class AddPaymentMethodViewModel : ViewModel() {
    private val _circuits = MutableLiveData<List<PaymentCircuit>>()
    private val _currentCircuit = MutableLiveData<PaymentCircuit?>()
    val currentCircuit: LiveData<PaymentCircuit?> = _currentCircuit

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        coroutineScope.launch {
            val circuits = Repository.getPaymentCircuits().await()
            _circuits.postValue(circuits)
        }
    }

    fun updateCircuit(startDigit: Char?) {
        if (startDigit == null)
            _currentCircuit.value = null

        _currentCircuit.value = _circuits.value!!.find { it.startDigit == startDigit }
    }

}