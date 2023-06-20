package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentFlightsBinding
import tau.timentau.detau.elytra.model.Company
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

private const val TAG = "FLIGHTS"

class FlightsFragment : Fragment() {

    private lateinit var binding: FragmentFlightsBinding
    private val flightsViewModel: FlightsViewModel by viewModels()

    private inline val departureAptField: MaterialAutoCompleteTextView
        get() = binding.departureAptText.editText as MaterialAutoCompleteTextView

    private inline val arrivalAptField: MaterialAutoCompleteTextView
        get() = binding.arrivalAptText.editText as MaterialAutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFlightsBinding.inflate(layoutInflater)

        setupAirportFields()
        setupCompanyFilters()

        // imposta lo slider in euro senza centesimi
        binding.priceSlider.setLabelFormatter {
            val formatter = NumberFormat.getCurrencyInstance()
            formatter.minimumFractionDigits = 0
            formatter.currency = Currency.getInstance(Locale.ITALY)
            formatter.format(it)
        }

        binding.searchFlightsBttn.setOnClickListener {
            if (validateFields())
                performSearch()
        }

        return binding.root
    }

    private fun performSearch() {
        flightsViewModel.flightsFetchStatus.observe(viewLifecycleOwner) {
            // TODO
            when (it) {
                is Status.Failed -> Log.e("FL", it.exception.stackTraceToString())
                is Status.Loading -> Log.e("FL", "Loading")
                is Status.Success -> {
                    Log.i("FL", "${it.data}")
                }
            }
        }

        // ottieni i nomi delle compagnie selezionate dall'utente
        val selectedCompanies = mutableListOf<String>()
        val checkedChipsIds = binding.companyChips.checkedChipIds
        binding.companyChips.children.forEach { it as Chip
            if (it.id in checkedChipsIds)
                selectedCompanies.add(it.tag as String)
        }

        var isEconomySelected = binding.economyChip.isChecked
        var isBusinessSelected = binding.businessChip.isChecked
        var isFirstClassSelected = binding.firstClassChip.isChecked

        // se non è stato selezionato almeno un filtraggio, considera tutte le classi
        if (!isEconomySelected && !isBusinessSelected && !isFirstClassSelected) {
            isEconomySelected = true
            isBusinessSelected = true
            isFirstClassSelected = true
        }

        flightsViewModel.searchFlights(
            binding.departureAptText.text,
            binding.arrivalAptText.text,
            binding.goingDateText.text,
            binding.priceSlider.values[0].toDouble(),
            binding.priceSlider.values[1].toDouble(),
            binding.passengersText.text.toInt(),
            isEconomySelected,
            isBusinessSelected,
            isFirstClassSelected,
            selectedCompanies
        )
    }

    private fun setupAirportFields() {
        flightsViewModel.airportsFetchStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is Status.Failed -> showNetworkErrorDialog()
                is Status.Loading -> {
                    showOrHideProgressBar(false)
                    Log.d(TAG, "Fetching flights from database")
                }
                is Status.Success -> {
                    val airportDropdownItems = status.data.map { it.toString() }.toTypedArray()
                    departureAptField.setSimpleItems(airportDropdownItems)
                    arrivalAptField.setSimpleItems(airportDropdownItems)

                    showOrHideProgressBar(true)

                    // non è più necessario osservare la lista
                    flightsViewModel.airportsFetchStatus.removeObservers(this)
                }
            }
        }

        flightsViewModel.loadAirports()

        // inizializza il pulsante di scambio destinazioni
        binding.swapAirportsBttn.setOnClickListener {

            // scambia solo se sono state selezionate entrambe le destinazioni
            if (binding.departureAptText.text.isBlank() || binding.arrivalAptText.text.isBlank())
                return@setOnClickListener

            val temp = binding.departureAptText.editText?.text
            binding.departureAptText.editText?.text = binding.arrivalAptText.editText?.text
            binding.arrivalAptText.editText?.text = temp

            // evita che si apra il menu di selezione dell'arrivo in automatico
            binding.arrivalAptText.clearFocus()
        }
    }

    private fun setupCompanyFilters() {
        flightsViewModel.companyFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> showNetworkErrorDialog()
                is Status.Loading -> Log.d(TAG, "Fetching companies from database")
                is Status.Success -> binding.expandCompanyMenuBttn.setOnClickListener {
                    openOrCloseCompanyMenu()
                }
            }
        }

        flightsViewModel.loadCompanies()
    }

    private fun openOrCloseCompanyMenu() {
        val isMenuOpened = !binding.expandCompanyMenuBttn.isChecked

        if (isMenuOpened) {
            // elimina tutte le chip
            binding.companyChips.removeAllViews()

            // cambia l'icona del pulsante di apertura del menu
            binding.expandCompanyMenuBttn.closeIcon =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_arrow_l2r_24,
                    requireContext().theme
                )
        } else {
            val companies =
                (flightsViewModel.companyFetchStatus.value as Status.Success<List<Company>>).data

            // crea e aggiungi le chip al layout
            for (company in companies) {
                val chip = Chip(requireContext())
                chip.tag = company.name
                chip.text = company.name
                chip.chipIcon = company.logo.toDrawable(resources)
                chip.isCheckable = true
                binding.companyChips.addView(chip)
            }

            // cambia l'icona del pulsante di apertura del menu
            binding.expandCompanyMenuBttn.closeIcon =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_arrow_r2l_24,
                    requireContext().theme
                )
        }
    }

    private fun validateFields() =
        validateAirportFields() and
        validateDateFields() and
        validatePassengersCountField()

    private fun validateAirportFields(): Boolean {
        // se sono stati selezionati entrambi gli aeroporti
        if (validateDepartureAptField() and validateArrivalAptField()) {
            // se gli aeroporti coincidono, segnala un errore
            if (binding.departureAptText.text == binding.arrivalAptText.text) {
                binding.departureAptText.error = getString(R.string.dest_part_coincidono)
                binding.arrivalAptText.error = getString(R.string.dest_part_coincidono)

                return false
            } else { // se non coincidono, i campi sono validi
                binding.arrivalAptText.error = null
                binding.arrivalAptText.error = null

                return true
            }
        } else // se non sono stati selezionati entrambi gli aeroporti
            return false
    }

    private fun validateDateFields(): Boolean {
        // se le date di partenza e arrivo sono state inserite
        if (validateDepartureDateField() and validateArrivalDateField()) {
            val departureDate = binding.goingDateText.text.parseToDate()
            val arrivalDate = binding.returnDatetext.text.parseToDate()

            // se l'arrivo precede la partenza
            if (arrivalDate < departureDate)
                binding.returnDatetext.error = "Inserisci una data successiva alla partenza"
            else
                binding.returnDatetext.error = null

            return binding.returnDatetext.error == null
        } else // se non sono state inserite entrambe le date
            return false
    }

    private fun validateDepartureDateField(): Boolean {
        try {
            val date = binding.goingDateText.text.parseToDate()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            if (date < today)
                binding.goingDateText.error = getString(R.string.inserisci_data_futura)
            else
                binding.goingDateText.error = null

        } catch (e: IllegalArgumentException) {
            binding.goingDateText.error = getString(R.string.data_invalida)
        }

        return binding.goingDateText.error == null
    }

    private fun validateArrivalDateField(): Boolean {
        try {
            val date = binding.returnDatetext.text.parseToDate()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            if (date < today)
                binding.returnDatetext.error = getString(R.string.inserisci_data_futura)
            else
                binding.returnDatetext.error = null

        } catch (e: IllegalArgumentException) {
            binding.returnDatetext.error = getString(R.string.data_invalida)
        }

        return binding.returnDatetext.error == null
    }

    private fun validateDepartureAptField(): Boolean {
        if (binding.departureAptText.text.isEmpty())
            binding.departureAptText.error = getString(R.string.seleziona_aeroporto)
        else
            binding.departureAptText.error = null

        return binding.departureAptText.error == null
    }

    private fun validateArrivalAptField(): Boolean {
        if (binding.arrivalAptText.text.isEmpty())
            binding.arrivalAptText.error = getString(R.string.seleziona_aeroporto)
        else
            binding.arrivalAptText.error = null

        return binding.arrivalAptText.error == null
    }

    private fun validatePassengersCountField(): Boolean {
        try {
            val count = binding.passengersText.text.toInt()

            // il numero di passeggeri deve essere compreso tra 1 e 4
            if (count <= 0 || count >= 5)
                binding.passengersText.error = " "
            else
                binding.passengersText.error = null

        } catch (e: NumberFormatException) {
            // se il formato non è intero, errore
            binding.passengersText.error = " "
        }

        return binding.passengersText.error == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.fetchAptsProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

}