package tau.timentau.detau.elytra.bookings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentBookingsBinding

class BookingsFragment : Fragment() {

    private lateinit var binding: FragmentBookingsBinding
    private val viewModel: BookingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBookingsBinding.inflate(inflater)

        setupTicketList()

        return binding.root
    }

    private fun setupTicketList() {
        binding.ticketList.layoutManager = LinearLayoutManager(
            requireContext(), HORIZONTAL, false
        )

        val adapter = TicketAdapter() {

        }
        binding.ticketList.adapter = adapter

        viewModel.ticketsFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> {
                    println(it.exception.stackTraceToString())
                }

                is Status.Loading -> {}
                is Status.Success -> {
                    println(it.data)
                    adapter.submitList(it.data)
                }
            }
        }

        viewModel.loadTickets()
    }


}