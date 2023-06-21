package tau.timentau.detau.elytra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import tau.timentau.detau.elytra.database.FlightsAdapter
import tau.timentau.detau.elytra.databinding.FragmentSelectFlightBinding
import tau.timentau.detau.elytra.model.Flight

const val SELECT_GOING_FLIGHT_REQUEST_KEY = "select_going_flight"
const val SELECT_RETURN_FLIGHT_REQUEST_KEY = "select_return_flight"
const val FLIGHT_RESULT_KEY = "flight"

class SelectFlightFragment : Fragment() {

    private lateinit var binding: FragmentSelectFlightBinding
    private val args: SelectFlightFragmentArgs by navArgs()
    private val flightsViewModel: FlightsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* se il fragment è marcato come selettore del volo di ritorno, ma non è stato
         * fornito alcun volo di ritorno */
        if (args.isReturn && args.goingFlight == null)
            throw IllegalArgumentException("No going flight provided for return flight selection")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectFlightBinding.inflate(inflater)

        if (args.isReturn)
            setupGoingFlightCard()

        setupFlightsList()

        return binding.root
    }

    private fun setupGoingFlightCard() {
        binding.goingFlightGroup.visibility = View.VISIBLE

        val flight = args.goingFlight!! // don't worry, ne happy :D

        Glide
            .with(binding.companyLogoImg)
            .load(flight.company.logo)
            .transform(RoundedCorners(24))
            .into(binding.companyLogoImg)

        binding.apply {
            companyNameLabel.text = flight.company.name

            departureTimeLabel.text = flight.departureTime
            departureAptLabel.text = flight.departureApt.name

            arrivalTimeLabel.text = flight.arrivalTime
            arrivalAptLabel.text = flight.arrivalApt.name

            durationLabel.text = flight.duration
        }
    }

    private fun setupFlightsList() {
        binding.goingFlightsList.layoutManager = LinearLayoutManager(
            requireContext(), VERTICAL, false
        )

        binding.goingFlightsList.adapter =
            FlightsAdapter(flightsViewModel.goingFlightsList) {
                flightSelected(it)
                findNavController().popBackStack()
            }
    }

    private fun flightSelected(selectedFlight: Flight) {
        val requestKey =
            if (args.isReturn)
                SELECT_RETURN_FLIGHT_REQUEST_KEY
            else
                SELECT_GOING_FLIGHT_REQUEST_KEY

        val result = Bundle().apply {
            putParcelable(FLIGHT_RESULT_KEY, selectedFlight)
        }

        setFragmentResult(requestKey, result)
    }
}