package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogInsertEmailBinding

private const val TAG = "MAIL_FOR_RESET"

class InsertEmailDialog : DialogFragment() {

    private lateinit var binding: DialogInsertEmailBinding
    private lateinit var handler: InsertEmailHandler
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        networkError()
        Log.e(TAG, e.stackTraceToString())
        dismiss()
    }
    private val coroutineScope = CoroutineScope(
        Dispatchers.Main + coroutineExceptionHandler
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as InsertEmailHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogInsertEmailBinding.inflate(layoutInflater)

            builder
                .setTitle(getString(R.string.inserisci_email))
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

        binding.insertEmailDialogBottomButtons.negativeButton
            .setOnClickListener { dismiss() }

        binding.insertEmailDialogBottomButtons.positiveButton
            .setOnClickListener {
                val email = binding.mailForResetText.text
                // non inviare richieste con email vuote
                if (email.isNotBlank()) {
                    showOrHideProgressBar(false)

                    coroutineScope.launch {
                        emailConfirmed()
                    }
                        .invokeOnCompletion { showOrHideProgressBar(true) }
                }
            }

        // disabilita il pulsante di conferma all'avvio
        enableOrDisableConfirmButton(true)

        binding.mailForResetText.editText!!.addTextChangedListener {
            // ad ogni cambiamento del testo
            if (it.isNullOrBlank())
                enableOrDisableConfirmButton(true)
            else
                enableOrDisableConfirmButton(false)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private suspend fun emailConfirmed() {
        val email = binding.mailForResetText.text
        val emailExists = handler.checkEmailExistence(email).await()

        println(emailExists)

        if (emailExists) {
            handler.setEmailForPasswordReset(email)
            dismiss()
        } else {
            binding.mailForResetText.error = getString(R.string.email_non_associata_msg)
        }
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.mailCheckProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun enableOrDisableConfirmButton(disable: Boolean) {
        binding.insertEmailDialogBottomButtons
            .positiveButton.isEnabled = !disable
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

    interface InsertEmailHandler {

        suspend fun checkEmailExistence(email: String): Deferred<Boolean>

        fun setEmailForPasswordReset(email: String)
    }
}