package tau.timentau.detau.elytra.accomodations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import tau.timentau.detau.elytra.databinding.FragmentSelectAccomodationBinding
import tau.timentau.detau.elytra.navHostActivity
import tau.timentau.detau.elytra.setContainerActivityTitle

class SelectAccomodationFragment : Fragment() {

    private lateinit var binding: FragmentSelectAccomodationBinding
    private val navArgs: SelectAccomodationFragmentArgs by navArgs()
    private val accomodationsViewModel: AccomodationsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSelectAccomodationBinding.inflate(inflater)

        setContainerActivityTitle("Seleziona alloggi")

        setupAccomodationsList()

        return binding.root
    }

    private fun setupAccomodationsList() {
        binding.accomodationsList.layoutManager = LinearLayoutManager(
            requireContext(), VERTICAL, false
        )

        binding.accomodationsList.adapter = AccomodationsAdapter(
            accomodationsViewModel.accomodations,
            navArgs.hostCount,
            navArgs.nightCount
        ) {
            accomodationsViewModel.selectAccomodation(it)
            navHostActivity.navigateTo(
                SelectAccomodationFragmentDirections.selectAccomodationToPayment(
                    navArgs.hostCount
                )
            )
        }
    }
}