package tau.timentau.detau.elytra

import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.delay
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
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogSecurityQuestionBinding.inflate(layoutInflater)

            builder
                .setTitle(getString(R.string.domanda_sicurezza))
                .setView(binding.root)
                .create()

        } ?: throw IllegalStateException("No activity to attach dialog")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showSecurityQuestion()
        return super.onCreateView(inflater, container, savedInstanceState)
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
    }
}