package tau.timentau.detau.elytra

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Deferred
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityStartBinding
import tau.timentau.detau.elytra.passwordReset.InsertEmailDialog
import tau.timentau.detau.elytra.passwordReset.PasswordResetDialog
import tau.timentau.detau.elytra.passwordReset.SecurityQuestionDialog

class StartActivity :
    AppCompatActivity(),
    NavHostActivity,
    SecurityQuestionDialog.SecurityQuestionHandler,
    PasswordResetDialog.PasswordResetHandler,
    InsertEmailDialog.InsertEmailHandler {

    private lateinit var binding : ActivityStartBinding
    private val navigator by lazy { getNavController() }

    private var _emailForPasswordReset: String? = null
        // vista non nullabile
    private val emailForPasswordReset: String
        get() {
            if (_emailForPasswordReset != null)
                return _emailForPasswordReset as String
            else
                throw IllegalStateException("No email for password reset has been set")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    fun login(email: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EMAIL_KEY, email)
        }
        startActivity(intent)
    }

    override fun navigateTo(directions: NavDirections) {
        navigator.navigate(directions)
    }

    override suspend fun fetchSecurityQuestion(): Deferred<String> {
        return Repository.getSecurityQuestion(emailForPasswordReset)
    }

    override suspend fun checkAnswer(answer: String): Deferred<Boolean> {
        return Repository.isAnswerCorrect(emailForPasswordReset, answer)
    }

    override fun toPasswordReset() {
        PasswordResetDialog().show(supportFragmentManager, "passwordReset")
    }

    override suspend fun resetPassword(newPassword: String) {
        Repository.resetPassword(emailForPasswordReset, newPassword)
    }

    override fun toPasswordResetConfirm() {
        MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.password_reimpostata))
            .setMessage(getString(R.string.password_reset_conferma))
            .setIcon(R.drawable.ic_check_circle_24)
            .setPositiveButton(R.string.okay) { _, _ -> }
            .show()
    }

    override suspend fun checkEmailExistence(email: String): Deferred<Boolean> {
        return Repository.isMailUsed(email)
    }

    override fun setEmailForPasswordReset(email: String) {
        _emailForPasswordReset = email
        SecurityQuestionDialog().show(supportFragmentManager, "securityQuestion")
    }
}