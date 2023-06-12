package tau.timentau.detau.elytra

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tau.timentau.detau.elytra.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity() {
    private lateinit var binding : ActivityStartBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}