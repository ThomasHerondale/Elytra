package tau.timentau.detau.elytra.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentRecustomizeFlightBinding
import tau.timentau.detau.elytra.model.CARGO_LUGGAGE_PRICE
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.model.HAND_LUGGAGE_PRICE
import tau.timentau.detau.elytra.model.PassengerData
import tau.timentau.detau.elytra.navHostActivity

class RecustomizeFlightFragment : Fragment() {

    private lateinit var binding: FragmentRecustomizeFlightBinding
    private val navArgs: RecustomizeFlightFragmentArgs by navArgs()
    private val viewModel: FlightRecustomizationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRecustomizeFlightBinding.inflate(inflater)

        val flight = navArgs.flight

        setupFlightInfo(flight)

        val passengerIdx = navArgs.passengerIdx

        // gli indici partono da 0!
        val isLastPassenger = navArgs.passengerIdx + 1 == navArgs.passengersCount

        if (isLastPassenger) {
            binding.nextStepBttn.text = getString(R.string.procedi_al_pagamento)

            viewModel.addonPrice.observe(viewLifecycleOwner) {
                // abilita se c'è qualcosa da pagare -> è stato aggiunto qualche bagaglio
                binding.nextStepBttn.isEnabled = it != 0.0
            }
        } else
            binding.nextStepBttn.text = getString(R.string.prossimo_passegero)

        binding.passengerLabel.text = viewModel.getPassengerName(navArgs.passengerIdx)

        setupChoices(passengerIdx)
        setupPriceInfo(passengerIdx)

        binding.handLuggagePriceLabel.text =
            getString(R.string.prezzo_str, HAND_LUGGAGE_PRICE)

        binding.cargoLuggagePriceLabel.text =
            getString(R.string.prezzo_str, CARGO_LUGGAGE_PRICE)

        binding.handLuggageCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.addOrRemoveHandLuggage(passengerIdx, isChecked)
        }

        binding.cargoLuggageCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.addOrRemoveCargoLuggage(passengerIdx, isChecked)
        }

        binding.nextStepBttn.setOnClickListener {
            if (isLastPassenger) {
                navHostActivity.navigateTo(
                    RecustomizeFlightFragmentDirections.recustomizeToPayment(
                        viewModel.addonPrice.value!!.toFloat()
                    )
                )
            } else {
                navHostActivity.navigateTo(
                    RecustomizeFlightFragmentDirections.recustomizeFlightToRecustomizeFlight(
                        navArgs.flight,
                        navArgs.passengerIdx + 1,
                        navArgs.passengersCount
                    )
                )
            }
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
    private fun setupPriceInfo(passengerIdx: Int) {
        binding.customizedPriceLabel.text =
            getString(R.string.prezzo_str, viewModel.getCustomizedPrice(passengerIdx))

        // rispondi ai cambiamenti nei prezzi
        viewModel.passengerData.observe(
            viewLifecycleOwner,
            object : Observer<List<PassengerData>> {
                // non fare questioni sull'oggetto anonimo, vedi sopra ^^^
                override fun onChanged(value: List<PassengerData>) {
                    // ottieni il prezzo compreso di aggiunte (bagagli)
                    val customizedPrice = viewModel.getCustomizedPrice(passengerIdx)

                    binding.customizedPriceLabel.text =
                        getString(R.string.prezzo_str, customizedPrice)
                }
            }
        )
    }

    private fun setupChoices(passengerIdx: Int) {
        val passengerData = viewModel.getPassengerData(passengerIdx)

        if (passengerData.handLuggage) {
            binding.handLuggageCheck.isChecked = true
            binding.handLuggageCheck.isEnabled = false
        }
        if (passengerData.cargoLuggage) {
            binding.cargoLuggageCheck.isChecked = true
            binding.cargoLuggageCheck.isEnabled = false
        }
    }
}