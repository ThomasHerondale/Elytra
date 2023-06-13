package tau.timentau.detau.elytra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Deferred
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityStartBinding

class StartActivity :
    AppCompatActivity(),
    NavHostActivity,
    SecurityQuestionDialog.SecurityQuestionHandler,
    PasswordResetDialog.PasswordResetHandler {

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

    // todo email dialog

    override suspend fun fetchSecurityQuestion(): Deferred<String> {
        return Repository.getSecurityQuestion("test@test.com")
    }

    override suspend fun checkAnswer(answer: String): Deferred<Boolean> {
        return Repository.isAnswerCorrect("test@test.com", answer)
    }

    override fun toPasswordReset() {
        PasswordResetDialog().show(supportFragmentManager, "passwordReset")
    }

    override suspend fun resetPassword(newPassword: String) {
        Repository.resetPassword("test@test.com", newPassword)
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
}