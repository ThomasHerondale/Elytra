package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SetSecurityQuestionDialog.SetSecurityQuestionHandler {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        checkForFirstAccess()

        setContentView(binding.root)
    }

    private fun checkForFirstAccess() {
        val progressDialog = showProgressDialog()

        // todo exception
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

    override fun choiceDone(question: String, answer: String) {
        TODO("Not yet implemented")
    }
}