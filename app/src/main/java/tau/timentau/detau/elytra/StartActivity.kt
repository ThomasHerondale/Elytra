package tau.timentau.detau.elytra

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
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

        // comando debug per saltare la schermata di login
        intent.getStringExtra("TEST_EMAIL")?.let { login(it) }

        // imposta il pulsante indietro
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                navigator.popBackStack()
            }
        })

        setContentView(binding.root)
    }

    fun login(email: String) {
        Session.login(email)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
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

    override fun toPasswordResetConfirm() =
        showConfirmDialog(R.string.password_reimpostata, R.string.password_reset_conferma)

    override suspend fun checkEmailExistence(email: String): Deferred<Boolean> {
        return Repository.isMailUsed(email)
    }

    override fun setEmailForPasswordReset(email: String) {
        _emailForPasswordReset = email
        SecurityQuestionDialog().show(supportFragmentManager, "securityQuestion")
    }
}