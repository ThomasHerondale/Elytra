package tau.timentau.detau.elytra

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import tau.timentau.detau.elytra.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), EntryActivity {

    private lateinit var binding : ActivityMainBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun login(email: String) {
        TODO("Not yet implemented")
    }
}