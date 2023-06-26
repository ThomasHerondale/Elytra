package tau.timentau.detau.elytra.accomodations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentPaymentBinding
import tau.timentau.detau.elytra.flights.SelectPaymentMethodDialog

private const val SERVICE_FEES = 16.70

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private val navArgs: PaymentFragmentArgs by navArgs()
    private val viewModel: AccomodationsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPaymentBinding.inflate(inflater)

        if (navArgs.hostCount == -1)
            throw IllegalArgumentException("Host count not provided to payment")

        val totalPrice = viewModel.getTotalPrice(navArgs.hostCount)

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