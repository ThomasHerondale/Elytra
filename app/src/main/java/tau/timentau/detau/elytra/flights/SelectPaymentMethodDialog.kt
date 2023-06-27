package tau.timentau.detau.elytra.flights

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.google.android.material.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.DialogSelectPaymentMethodBinding
import tau.timentau.detau.elytra.showNetworkErrorDialog

class SelectPaymentMethodDialog : DialogFragment() {

    private lateinit var binding: DialogSelectPaymentMethodBinding
    private val viewModel: PaymentViewModel by viewModels()

    private var currentSelectedItem: Int? = null
    private lateinit var handler: SelectPaymentMethodHandler

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as SelectPaymentMethodHandler
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogSelectPaymentMethodBinding.inflate(layoutInflater)

            binding.dialogSelectPaymentMethodBottomButtons.positiveButton.isEnabled = false

            setupPaymentMethodsList()

            binding.dialogSelectPaymentMethodBottomButtons.negativeButton.setOnClickListener {
                dismiss()
            }

            binding.dialogSelectPaymentMethodBottomButtons.positiveButton.setOnClickListener {
                val progressDialog = showProgressDialog()
                CoroutineScope(Dispatchers.Main).launch {
                    delay((3001 + Math.random() * 3001).toLong())
                    progressDialog.cancel()
                    showConfirmDialog()
                    dismiss()
                }
            }

            builder
                .setTitle("Selezione metodo di pagamento")
                .setView(binding.root)
                .create()
                .also {
                    it.setCanceledOnTouchOutside(false)
                }

        } ?: throw IllegalStateException("No activity to attach dialog")
    }

    private fun setupPaymentMethodsList() {
        val adapter = PaymentMethodAdapter {
            if (currentSelectedItem == null)
                enableConfirmButton()
            currentSelectedItem = it
        }

        viewModel.paymentMethodsFetchStatus.observe(this) { status ->
            when (status) {
                is Status.Failed -> showNetworkErrorDialog()
                is Status.Loading -> showOrHideProgressBar(false)
                is Status.Success -> {
                    showOrHideProgressBar(true)
                    adapter.submitList(status.data.map { PaymentMethodChoice(it) })
                }
            }
        }

        viewModel.fetchPaymentMethods()

        binding.selectPaymentList.layoutManager = LinearLayoutManager(
            requireContext(), VERTICAL, false
        )

        binding.selectPaymentList.adapter = adapter
    }

    private fun showProgressDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(tau.timentau.detau.elytra.R.string.pagamento_in_corso))
            .setView(tau.timentau.detau.elytra.R.layout.dialog_progress_simple)
            .setCancelable(false)
            .show()
    }

    private fun showConfirmDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(getString(tau.timentau.detau.elytra.R.string.pagamento_effettuato))
            .setMessage(getString(tau.timentau.detau.elytra.R.string.importo_saldato_correttamente))
            .setIcon(tau.timentau.detau.elytra.R.drawable.ic_check_circle_24)
            .setPositiveButton(tau.timentau.detau.elytra.R.string.okay) { _, _ ->
                handler.paymentDone()
            }
            .show()
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.paymentFetchProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }


    private fun enableConfirmButton() {
        binding.dialogSelectPaymentMethodBottomButtons.positiveButton.isEnabled = true
    }

    interface SelectPaymentMethodHandler {

        fun paymentDone()
    }
}