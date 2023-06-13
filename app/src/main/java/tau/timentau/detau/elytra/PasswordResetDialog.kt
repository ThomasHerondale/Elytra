package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tau.timentau.detau.elytra.databinding.DialogPasswordResetBinding

class PasswordResetDialog : DialogFragment() {

    private lateinit var binding: DialogPasswordResetBinding
    private lateinit var handler: PasswordResetHandler

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

    interface PasswordResetHandler {
    }
}