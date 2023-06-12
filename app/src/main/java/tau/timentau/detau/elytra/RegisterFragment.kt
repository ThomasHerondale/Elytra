package tau.timentau.detau.elytra

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)

        binding.mailText.editText?.setOnFocusChangeListener { _, _ -> validateMailField() }
        binding.passText.editText?.setOnFocusChangeListener { _, _ -> validatePasswordField() }
        binding.confirmText.editText?.setOnFocusChangeListener { _, _ -> validateConfirmField() }

        return binding.root
    }

    private fun validateMailField(): Boolean {
        var errorTextRes: Int? = null

        if (binding.mailText.text.isNotEmail())
            errorTextRes = R.string.mail_invalida

            if (errorTextRes != null)
                binding.mailText.error = getString(errorTextRes)
            else
                binding.mailText.error = null

        coroutineScope.launch {
            try {
                val isEmailUsed = Repository.isMailUsed(binding.mailText.text).await()
                if (isEmailUsed)
                    binding.mailText.error = getString(R.string.mail_in_uso)
            } catch (_: Exception) {
                // ignora le eccezioni, il controllo sarà bloccante una volta premuto registrati
            }
        }

        // messaggio di errore vuoto -> campo corretto
        return errorTextRes == null
    }

    private fun validatePasswordField(): Boolean {
        binding.passText.text.let {
            binding.passText.error = when {
                (it.length !in 6..18) ->
                    getString(R.string.pwd_corta)
                it.contains(Regex("\\s")) ->
                    getString(R.string.pwd_spaziata)
                !it.matches(Regex("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*")) ->
                    getString(R.string.pwd_invalida)
                else -> null
            }
        }

        return binding.passText.error == null
    }

    private fun validateConfirmField(): Boolean {
        val password = binding.passText.text
        val confirm = binding.confirmText.text

        if (confirm.isBlank() || confirm != password)
            binding.confirmText.error = getString(R.string.conferma_invalida)
        else
            binding.confirmText.error = null

        return binding.confirmText.error == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.registerProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }
}