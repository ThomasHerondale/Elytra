package tau.timentau.detau.elytra.profile

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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.DialogEditEmailBinding
import tau.timentau.detau.elytra.isNotEmail
import tau.timentau.detau.elytra.showNetworkErrorDialog
import tau.timentau.detau.elytra.text

private const val TAG = "EDIT_EMAIL"

class EditEmailDialog : DialogFragment() {

    private lateinit var binding: DialogEditEmailBinding
    private lateinit var handler: EditEmailHandler
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        showNetworkErrorDialog()
        Log.e(TAG, e.stackTraceToString())
        dismiss()
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as EditEmailHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogEditEmailBinding.inflate(layoutInflater)

            builder
                .setTitle(getString(R.string.modifica_email))
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

        binding.newEmailText.setOnFocusChangeListener { _, _ -> validateMailField() }

        binding.editEmailDialogBottomButtons.negativeButton.setOnClickListener { dismiss() }
        binding.editEmailDialogBottomButtons.positiveButton.setOnClickListener {

            if (validateMailField()) {
                showOrHideProgressBar(false)
                coroutineScope.launch {
                    // se i dati sono corretti, richiedi la modifica
                    if (validateFields()) {
                        handler.editEmail(binding.newEmailText.text)
                        dismiss()
                        handler.toEmailEditedConfirm()
                    }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private suspend fun validateFields(): Boolean {
        // richiedi i controlli
        val isEmailused =
            handler.checkEmailUsage(binding.newEmailText.text).await()
        val isPasswordCorrect =
            handler.isPasswordCorrect(binding.editEmailPwdText.text).await()

        // mostra gli eventuali errori
        if (isEmailused) {
            binding.newEmailText.error = getString(R.string.mail_in_uso)
            showOrHideProgressBar(true)
        }
        if (!isPasswordCorrect) {
            binding.editEmailPwdText.error = getString(R.string.password_non_corretta)
            showOrHideProgressBar(true)
        }

        return !isEmailused && isPasswordCorrect
    }

    private fun validateMailField(): Boolean {
        var errorTextRes: Int? = null

        if (binding.newEmailText.text.isNotEmail())
            errorTextRes = R.string.mail_invalida

        if (errorTextRes != null)
            binding.newEmailText.error = getString(errorTextRes)
        else
            binding.newEmailText.error = null

        // messaggio di errore vuoto -> campo corretto
        return errorTextRes == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.editEmailProgress.visibility = if (hide) View.GONE else View.VISIBLE
    }

    interface EditEmailHandler {

        suspend fun checkEmailUsage(email: String): Deferred<Boolean>

        suspend fun isPasswordCorrect(password: String): Deferred<Boolean>

        suspend fun editEmail(email: String)

        fun toEmailEditedConfirm()
    }
}