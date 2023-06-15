package tau.timentau.detau.elytra

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Deferred
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityProfileBinding
import tau.timentau.detau.elytra.firstAccess.SelectAvatarDialog

class ProfileActivity : AppCompatActivity(),
    SelectAvatarDialog.SelectAvatarHandler by MainActivity() {

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

        binding.editProfileImgBttn.setOnClickListener {
            SelectAvatarDialog(false).show(supportFragmentManager, "selectAvatar")
        }

        setContentView(binding.root)
    }

    private fun setupUserUiInfo() {
        profileViewModel.user.observe(this) {
            Glide
                .with(this)
                .load(it.avatar)
                .into(binding.profileImg)

            binding.fullnameLabel.text = it.fullName
            binding.sexLabel.text = it.sex.stringVal
            binding.birthDateLabel.text = it.birthDate.toReadable()

            binding.profileEmailLabel.text = it.email
            binding.profilePwdLabel.text = hiddenPasswordString(it.passwordLength)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override suspend fun fetchAvatars(): Deferred<List<Bitmap>> {
        return Repository.getAvatars()
    }

    override suspend fun avatarSelected(id: Int) {
        Repository.setAvatar(loggedEmail, id)
        toAvatarSetConfirm()
    }

    override fun toAvatarSetConfirm() {
        profileViewModel.reloadUserData(loggedEmail)

        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.avatar_impostato))
            .setMessage(getString(R.string.immagine_impostata))
            .setIcon(R.drawable.ic_check_circle_24)
            .setPositiveButton(R.string.okay) { _, _ -> }
            .show()
    }
}