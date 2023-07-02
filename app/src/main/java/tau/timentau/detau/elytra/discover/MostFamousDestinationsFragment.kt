package tau.timentau.detau.elytra.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import tau.timentau.detau.elytra.databinding.FragmentMostFamousDestinationsBinding
import tau.timentau.detau.elytra.navHostActivity

class MostFamousDestinationsFragment : Fragment() {

    private lateinit var binding: FragmentMostFamousDestinationsBinding
    private val viewModel: DiscoverViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMostFamousDestinationsBinding.inflate(inflater)

        setupList()

        return binding.root
    }

    private fun setupList() {
        binding.mostFamousList.layoutManager = LinearLayoutManager(
            requireContext(), HORIZONTAL, false
        )

        binding.mostFamousList.adapter =
            DestinationsAdapter(
                viewModel.mostFamousDestinations,
                onFlightsClicked = {
                    navHostActivity.navigateTo(
                        DiscoverFragmentDirections.discoverToFlights(it)
                    )
                },
                onAccomodationsClicked = {
                    navHostActivity.navigateTo(
                        DiscoverFragmentDirections.discoverToAccomodations(it)
                    )
                }
            )
    }

}