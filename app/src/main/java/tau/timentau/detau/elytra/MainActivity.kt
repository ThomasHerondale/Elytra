package tau.timentau.detau.elytra

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.Session.loggedEmail
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityMainBinding
import tau.timentau.detau.elytra.firstAccess.SelectAvatarDialog
import tau.timentau.detau.elytra.firstAccess.SetSecurityQuestionDialog
import tau.timentau.detau.elytra.profile.ProfileActivity

class MainActivity :
    AppCompatActivity(),
    SetSecurityQuestionDialog.SetSecurityQuestionHandler,
    SelectAvatarDialog.SelectAvatarHandler {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        checkForFirstAccess()

        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.profile -> toProfile()
                else -> throw IllegalStateException("Unknown menu item")
            }
        }

        setContentView(binding.root)
    }

    private fun checkForFirstAccess() {
        val progressDialog = showProgressDialog()

        try {
            CoroutineScope(Dispatchers.Main).launch {
                val isFirstAccess = Repository.isFirstAccess(loggedEmail).await()
                if (isFirstAccess) {
                    Log.i("FIRST_ACCESS", "User $loggedEmail first access")

                    val questions = Repository.getSecurityQuestions().await()
                    Log.i("FIRST_ACCESS", "Fetched questions from database")

                    startFirstAccessProcedure(questions)
                }
            }
                .invokeOnCompletion { progressDialog.cancel() }
        } catch (e: Exception) {
            Log.e("FIRST_ACCESS", e.stackTraceToString())
            networkErrorOnFirstAccess()
        }

    }

    private fun startFirstAccessProcedure(securityQuestions: List<String>) {
        SetSecurityQuestionDialog(securityQuestions)
            .show(supportFragmentManager, "setSecurityQuestion")
    }

    private fun showProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setView(R.layout.dialog_progress_simple)
            .setCancelable(false)
            .show()
    }

    private fun toProfile(): Boolean {
        startActivity(Intent(this, ProfileActivity::class.java))
        return true
    }

    private fun networkErrorOnFirstAccess() {
        MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(R.string.errore_connessione)
            .setMessage(R.string.imposs_connettersi_al_server_torna_al_login)
            .setIcon(R.drawable.ic_link_off_24)
            .setPositiveButton(R.string.okay) { _, _ ->
                goBackToLogin()
            }
            .show()
    }

    private fun goBackToLogin() {
        startActivity(Intent(this, StartActivity::class.java))
    }

    override suspend fun questionSelected(question: String, answer: String) {
        Repository.setupSecurityQuestion(loggedEmail, question, answer)
    }

    override fun toAvatarSelection() {
        SelectAvatarDialog(true).show(supportFragmentManager, "selectAvatar")
    }

    override fun connectionError(e: Throwable) {
        Log.e("FIRST_ACCESS", e.stackTraceToString())
        networkErrorOnFirstAccess()
    }

    override suspend fun fetchAvatars(): Deferred<List<Bitmap>> {
        return Repository.getAvatars()
    }

    override suspend fun avatarSelected(id: Int) {
        Repository.setAvatar(loggedEmail, id)
    }

    override fun toAvatarSetConfirm() =
        showConfirmDialog(R.string.avatar_impostato, R.string.immagine_impostata)
}