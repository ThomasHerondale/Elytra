package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import tau.timentau.detau.elytra.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity(), EntryActivity {

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