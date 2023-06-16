package tau.timentau.detau.elytra.profile

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.DialogAddPaymentMethodBinding

class AddPaymentMethodDialog : DialogFragment() {

    private lateinit var binding: DialogAddPaymentMethodBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogAddPaymentMethodBinding.inflate(layoutInflater)

            builder
                .setTitle(getString(R.string.nuovo_metodo_di_pagamento))
                .setView(binding.root)
                .create()
                .also {
                    it.setCanceledOnTouchOutside(false)
                }

        } ?: throw IllegalStateException("No activity to attach dialog")
    }

}