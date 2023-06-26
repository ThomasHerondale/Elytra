package tau.timentau.detau.elytra.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentCustomizeFlightBinding
import tau.timentau.detau.elytra.model.CARGO_LUGGAGE_PRICE
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.model.HAND_LUGGAGE_PRICE
import tau.timentau.detau.elytra.model.PassengerData

private const val ARG_FLIGHT = "flight"
private const val ARG_IS_RETURN = "isReturn"
private const val ARG_PASSENGER_INDEX = "passengerIdx"

class CustomizeFlightFragment : Fragment() {

    private lateinit var binding: FragmentCustomizeFlightBinding
    private val viewModel: TripCustomizationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCustomizeFlightBinding.inflate(inflater)

        val isReturn = arguments?.getBoolean(ARG_IS_RETURN) ?:
            throw IllegalStateException("Could not determine if flight is for return")

        val flight = requireArguments().getParcelable<Flight>(ARG_FLIGHT)
            ?: throw IllegalArgumentException("Referred flight not provided.")

        binding.flightLabel.text =
            if (isReturn)
                getString(R.string.volo_di_ritorno)
            else
                getString(R.string.volo_di_andata)

        setupFlightInfo(flight)

        val passengerIdx = arguments?.getInt(ARG_PASSENGER_INDEX) ?:
            throw IllegalStateException("Passenger index has not been provided")

        if (viewModel.isPassengerDataInitialized()) {
            viewModel.updateFlightData(flight, isReturn)
            setupChoices(passengerIdx, isReturn)
        }

        setupPriceInfo(passengerIdx, isReturn)

        binding.handLuggagePriceLabel.text =
            getString(R.string.prezzo_str, HAND_LUGGAGE_PRICE)

        binding.cargoLuggagePriceLabel.text =
            getString(R.string.prezzo_str, CARGO_LUGGAGE_PRICE)

        binding.handLuggageCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.addOrRemoveHandLuggage(passengerIdx, isChecked, isReturn)
        }

        binding.cargoLuggageCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.addOrRemoveCargoLuggage(passengerIdx, isChecked, isReturn)
        }

            return binding.root
    }

    private fun setupFlightInfo(flight: Flight) {

        binding.apply {
            Glide
                .with(companyLogoImg)
                .load(flight.company.logo)
                .into(companyLogoImg)

            companyNameLabel.text = flight.company.name

            departureTimeLabel.text = flight.departureTime
            departureAptLabel.text = flight.departureApt.name

            arrivalTimeLabel.text = flight.arrivalTime
            arrivalAptLabel.text = flight.arrivalApt.name

            durationLabel.text = flight.duration

            serviceClassLabel.text = flight.serviceClass.stringValue
            priceLabel.text = getString(R.string.prezzo_str, flight.price)
        }
    }

    // no android studio, non è vero che conviene trasformarla in lambda, altrimenti il compilatore
    // usa un solo oggetto lambda e android si lamenterà del fatto che l'observer è stato già
    // settato
    @Suppress("ObjectLiteralToLambda")
    private fun setupPriceInfo(passengerIdx: Int, isReturn: Boolean) {
        binding.customizedPriceLabel.text =
            getString(R.string.prezzo_str, viewModel.getCustomizedPrice(passengerIdx, isReturn))

        // rispondi ai cambiamenti nei prezzi
        viewModel.passengerData.observe(
            viewLifecycleOwner,
            object : Observer<List<PassengerData>> {
                // non fare questioni sull'oggetto anonimo, vedi sopra ^^^
                override fun onChanged(value: List<PassengerData>) {
                    // ottieni il prezzo compreso di aggiunte (bagagli)
                    val customizedPrice = viewModel.getCustomizedPrice(passengerIdx, isReturn)

                    binding.customizedPriceLabel.text =
                        getString(R.string.prezzo_str, customizedPrice)
                }
            }
        )
    }

    private fun setupChoices(passengerIdx: Int, isReturn: Boolean) {
        val passengerData = viewModel.getPassengerData(passengerIdx, isReturn)

        binding.handLuggageCheck.isChecked = passengerData.handLuggage
        binding.cargoLuggageCheck.isChecked = passengerData.cargoLuggage
    }

    companion object {
        @JvmStatic
        fun newInstance(
            flight: Flight,
            passengerIdx: Int,
            isReturn: Boolean
        ): CustomizeFlightFragment {

            val args = Bundle().apply {
                putParcelable(ARG_FLIGHT, flight)
                putBoolean(ARG_IS_RETURN, isReturn)
                putInt(ARG_PASSENGER_INDEX, passengerIdx)
            }

            return CustomizeFlightFragment().apply {
                arguments = args
            }
        }
    }
}