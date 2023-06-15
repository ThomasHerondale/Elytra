package tau.timentau.detau.elytra.passwordReset

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
import tau.timentau.detau.elytra.databinding.DialogSecurityQuestionBinding
import tau.timentau.detau.elytra.text

private const val TAG = "SECURITY_QUESTION"

class SecurityQuestionDialog : DialogFragment() {

    private lateinit var binding: DialogSecurityQuestionBinding
    private lateinit var handler: SecurityQuestionHandler
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        networkError()
        Log.e(TAG, e.stackTraceToString())
        dismiss()
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as SecurityQuestionHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogSecurityQuestionBinding.inflate(layoutInflater)

            builder
                .setTitle(getString(R.string.domanda_sicurezza))
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
        showSecurityQuestion()

        binding.securityQuestionDialogBottomButtons.negativeButton
            .setOnClickListener { dismiss() }

        binding.securityQuestionDialogBottomButtons.positiveButton
            .setOnClickListener { answerConfirmed() }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun answerConfirmed() {
        val answer = binding.answerText.text
        if (answer.isBlank()) {
            // segnala se la risposta è vuota e non inviarla
            binding.answerText.error = getString(R.string.inserisci_risposta)
            return
        }

        // disabilita il pulsante di conferma mentre l'operazione è in corso
        enableOrDisableConfirmButton(true)

        coroutineScope.launch {
            val isAnswerOkay = handler.checkAnswer(answer).await()

            if (isAnswerOkay) {
                dismiss()
                handler.toPasswordReset()
            } else
                binding.answerText.error = getString(R.string.risposta_errata)
        }
            .invokeOnCompletion { enableOrDisableConfirmButton(false) }
    }

    private fun showSecurityQuestion() {
        enableOrDisableConfirmButton(true)

        coroutineScope.launch {
            val question = handler.fetchSecurityQuestion().await()

            binding.questionText.text = question
            // mostra le componenti quando la domanda è carica
            binding.questionProgress.visibility = View.GONE
            binding.questionText.visibility = View.VISIBLE
            binding.answerText.visibility = View.VISIBLE

            enableOrDisableConfirmButton(false)
        }
    }

    private fun enableOrDisableConfirmButton(disable: Boolean) {
        binding.securityQuestionDialogBottomButtons
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

    interface SecurityQuestionHandler {

        suspend fun fetchSecurityQuestion(): Deferred<String>

        suspend fun checkAnswer(answer: String): Deferred<Boolean>

        fun toPasswordReset()
    }
}