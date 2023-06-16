package tau.timentau.detau.elytra.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.PaymentMethod
import tau.timentau.detau.elytra.model.User

class ProfileViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _paymentMethods = MutableLiveData<List<PaymentMethod>>()
    val paymentMethods: LiveData<List<PaymentMethod>> = _paymentMethods

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun retrieveUserData(email: String) {
        coroutineScope.launch {
            val user = Repository.fetchUserData(email).await()
            _user.postValue(user)
        }
    }

    fun reloadUserData(email: String) {
        if (_user.value == null)
            throw IllegalStateException("User cannot be reloaded as it's not been loaded once")

        retrieveUserData(email)
    }

    fun retrievePaymentMethods(email: String) {
        coroutineScope.launch {
            val paymentMethods = Repository.getPaymentMethods(email).await()
            _paymentMethods.postValue(paymentMethods)
        }
    }

    fun reloadPaymentMethods(email: String) {
        if (_paymentMethods.value == null)
            throw IllegalStateException(
                "Payment methods cannot be reloaded as they've not been loaded once"
            )

        retrievePaymentMethods(email)
    }
}