package tau.timentau.detau.elytra.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FragmentPaymentAccomodationsBinding
import tau.timentau.detau.elytra.flights.SelectPaymentMethodDialog
import tau.timentau.detau.elytra.flights.SelectPaymentMethodDialog.PaymentSubject.CUSTOMIZATION

private const val SERVICE_FEES = 4.0

class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentAccomodationsBinding
    private val navArgs: PaymentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPaymentAccomodationsBinding.inflate(inflater)

        val totalPrice = navArgs.price

        binding.totalText.text = getString(R.string.prezzo_str, totalPrice)
        binding.feesText.text = getString(R.string.prezzo_str, SERVICE_FEES)

        binding.paymentText.text =
            getString(R.string.prezzo_str, totalPrice + SERVICE_FEES)

        binding.selectPaymentMethodBttn.setOnClickListener {
            SelectPaymentMethodDialog.newInstance(CUSTOMIZATION)
                .show(parentFragmentManager, "selectPaymentMethod")
        }

        return binding.root
    }
}