package tau.timentau.detau.elytra.profile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Deferred
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.StartActivity
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityProfileBinding
import tau.timentau.detau.elytra.firstAccess.SelectAvatarDialog
import tau.timentau.detau.elytra.hiddenPasswordString
import tau.timentau.detau.elytra.loggedEmail
import tau.timentau.detau.elytra.toReadable

class ProfileActivity : AppCompatActivity(),
    SelectAvatarDialog.SelectAvatarHandler,
    EditEmailDialog.EditEmailHandler,
    EditPasswordDialog.EditPasswordHandler {

    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        // aggiorna i dati del profilo
        profileViewModel.retrieveUserData(loggedEmail)
        profileViewModel.retrievePaymentMethods(loggedEmail)

        // abilita il pulsante indietro
        setSupportActionBar(binding.profileTopAppBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setupUserUiInfo()
        setupPaymentMethodsUi()

        binding.editProfileImgBttn.setOnClickListener {
            SelectAvatarDialog(false).show(supportFragmentManager, "selectAvatar")
        }
        binding.editEmailBttn.setOnClickListener {
            EditEmailDialog().show(supportFragmentManager, "editEmail")
        }
        binding.editPwdBttn.setOnClickListener {
            EditPasswordDialog().show(supportFragmentManager, "editPassword")
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

    private fun setupPaymentMethodsUi() {
        binding.paymentMethodsList.layoutManager = LinearLayoutManager(
            this, VERTICAL, false
        )

        val adapter = PaymentMethodAdapter(::removePaymentMethod)
        binding.paymentMethodsList.adapter = adapter

        profileViewModel.paymentMethods.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun removePaymentMethod(number: String) {

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
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.avatar_impostato))
            .setMessage(getString(R.string.immagine_impostata))
            .setIcon(R.drawable.ic_check_circle_24)
            .setPositiveButton(R.string.okay) { _, _ -> }
            .show()
    }

    override suspend fun checkEmailUsage(email: String): Deferred<Boolean> {
        return Repository.isMailUsed(email)
    }

    override suspend fun isPasswordCorrect(password: String): Deferred<Boolean> {
        return Repository.userExists(loggedEmail, password)
    }

    override suspend fun editEmail(email: String) {
        Repository.changeEmail(loggedEmail, email)
    }

    override suspend fun editPassword(password: String) {
        Repository.resetPassword(loggedEmail, password)
    }

    override fun toEmailEditedConfirm() = toCredentialEditedConfirm(false)

    override fun toPasswordEditedConfirm() = toCredentialEditedConfirm(true)

    private fun toCredentialEditedConfirm(isPassword: Boolean) {
        val titleResId =
            if (isPassword)
                R.string.password_modificata
            else
                R.string.indirizzo_email_modificato

        val messageResId =
            if (isPassword)
                R.string.password_modificata_to_login
            else
                R.string.indirizzo_mail_modificato_to_login

        MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(titleResId))
            .setMessage(getString(messageResId))
            .setIcon(R.drawable.ic_check_circle_24)
            .setCancelable(false)
            .setPositiveButton(R.string.okay) { _, _ ->
                // ritorna alla schermata di login al click
                val intent = Intent(this, StartActivity::class.java)

                // impedisci di tornare alle altre activity premendo il tasto indietro
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(intent)
                finish()
            }
            .show()
    }
}