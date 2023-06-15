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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogEditPasswordBinding

private const val TAG = "EDIT_PASSWORD"

class EditPasswordDialog : DialogFragment() {

    private lateinit var binding: DialogEditPasswordBinding
    private lateinit var handler: EditPasswordHandler
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        networkError()
        Log.e(TAG, e.stackTraceToString())
        dismiss()
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as EditPasswordHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogEditPasswordBinding.inflate(layoutInflater)

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

        binding.editPasswordDialogBottomButtons.negativeButton.setOnClickListener { dismiss() }

        binding.editPasswordDialogBottomButtons.positiveButton.setOnClickListener {
            coroutineScope.launch {
                if (validatePasswordField() and validateConfirmField()) {
                    showOrHideProgressBar(false)

                    val isPasswordCorrect =
                        handler.isPasswordCorrect(binding.oldPwdText.text).await()

                    if (isPasswordCorrect) {
                        handler.editPassword(binding.newPwdText.text)
                        dismiss()
                        handler.toPasswordEditedConfirm()
                    } else {
                        showOrHideProgressBar(true)
                        binding.oldPwdText.error = getString(R.string.password_incorretta)
                    }
                }
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }


    private fun validatePasswordField(): Boolean {
        binding.newPwdText.text.let {
            binding.newPwdText.error = when {
                (it.length !in 6..18) ->
                    getString(R.string.pwd_corta)
                it.contains(Regex("\\s")) ->
                    getString(R.string.pwd_spaziata)
                !it.matches(Regex("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*")) ->
                    getString(R.string.pwd_invalida)
                else -> null
            }
        }

        return binding.newPwdText.error == null
    }

    private fun validateConfirmField(): Boolean {
        val password = binding.newPwdText.text
        val confirm = binding.editConfirmText.text

        if (confirm.isBlank() || confirm != password)
            binding.editConfirmText.error = getString(R.string.conferma_invalida)
        else
            binding.editConfirmText.error = null

        return binding.editConfirmText.error == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.editPwdProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
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


    interface EditPasswordHandler {

        suspend fun isPasswordCorrect(password: String): Deferred<Boolean>

        suspend fun editPassword(password: String)

        fun toPasswordEditedConfirm()
    }
}