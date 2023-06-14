package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogPasswordResetBinding

private const val TAG = "PASSWORD_RESET"

class PasswordResetDialog : DialogFragment() {

    private lateinit var binding: DialogPasswordResetBinding
    private lateinit var handler: PasswordResetHandler
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        networkError()
        Log.e(TAG, e.stackTraceToString())
    }
    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + coroutineExceptionHandler
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as PasswordResetHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogPasswordResetBinding.inflate(layoutInflater)

            builder
                .setTitle(getString(R.string.reimposta_password))
                .setView(binding.root)
                .create()
                .also {
                    it.setCanceledOnTouchOutside(false)
                }

        } ?: throw IllegalStateException("No activity to attach dialog")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.resetPasswordText.setOnFocusChangeListener { _, _ -> validatePasswordField() }
        binding.resetConfirmText.setOnFocusChangeListener { _, _ -> validateConfirmField() }

        binding.passwordResetDialogBottomButtons.negativeButton
            .setOnClickListener { dismiss() }

        binding.passwordResetDialogBottomButtons.positiveButton
            .setOnClickListener {
                if (validatePasswordField() and validateConfirmField())
                    resetConfirmed()
            }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun resetConfirmed() {
        val password = binding.resetPasswordText.text

        showOrHideProgressBar(false)
        coroutineScope.launch {
            handler.resetPassword(password)
            dismiss()
            handler.toPasswordResetConfirm()
        }
            .invokeOnCompletion { showOrHideProgressBar(true) }
    }

    private fun validatePasswordField(): Boolean {
        binding.resetPasswordText.text.let {
            binding.resetPasswordText.error = when {
                (it.length !in 6..18) ->
                    getString(R.string.pwd_corta)
                it.contains(Regex("\\s")) ->
                    getString(R.string.pwd_spaziata)
                !it.matches(Regex("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*")) ->
                    getString(R.string.pwd_invalida)
                else -> null
            }
        }

        return binding.resetPasswordText.error == null
    }

    private fun validateConfirmField(): Boolean {
        val password = binding.resetPasswordText.text
        val confirm = binding.resetConfirmText.text

        if (confirm != password)
            binding.resetConfirmText.error = getString(R.string.conferma_invalida)
        else
            binding.resetConfirmText.error = null

        return binding.resetConfirmText.error == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.resetProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun networkError() {
        MaterialAlertDialogBuilder(
            requireActivity(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(R.string.errore_connessione)
            .setMessage(R.string.imposs_connettersi_al_server)
            .setIcon(R.drawable.ic_link_off_24)
            .setPositiveButton(R.string.okay) { _, _ -> }
            .show()
    }

    interface PasswordResetHandler {

        suspend fun resetPassword(newPassword: String)

        fun toPasswordResetConfirm()
    }
}