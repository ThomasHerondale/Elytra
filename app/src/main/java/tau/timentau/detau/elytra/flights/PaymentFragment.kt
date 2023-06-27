package tau.timentau.detau.elytra.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentPaymentFlightsBinding

private const val SERVICE_FEES = 16.70

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentFlightsBinding
    private val viewModel: TripCustomizationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPaymentFlightsBinding.inflate(inflater)

        binding.goingFlightTotalText.text =
            getString(R.string.prezzo_str, viewModel.getFlightTotalPrice(false))
        binding.returnFlightTotalText.text =
            getString(R.string.prezzo_str, viewModel.getFlightTotalPrice(true))

        val totalPrice = viewModel.getTripTotalPrice()

        binding.totalText.text = getString(R.string.prezzo_str, totalPrice)
        binding.feesText.text = getString(R.string.prezzo_str, SERVICE_FEES)

        binding.paymentText.text =
            getString(R.string.prezzo_str, totalPrice + SERVICE_FEES)

        binding.selectPaymentMethodBttn.setOnClickListener {
            SelectPaymentMethodDialog().show(parentFragmentManager, "selectPaymentMethod")
        }

        return binding.root
    }
}