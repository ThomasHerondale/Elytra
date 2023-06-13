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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogSecurityQuestionBinding

class SecurityQuestionDialog : DialogFragment() {

    private lateinit var binding: DialogSecurityQuestionBinding
    private lateinit var handler: SecurityQuestionHandler
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

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
        // non inviare risposte vuote
        if (answer.isBlank()) {
            binding.answerText.error = getString(R.string.inserisci_risposta)
            return
        }

        coroutineScope.launch {
            val isAnswerOkay = handler.checkAnswer(answer).await()

            if (isAnswerOkay)
                TODO()
            else
                binding.answerText.error = getString(R.string.risposta_errata)
        }
    }

    private fun showSecurityQuestion() {
        enableOrDisableConfirmButton(true)

        coroutineScope.launch {
            val question = handler.fetchSecurityQuestion().await()

            binding.questionText.text = question
            // show layout
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

    interface SecurityQuestionHandler {

        suspend fun fetchSecurityQuestion(): Deferred<String>

        suspend fun checkAnswer(answer: String): Deferred<Boolean>
    }
}