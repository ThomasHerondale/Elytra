package tau.timentau.detau.elytra.profile

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.DialogAddPaymentMethodBinding
import tau.timentau.detau.elytra.parseToDate
import tau.timentau.detau.elytra.setDialogResult
import tau.timentau.detau.elytra.text

const val METHOD_CREATION_OK = "method_creation_ok"
const val METHOD_CREATION_FAILED = "method_creation_failed"

class AddPaymentMethodDialog : DialogFragment() {

    private lateinit var binding: DialogAddPaymentMethodBinding

    private val viewModel: AddPaymentMethodViewModel by viewModels()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        setupCircuitObserver()
        setupNumberField()

        binding.expiryText.editText?.setOnFocusChangeListener { _, _ -> validateExpiryDateField() }
        binding.securityCodeText.editText?.setOnFocusChangeListener { _, _ ->
            validateSafetyCodeField()
        }
        binding.ownerNameText.editText?.setOnFocusChangeListener { _, _ ->
            validateOwnerNameField()
        }
        binding.numberText.editText?.setOnFocusChangeListener { _, _ -> validateNumberField() }

        binding.addPaymentMethodDialogBottomButtons.negativeButton.setOnClickListener { dismiss() }

        // Pulsante di conferma -> creazione del metodo di pagamento

        binding.addPaymentMethodDialogBottomButtons.positiveButton.setOnClickListener {
            if (validateFields()) {
                viewModel.createPaymentMethod(
                    binding.numberText.text,
                    binding.expiryText.text.parseToDate(),
                    binding.securityCodeText.text,
                    binding.ownerNameText.text
                )
            }
        }

        viewModel.paymentMethodCreationStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> { setDialogResult(METHOD_CREATION_FAILED) }
                is Status.Loading -> binding.newCardProgress.visibility = View.VISIBLE
                is Status.Success -> { setDialogResult(METHOD_CREATION_OK) }
            }
        }

        return binding.root
    }

    private fun validateFields() =
        validateNumberField() and
        validateExpiryDateField() and
        validateSafetyCodeField() and
        validateOwnerNameField()


    private fun setupNumberField() {
        /* istanzia il componente che si occupa di aggiungere gli spazi al numero di carta
         * durante l'inserimento */
        binding.numberText.editText?.addTextChangedListener(object : TextWatcher {

            private val SPACE_REGEX = Regex("(\\d{4})")
            private val SPACE_REPLACEMENT = "$1 "

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // inutile
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // inutile
            }

            override fun afterTextChanged(s: Editable?) {
                val editText = binding.numberText.editText!!

                // evitiamo le chiamate ricorsive
                editText.removeTextChangedListener(this)

                // rimuovi ogni tipo di spazio
                val text = editText.text.replace(Regex("\\s"), "")

                // aggiungi gli spazi ogni quattro cifre
                val formattedText = text.replace(SPACE_REGEX, SPACE_REPLACEMENT).trim()

                editText.setText(formattedText)

                // sposta il cursore alla fine del testo
                editText.setSelection(formattedText.length)

                // resetta il watcher
                editText.addTextChangedListener(this)
            }
        })

        setupCircuitObserver()
    }

    private fun setupCircuitObserver() {
        binding.numberText.editText?.doOnTextChanged { text, start, _, _ ->
            /* se la prima cifra è cambiata e il campo non è vuoto, aggiorna il circuito */
            if (start == 0)
                viewModel.updateCircuit(text!!.firstOrNull())
        }

        viewModel.currentCircuit.observe(viewLifecycleOwner) {
            if (it != null) {
                /* se il numero inserito corrisponde a un circuito esistente
                 * carica l'immagine del logo */
                Glide
                    .with(binding.newCardLogo)
                    .load(it.logo)
                    .into(binding.newCardLogo)

                binding.numberText.error = null
            } else {
                // elimina l'immagine del logo
                Glide
                    .with(binding.newCardLogo)
                    .clear(binding.newCardLogo)

                /* se il campo è vuoto, non mostrare nulla. Altrimenti, il circuito non
                 * è supportato */
                if (binding.numberText.text.isNotBlank())
                    binding.numberText.error = getString(R.string.circuito_non_supportato)
                else
                    binding.numberText.error = null
            }
        }
    }

    private fun validateNumberField(): Boolean {
        // se il campo non è vuoto e il testo è lungo 19 caratteri (16 cifre e 3 spazi)
        if (binding.numberText.text.isBlank() || binding.numberText.text.length != 19)
            binding.numberText.error = getString(R.string.inserisci_num_carta_valido)
        else
            binding.numberText.error = null

        return binding.numberText.error == null
    }

    private fun validateExpiryDateField(): Boolean {
        try {
            val date = binding.expiryText.text.parseToDate()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // controlla se la carta è già scaduta
            if (date <= today)
                binding.expiryText.error = getString(R.string.carta_scaduta)
            else
                binding.expiryText.error = null

        } catch (e: IllegalArgumentException) {
            binding.expiryText.error = getString(R.string.data_invalida)
        }

        return binding.expiryText.error == null
    }

    private fun validateSafetyCodeField(): Boolean {
        if (binding.securityCodeText.text.length != 3)
            binding.securityCodeText.error = " "
        else
            binding.securityCodeText.error = null

        return binding.securityCodeText.error == null
    }

    private fun validateOwnerNameField(): Boolean {
        if (binding.ownerNameText.text.isBlank())
            binding.ownerNameText.error = getString(R.string.inserisci_nome_intestatario)
        else
            binding.ownerNameText.error = null

        return binding.ownerNameText.error == null
    }
}