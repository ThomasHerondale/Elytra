package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentFlightsBinding

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

        binding.searchFlightsBttn.setOnClickListener {
        }

        return binding.root
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
            val departureDate = binding.departureDateText.text.parseToDate()
            val arrivalDate = binding.arrivalDateText.text.parseToDate()

            // se l'arrivo precede la partenza
            if (arrivalDate < departureDate)
                binding.arrivalDateText.error = "Inserisci una data successiva alla partenza"
            else
                binding.arrivalDateText.error = null

            return binding.arrivalDateText.error == null
        } else // se non sono state inserite entrambe le date
            return false
    }

    private fun validateDepartureDateField(): Boolean {
        try {
            val date = binding.departureDateText.text.parseToDate()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            if (date < today)
                binding.departureDateText.error = getString(R.string.inserisci_data_futura)
            else
                binding.departureDateText.error = null

        } catch (e: IllegalArgumentException) {
            binding.departureDateText.error = getString(R.string.data_invalida)
        }

        return binding.departureDateText.error == null
    }

    private fun validateArrivalDateField(): Boolean {
        try {
            val date = binding.arrivalDateText.text.parseToDate()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            if (date < today)
                binding.arrivalDateText.error = getString(R.string.inserisci_data_futura)
            else
                binding.arrivalDateText.error = null

        } catch (e: IllegalArgumentException) {
            binding.arrivalDateText.error = getString(R.string.data_invalida)
        }

        return binding.arrivalDateText.error == null
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
        if (binding.passengersText.text.isBlank())
            binding.passengersText.error = " "
        else
            binding.passengersText.error = null

        return binding.passengersText.error == null
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.fetchAptsProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

}