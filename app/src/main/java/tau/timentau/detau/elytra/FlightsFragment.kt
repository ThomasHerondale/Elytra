package tau.timentau.detau.elytra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tau.timentau.detau.elytra.databinding.FragmentFlightsBinding

class FlightsFragment : Fragment() {

    private lateinit var binding: FragmentFlightsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFlightsBinding.inflate(layoutInflater)

        return binding.root
    }

}