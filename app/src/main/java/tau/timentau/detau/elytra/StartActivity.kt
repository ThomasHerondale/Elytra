package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import tau.timentau.detau.elytra.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity(), NavHostActivity {

    private lateinit var binding : ActivityStartBinding
    private val navigator by lazy { getNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun navigateTo(directions: NavDirections) {
        navigator.navigate(directions)
    }
}