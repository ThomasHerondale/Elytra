package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        questionBox.setSimpleItems(questions)

        binding.setSecurityQuestionBottomButtons.positiveButton
            .setOnClickListener {
                if (binding.setAnswerText.text.isNotBlank()) {
                }
            }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    interface SetSecurityQuestionHandler {
        fun choiceDone(question: String, answer: String)
    }
}