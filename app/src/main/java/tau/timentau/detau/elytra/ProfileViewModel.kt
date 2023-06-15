package tau.timentau.detau.elytra

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.model.User

class ProfileViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun retrieveUserData(email: String) {
        coroutineScope.launch {
            val user = Repository.fetchUserData(email).await()
            _user.postValue(user)
        }
    }
}