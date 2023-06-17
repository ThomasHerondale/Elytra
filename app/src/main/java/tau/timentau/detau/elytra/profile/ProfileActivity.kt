package tau.timentau.detau.elytra.profile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.Session
import tau.timentau.detau.elytra.Session.loggedEmail
import tau.timentau.detau.elytra.StartActivity
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityProfileBinding
import tau.timentau.detau.elytra.firstAccess.SelectAvatarDialog
import tau.timentau.detau.elytra.hiddenPasswordString
import tau.timentau.detau.elytra.setDialogResultListener
import tau.timentau.detau.elytra.show
import tau.timentau.detau.elytra.showConfirmDialog
import tau.timentau.detau.elytra.showNetworkErrorDialog
import tau.timentau.detau.elytra.toReadable

private const val TAG = "PROFILE"

class ProfileActivity : AppCompatActivity(),
    SelectAvatarDialog.SelectAvatarHandler,
    EditEmailDialog.EditEmailHandler,
    EditPasswordDialog.EditPasswordHandler {

    private lateinit var binding: ActivityProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        showOrHideProgressBar(true)
        showNetworkErrorDialog()
        Log.e(TAG, e.stackTraceToString())
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        // aggiorna i dati del profilo
        profileViewModel.retrieveUserData()
        profileViewModel.retrievePaymentMethods()

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

        binding.addCardBttn.setOnClickListener {
            AddPaymentMethodDialog()
                .show(supportFragmentManager)
                .setDialogResultListener(METHOD_CREATION_OK) { paymentMethodCreatedConfirm() }
                .setDialogResultListener(METHOD_CREATION_FAILED) { showNetworkErrorDialog() }
        }

        binding.logoutBttn.setOnClickListener { showLogoutConfirmDialog() }

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

        val adapter = PaymentMethodAdapter {// al click su un pulsante rimuovi
            removePaymentMethod(it)
        }
        binding.paymentMethodsList.adapter = adapter

        profileViewModel.paymentMethods.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun removePaymentMethod(number: String) {
        showOrHideProgressBar(false)
        coroutineScope.launch {
            Repository.removePaymentMethod(number)
        }
            .invokeOnCompletion {
                showOrHideProgressBar(true)
                if (it == null) {
                    // aggiorna la lista delle carte solo se non ci sono stati problemi
                    profileViewModel.reloadPaymentMethods()
                }
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
        profileViewModel.reloadUserData()

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

    private fun paymentMethodCreatedConfirm() {
        profileViewModel.reloadPaymentMethods()

        MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.metodo_di_pagamento_aggiunto))
            .setMessage(getString(R.string.carta_associata_profilo))
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

        showConfirmDialog(titleResId, messageResId)
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.removeCardProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun logoutConfirmed() {
        Session.invalidate()

        val intent = Intent(this, StartActivity::class.java)
        // impedisci di tornare qui premendo indietro
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)
        finish()
    }

    private fun showLogoutConfirmDialog() {
        MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.sicuro_del_logout))
            .setIcon(R.drawable.ic_error_24)
            .setPositiveButton(R.string.logout_title) { _, _ ->
                logoutConfirmed()
            }
            .setNegativeButton(R.string.annulla) { _, _ -> }
            .show()
    }

}