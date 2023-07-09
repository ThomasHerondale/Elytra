package tau.timentau.detau.elytra.flights

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.FlightViewHolderBinding
import tau.timentau.detau.elytra.model.Flight
import tau.timentau.detau.elytra.toReadableTimeString

class FlightsAdapter(
    private val flights: List<Flight>,
    private val passengersCount: Int,
    private val onFlightSelected: (Flight) -> Unit)
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
            .transform(RoundedCorners(8))
            .into(holder.binding.companyLogoImg)

        holder.binding.apply {
            companyNameLabel.text = flight.company.name

            departureTimeLabel.text = flight.departureTime.toReadableTimeString()
            departureAptLabel.text = flight.departureApt.name

            arrivalTimeLabel.text = flight.arrivalTime.toReadableTimeString()
            arrivalAptLabel.text = flight.arrivalApt.name

            durationLabel.text = flight.duration.toReadableTimeString()

            priceLabel.text = holder.itemView.context.
                getString(R.string.prezzo_str, flight.price * passengersCount)

            passengersCountLabel.text = holder.itemView.context
                .getString(R.string.passegeri_str, passengersCount)

            serviceClassLabel.text = flight.serviceClass.stringValue
        }

        holder.binding.flightViewHolderCard.setOnClickListener {
            onFlightSelected(flight)
            Log.d("RESULT", "Called")
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.binding.companyLogoImg)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = FlightViewHolderBinding.bind(itemView)
    }
}