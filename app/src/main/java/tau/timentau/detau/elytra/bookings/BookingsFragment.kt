package tau.timentau.detau.elytra.bookings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import tau.timentau.detau.elytra.database.Status
import tau.timentau.detau.elytra.databinding.FragmentBookingsBinding
import tau.timentau.detau.elytra.navHostActivity

private const val TAG = "BOOKINGS"

class BookingsFragment : Fragment() {

    private lateinit var binding: FragmentBookingsBinding
    private val viewModel: BookingsViewModel by viewModels()
    private val customizationViewModel: FlightRecustomizationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBookingsBinding.inflate(inflater)

        setupTicketList()
        setupBookingList()

        return binding.root
    }

    private fun setupTicketList() {
        binding.ticketList.layoutManager = LinearLayoutManager(
            requireContext(), HORIZONTAL, false
        )

        val adapter = TicketAdapter {
            customizationViewModel.initializePassengerData(it.passengersInfo)
            customizationViewModel.setTicket(it)
            navHostActivity.navigateTo(
                BookingsFragmentDirections.bookingsToRecustomizeFlight(
                    it.flight, 0, it.passengersCount
                )
            )
        }

        binding.ticketList.adapter = adapter

        viewModel.ticketsFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> {
                    println(it.exception.stackTraceToString())
                }

                is Status.Loading -> {}
                is Status.Success -> {
                    adapter.submitList(it.data)
                }
            }
        }

        viewModel.loadTickets()
    }

    private fun setupBookingList() {
        binding.bookingList.layoutManager = LinearLayoutManager(
            requireContext(), HORIZONTAL, false
        )

        val adapter = BookingAdapter()

        binding.bookingList.adapter = adapter

        viewModel.bookingsFetchStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Status.Failed -> {
                    Log.e(TAG, it.exception.stackTraceToString())
                }

                is Status.Loading -> {}
                is Status.Success -> adapter.submitList(it.data)
            }
        }

        viewModel.loadBookings()
    }

}