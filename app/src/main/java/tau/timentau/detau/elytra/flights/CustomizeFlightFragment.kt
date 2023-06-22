package tau.timentau.detau.elytra.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentCustomizeFlightBinding
import tau.timentau.detau.elytra.model.Flight

private const val ARG_FLIGHT = "flight"
private const val ARG_IS_RETURN = "isReturn"

class CustomizeFlightFragment : Fragment() {

    private lateinit var binding: FragmentCustomizeFlightBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCustomizeFlightBinding.inflate(inflater)

        setupFlightInfo()

        return binding.root
    }

    private fun setupFlightInfo() {
        val flight = requireArguments().getParcelable<Flight>(ARG_FLIGHT)
            ?: throw IllegalArgumentException("Referred flight not provided.")

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

    companion object {
        @JvmStatic
        fun newInstance(flight: Flight, isReturn: Boolean): CustomizeFlightFragment {
            val args = Bundle().apply {
                putParcelable(ARG_FLIGHT, flight)
                putBoolean(ARG_IS_RETURN, isReturn)
            }

            return CustomizeFlightFragment().apply {
                arguments = args
            }
        }
    }
}