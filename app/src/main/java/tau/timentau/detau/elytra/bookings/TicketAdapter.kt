package tau.timentau.detau.elytra.bookings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.TicketViewHolderBinding
import tau.timentau.detau.elytra.model.Ticket
import tau.timentau.detau.elytra.toReadableDateString
import tau.timentau.detau.elytra.toReadableTimeString

class TicketAdapter(
    private val onCustomizeClicked: (Ticket) -> Unit,
) : ListAdapter<Ticket, TicketAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.ticket_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = getItem(position)

        holder.binding.apply {
            departAptCodeLabel.text = ticket.flight.departureApt.code
            departAptLabel.text = ticket.flight.departureApt.name

            arrivalAptCodeLabel.text = ticket.flight.arrivalApt.code
            arrivalAptLabel.text = ticket.flight.arrivalApt.name

            dateLabel.text = ticket.flight.date.toReadableDateString()

            gateClosingLabel.text = ticket.flight.gateClosingTime.toReadableTimeString()
            takeOffLabel.text = ticket.flight.departureTime.toReadableTimeString()

            ticketDateLabel.text = ticket.makingDate.toReadableDateString()
        }

        holder.binding.customizeBttn.setOnClickListener { onCustomizeClicked(ticket) }

        holder.binding.customizeBttn.isEnabled = ticket.isRecustomizable
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Ticket>() {
            override fun areItemsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItem: Ticket, newItem: Ticket): Boolean {
                return true
            }

        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = TicketViewHolderBinding.bind(itemView)
    }
}