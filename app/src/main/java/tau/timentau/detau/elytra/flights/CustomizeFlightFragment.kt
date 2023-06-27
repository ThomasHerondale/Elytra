package tau.timentau.detau.elytra.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tau.timentau.detau.elytra.databinding.FragmentCustomizeFlightBinding

class CustomizeFlightFragment : Fragment() {

    private lateinit var binding: FragmentCustomizeFlightBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCustomizeFlightBinding.inflate(inflater)

        return binding.root
    }

}