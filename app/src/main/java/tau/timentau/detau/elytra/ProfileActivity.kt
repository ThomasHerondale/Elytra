package tau.timentau.detau.elytra

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import tau.timentau.detau.elytra.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        setSupportActionBar(binding.profileTopAppBar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setContentView(binding.root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}