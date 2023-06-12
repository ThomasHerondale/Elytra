package tau.timentau.detau.elytra

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import tau.timentau.detau.elytra.databinding.ActivityMainBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding : ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun login(email: String) {
        Log.v("LOGIN", "Logging user $email")
    }
}