package tau.timentau.detau.elytra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import tau.timentau.detau.elytra.database.FlightsAdapter
import tau.timentau.detau.elytra.databinding.FragmentSelectFlightBinding
import tau.timentau.detau.elytra.model.Flight

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

        binding.editGoingFlightBttn.setOnClickListener {
            // invalida la precedente selezione del volo di andata
            flightsViewModel.unselectGoingFlight()
            // torna alla schermata di scelta del volo di andata
            navHostActivity.popBackStack()
        }

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

            departureTimeLabel.text = flight.departureTime.toReadableTimeString()
            departureAptLabel.text = flight.departureApt.name

            arrivalTimeLabel.text = flight.arrivalTime.toReadableTimeString()
            arrivalAptLabel.text = flight.arrivalApt.name

            durationLabel.text = flight.duration.toReadableTimeString()

            serviceClassLabel.text = flight.serviceClass.stringValue
            priceLabel.text = getString(R.string.prezzo_str, flight.price)
            passengersCountLabel.text =
                getString(R.string.passegeri_str, flightsViewModel.passengersCount)
        }
    }

    private fun setupFlightsList() {
        binding.goingFlightsList.layoutManager = LinearLayoutManager(
            requireContext(), VERTICAL, false
        )

        val flights =
            if (args.isReturn)
                flightsViewModel.returnFlightsList
            else
                flightsViewModel.goingFlightsList

        binding.goingFlightsList.adapter =
            FlightsAdapter(flights, flightsViewModel.passengersCount) { flightSelected(it) }
    }

    private fun flightSelected(selectedFlight: Flight) {
        if (args.isReturn) {
            flightsViewModel.selectReturnFlight(selectedFlight)
            TODO()
            // TODO naviga al pagamento
        } else {
            flightsViewModel.selectGoingFlight(selectedFlight)

            if (args.isPaymentNext) {
                // TODO naviga al pagamento
                TODO()
            }
            else
                navHostActivity.navigateTo(
                    SelectFlightFragmentDirections.selectGoingFlightToSelectReturnFlight(
                        isReturn = true, goingFlight = selectedFlight, isPaymentNext = true
                    )
                )
        }
    }
}