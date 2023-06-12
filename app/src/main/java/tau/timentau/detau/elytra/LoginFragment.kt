package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.FragmentLoginBinding

private const val TAG = "LOGIN"

class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        showOrHideProgressBar(true)
        networkError()
        Log.e(TAG, e.stackTraceToString())
    }
    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + coroutineExceptionHandler
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginBttn.setOnClickListener {
            attemptLogin(binding.emailText.text, binding.pwdText.text)
        }
    }

    private fun attemptLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank())
            return

        showOrHideProgressBar(false)
        Log.v(TAG, "Checking existence for ($email, $password)")

        coroutineScope.launch {
            val userExists = Repository.userExists(email, password).await()
            showOrHideProgressBar(true)
            if (userExists)
                    (requireActivity() as EntryActivity).login(email)
            else
                loginIncorrect()
        }
    }

    private fun loginIncorrect() {
        MaterialAlertDialogBuilder(
            requireActivity(),
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(R.string.login_fallito))
            .setMessage(R.string.dati_inseriti_incorretti)
            .setIcon(R.drawable.ic_problem_48)
            .setPositiveButton(R.string.okay) { _, _ -> } // non fare nulla, il dismiss è automatico
            .show()
    }


    private fun networkError() {
        MaterialAlertDialogBuilder(
            requireActivity(),
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(R.string.errore_connessione)
            .setMessage(R.string.imposs_connettersi_al_server)
            .setIcon(R.drawable.ic_link_off_24)
            .setPositiveButton(R.string.okay) { _, _ -> }
            .show()
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.loginProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }
}