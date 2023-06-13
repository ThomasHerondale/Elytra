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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.FragmentRegisterBinding
import tau.timentau.detau.elytra.model.Sex
import tau.timentau.detau.elytra.model.Sex.FEMALE
import tau.timentau.detau.elytra.model.Sex.MALE
import tau.timentau.detau.elytra.model.Sex.OTHER

private const val TAG = "REGISTRATION"

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        showOrHideProgressBar(true)
        networkError()
        Log.e(TAG, e.stackTraceToString())
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mailText.editText?.setOnFocusChangeListener { _, _ -> validateMailField() }
        binding.passText.editText?.setOnFocusChangeListener { _, _ -> validatePasswordField() }
        binding.confirmText.editText?.setOnFocusChangeListener { _, _ -> validateConfirmField() }
        binding.dateText.editText?.setOnFocusChangeListener { _, _ -> validateBirthDateField() }

        binding.registerBttn.setOnClickListener {
            showOrHideProgressBar(false)

            coroutineScope.launch {
                if (checkMailField() and validateForm()) {
                    registerUser()
                    // todo return to login screen
                }
            }
        }
    }

    private suspend fun registerUser() {
        val fullName = binding.nameText.text
        val email = binding.mailText.text
        val password = binding.passText.text
        val birthDate = binding.dateText.text.parseToDate()
        val sex = selectedSex()

        Repository.createUser(email, fullName, birthDate, sex, password)
    }

    private suspend fun checkMailField(): Boolean {
        try {
            val isEmailUsed = Repository.isMailUsed(binding.mailText.text).await()

            var errorTextRes: Int? = null

            if (binding.mailText.text.isNotEmail())
                errorTextRes = R.string.mail_invalida

            if (errorTextRes != null)
                binding.mailText.error = getString(errorTextRes)
            else
                binding.mailText.error = null

            if (isEmailUsed)
                binding.mailText.error = getString(R.string.mail_in_uso)

            showOrHideProgressBar(true)
            return binding.mailText.error == null
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            networkError()

            showOrHideProgressBar(true)
            return false
        }
    }

    private fun validateForm() =
        validatePasswordField() and validateConfirmField() and validateBirthDateField() and
                validateSexSelection()

    private fun validateMailField(): Boolean {
        var errorTextRes: Int? = null

        if (binding.mailText.text.isNotEmail())
            errorTextRes = R.string.mail_invalida

        if (errorTextRes != null)
            binding.mailText.error = getString(errorTextRes)
        else
            binding.mailText.error = null

        // nuovo perché non gestirà le eccezioni allo stesso modo di quello d'istanza
        CoroutineScope(Dispatchers.Main).launch {
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

    private fun validateBirthDateField(): Boolean {
        try {
            val date = binding.dateText.text.parseToDate()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            if (date > today.minus(18, DateTimeUnit.YEAR))
                binding.dateText.error = getString(R.string.data_non_maggiorenne)
            else
                binding.dateText.error = null

        } catch (e: IllegalArgumentException) {
            binding.dateText.error = getString(R.string.data_invalida)
        }

        return binding.dateText.error == null
    }

    private fun validateSexSelection(): Boolean {
        var resId = 0

        if (binding.sexChips.noChipSelected()) {
            resId = R.drawable.sex_chipgroup_error_bground
            binding.sexError.text = getString(R.string.seleziona_sesso)
        } else {
            binding.sexError.text = null
        }

        binding.sexChips.setBackgroundResource(resId)

        // resId == 0 -> no bground -> sesso selezionato
        return resId == 0
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.registerProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun selectedSex(): Sex {
        return when(binding.sexChips.checkedChipId) {
            R.id.maleChip -> MALE
            R.id.femaleChip -> FEMALE
            R.id.otherChip -> OTHER
            else -> throw IllegalStateException("No sex selected")
        }
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
}