package tau.timentau.detau.elytra.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentCustomizeTripBinding
import tau.timentau.detau.elytra.navHostActivity
import tau.timentau.detau.elytra.text

class CustomizeTripFragment : Fragment() {

    private lateinit var binding: FragmentCustomizeTripBinding
    private val navArgs: CustomizeTripFragmentArgs by navArgs()
    private val flightsViewModel: FlightsViewModel by activityViewModels()
    private val tripCustomizationViewModel: TripCustomizationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCustomizeTripBinding.inflate(inflater)

        /* se questo fragment fa riferimento al primo passegero, inizializza le preferenze sui
        passegeri */
        if (!tripCustomizationViewModel.isPassengerDataInitialized()) {
            if (navArgs.isRoundTrip)
                tripCustomizationViewModel.initializePassengerDataForRoundTrip(
                    flightsViewModel.passengersCount,
                    flightsViewModel.selectedGoingFlight,
                    flightsViewModel.selectedReturnFlight
                )
            else
                tripCustomizationViewModel.initializePassengerData(
                    flightsViewModel.passengersCount,
                    flightsViewModel.selectedGoingFlight
                )
        }

        binding.passengerText.editText?.setText(
            tripCustomizationViewModel.getPassengerName(navArgs.passengerIndex)
        )

        setupGoingFlightSection()

        if (navArgs.isRoundTrip)
            setupReturnFlightSection()

        binding.nextStepBttn.setOnClickListener {
            // Se il campo non è stato svuotato, imposta il nome del passegero
            if (binding.passengerText.text.isNotBlank()) {
                tripCustomizationViewModel.setPassengerName(
                    navArgs.passengerIndex, binding.passengerText.text
                )
            }

            // gli indici partono da 0!
            val isLastPassenger = navArgs.passengerIndex + 1 == flightsViewModel.passengersCount

            if (isLastPassenger) {
                TODO() // al riepilogo
            } else {
                navHostActivity.navigateTo(
                    CustomizeTripFragmentDirections.customizeTripToCustomizeTrip(
                        navArgs.passengerIndex + 1,
                        isRoundTrip = navArgs.isRoundTrip
                    )
                )
            }
        }

        return binding.root
    }

    private fun setupGoingFlightSection() {
        val fragment = CustomizeFlightFragment.newInstance(
            flightsViewModel.selectedGoingFlight,
            navArgs.passengerIndex,
            isReturn = false
        )

        parentFragmentManager
            .beginTransaction()
            .add(R.id.goingFlightFragContainer, fragment, "customizeGoingFlight")
            .commit()
    }

    private fun setupReturnFlightSection() {
        val fragment = CustomizeFlightFragment.newInstance(
            flightsViewModel.selectedReturnFlight,
            navArgs.passengerIndex,
            isReturn = true
        )

        parentFragmentManager
            .beginTransaction()
            .add(R.id.returnFlightFragContainer, fragment, "customizeReturnFlight")
            .commit()
    }
}