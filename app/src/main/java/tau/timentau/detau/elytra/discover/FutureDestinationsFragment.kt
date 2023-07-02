package tau.timentau.detau.elytra.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import tau.timentau.detau.elytra.databinding.FragmentFutureDestinationsBinding
import tau.timentau.detau.elytra.navHostActivity

class FutureDestinationsFragment : Fragment() {

    private lateinit var binding: FragmentFutureDestinationsBinding
    private val viewModel: DiscoverViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFutureDestinationsBinding.inflate(inflater)

        setupList()

        return binding.root
    }

    private fun setupList() {
        binding.destinationsList.layoutManager = LinearLayoutManager(
            requireContext(), HORIZONTAL, false
        )

        binding.destinationsList.adapter = DestinationsAdapter(viewModel.futureDestinations) {
            // on accomodations clicked
            navHostActivity.navigateTo(DiscoverFragmentDirections.discoverToAccomodations(it))
        }
    }
}