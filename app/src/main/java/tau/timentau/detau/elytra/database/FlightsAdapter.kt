package tau.timentau.detau.elytra.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FlightViewHolderBinding
import tau.timentau.detau.elytra.model.Flight

class FlightsAdapter(private val flights: List<Flight>)
    : RecyclerView.Adapter<FlightsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.flight_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = flights.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flight = flights[position]

        Glide
            .with(holder.itemView)
            .load(flight.company.logo)
            .transform(RoundedCorners(24))
            .into(holder.binding.companyLogoImg)

        holder.binding.apply {
            companyNameLabel.text = flight.company.name

            departureTimeLabel.text = flight.departureTime
            departureAptLabel.text = flight.departureApt.name

            arrivalTimeLabel.text = flight.arrivalTime
            arrivalAptLabel.text = flight.arrivalApt.name

            durationLabel.text = flight.duration
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = FlightViewHolderBinding.bind(itemView)
    }
}