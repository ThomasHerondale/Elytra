package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogPasswordResetBinding

class PasswordResetDialog : DialogFragment() {

    private lateinit var binding: DialogPasswordResetBinding
    private lateinit var handler: PasswordResetHandler
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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

    interface PasswordResetHandler {

        suspend fun resetPassword(newPassword: String)
    }
}