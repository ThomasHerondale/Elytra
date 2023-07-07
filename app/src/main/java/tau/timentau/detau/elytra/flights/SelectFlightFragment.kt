package tau.timentau.detau.elytra.flights

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
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentSelectFlightBinding
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.navHostActivity
import tau.timentau.detau.elytra.toReadableDateString
import tau.timentau.detau.elytra.toReadableTimeString

class SelectFlightFragment : Fragment() {

    private lateinit var binding: FragmentSelectFlightBinding
    private val args: SelectFlightFragmentArgs by navArgs()
    private val flightsViewModel: FlightsViewModel by activityViewModels()

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
        binding.voliRitorno.text = getString(R.string.volo_ritorno)

        val flight = flightsViewModel.selectedGoingFlight!! // don't worry, ne happy :D

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

        // ottieni la data da uno dei voli della lista. Che schifo è proprio poco elegante così
        binding.dataVolo.text = flights.first().date.toReadableDateString()

        binding.goingFlightsList.adapter =
            FlightsAdapter(flights, flightsViewModel.passengersCount) { flightSelected(it) }
    }

    private fun flightSelected(selectedFlight: Flight) {
        // se abbiamo selezionato il volo di ritorno, vai alla personalizzazione
        if (args.isReturn) {
            flightsViewModel.selectReturnFlight(selectedFlight)
            navHostActivity.navigateTo(
                SelectFlightFragmentDirections.selectGoingFlightToCustomizeTrip(
                    0,
                    isRoundTrip = true
                )
            )
        // altrimenti abbiamo selezionato il volo di andata
        } else {
            flightsViewModel.selectGoingFlight(selectedFlight)

            // se il viaggio è di sola andata, vai alla personalizzazione
            if (args.isPaymentNext) {
                navHostActivity.navigateTo(
                    SelectFlightFragmentDirections.selectGoingFlightToCustomizeTrip(
                        0,
                        isRoundTrip = false
                    )
                )
            }
            // altrimenti, vai alla selezione del volo di ritorno
            else
                navHostActivity.navigateTo(
                    SelectFlightFragmentDirections.selectGoingFlightToSelectReturnFlight(
                        isReturn = true, isPaymentNext = true
                    )
                )
        }
    }
}