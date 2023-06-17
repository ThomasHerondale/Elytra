package tau.timentau.detau.elytra

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.MaterialAutoCompleteTextView
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

        flightsViewModel.airportsFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> showNetworkErrorDialog()
                is Status.Loading -> {
                    showOrHideProgressBar(false)
                    Log.d(TAG, "Fetching flights from database")
                }
                is Status.Success -> {
                    showOrHideProgressBar(true)
                    departureAptField.setSimpleItems(it.data.map { "${it.code} - ${it.name}" }.toTypedArray())
                }
            }
        }

        flightsViewModel.loadAirports()


        return binding.root
    }

    private fun showOrHideProgressBar(hide: Boolean) {
        binding.fetchAptsProgress.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

}