package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import tau.timentau.detau.elytra.databinding.DialogSetSecurityQuestionBinding

class SetSecurityQuestionDialog(
    private val securityQuestions: List<String>
) : DialogFragment() {

    private lateinit var binding: DialogSetSecurityQuestionBinding
    private lateinit var handler: SetSecurityQuestionHandler

    private val questions = securityQuestions.toTypedArray()
    private var selectedQuestion: String? = null

    private inline val questionBox: MaterialAutoCompleteTextView
        get() = (binding.questionBox.editText as MaterialAutoCompleteTextView)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as SetSecurityQuestionHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogSetSecurityQuestionBinding.inflate(layoutInflater)

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

        // disabilita il pulsante di conferma all'avvio
        enableOrDisableConfirmButton(true)

        questionBox.setSimpleItems(questions)

        questionBox.setOnItemClickListener { adapter, _, position, _ ->
            selectedQuestion = adapter.getItemAtPosition(position) as String
            enableOrDisableConfirmButton(false)
        }

        binding.setAnswerText.editText?.doOnTextChanged { _, _, _, _ ->
            enableOrDisableConfirmButton(false)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.setProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }


    private fun enableOrDisableConfirmButton(disable: Boolean) {
        binding.setSecurityQuestionBottomButtons.positiveButton.isEnabled =
            !disable &&
            selectedQuestion != null && // abilita solo se la domanda è stata selezionata
            binding.setAnswerText.text.isNotBlank() // abilita solo se la risposta non è vuota

    }

    interface SetSecurityQuestionHandler {
        fun choiceDone(question: String, answer: String)
    }
}