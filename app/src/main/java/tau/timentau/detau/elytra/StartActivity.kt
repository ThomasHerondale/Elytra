package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
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

    override suspend fun fetchSecurityQuestion(): Deferred<String> {
        return Repository.getSecurityQuestion("test@test.com")
    }

    override suspend fun checkAnswer(answer: String): Deferred<Boolean> {
        return Repository.isAnswerCorrect("test@test.com", answer)
    }

    override fun toPasswordReset() {
        PasswordResetDialog().show(supportFragmentManager, "passwordReset")
    }

    override suspend fun resetPassword(password: String) {
        TODO("Not yet implemented")
    }
}