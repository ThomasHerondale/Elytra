package tau.timentau.detau.elytra

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        // aggiorna i dati del profilo
        profileViewModel.retrieveUserData(loggedEmail)

        // abilita il pulsante indietro
        setSupportActionBar(binding.profileTopAppBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setupUserUiInfo()

        setContentView(binding.root)
    }

    private fun setupUserUiInfo() {
        profileViewModel.user.observe(this) {
            Glide
                .with(this)
                .load(it.avatar)
                .into(binding.profileImg)

            binding.fullnameLabel.text = it.fullName
            binding.birthDateLabel.text = it.birthDate.toString()
            binding.sexLabel.text = it.sex.stringVal
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}