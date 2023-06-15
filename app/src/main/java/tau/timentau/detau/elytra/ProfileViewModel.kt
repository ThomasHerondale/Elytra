package tau.timentau.detau.elytra

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tau.timentau.detau.elytra.model.User

class ProfileViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    private val user: LiveData<User> = _user
}