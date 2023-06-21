package tau.timentau.detau.elytra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import tau.timentau.detau.elytra.database.FlightsAdapter
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentSelectGoingFlightBinding
import tau.timentau.detau.elytra.model.Flight

class SelectFlightFragment : Fragment() {

    private lateinit var binding: FragmentSelectGoingFlightBinding
    private val flightsViewModel: FlightsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectGoingFlightBinding.inflate(inflater)

        setupFlightsList()

        return binding.root
    }

    private fun setupFlightsList() {
        binding.goingFlightsList.layoutManager = LinearLayoutManager(
            requireContext(), VERTICAL, false
        )
        flightsViewModel.goingFlightsFetchStatus.observe(viewLifecycleOwner) {
            if (it is Status.Success<List<Flight>>)
                binding.goingFlightsList.adapter = FlightsAdapter(it.data)
        }
    }
}