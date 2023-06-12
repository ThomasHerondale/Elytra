package tau.timentau.detau.elytra

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import tau.timentau.detau.elytra.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)

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

        // messaggio di errore vuoto -> campo corretto
        return errorTextRes == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.registerProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }
}