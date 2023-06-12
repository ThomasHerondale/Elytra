package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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
        Log.v("LOGIN", "Checking existence for ($email, $password)")

        coroutineScope.launch {
            val userExists = Repository.userExists(email, password).await()
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
            .setMessage(R.string.dati_inseriti_ncorretti)
            .setIcon(R.drawable.ic_problem_48dp)
            .setPositiveButton(R.string.okay) { _, _ -> } // non fare nulla, il dismiss è automatico
            .show()
    }
}