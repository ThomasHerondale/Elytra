package tau.timentau.detau.elytra.discover

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.DestinationViewHolderBinding
import tau.timentau.detau.elytra.model.City

class DestinationsAdapter(
    private val destinations: List<Pair<City, Bitmap>>,
    private val onFlightsClicked: ((City) -> Unit)? = null,
    private val onAccomodationsClicked: (City) -> Unit,
) : RecyclerView.Adapter<DestinationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.destination_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = destinations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (city, image) = destinations[position]

        Glide
            .with(holder.itemView)
            .load(image)
            .transform(RoundedCorners(36))
            .into(holder.binding.cityImg)

        holder.binding.cityName.text = city.name

        if (onFlightsClicked != null) {
            holder.binding.toFlightsBttn.setOnClickListener { onFlightsClicked.invoke(city) }
            holder.binding.toFlightsBttn.visibility = View.VISIBLE
        } else
            holder.binding.toFlightsBttn.visibility = View.GONE

        holder.binding.toAccomodationsBttn.setOnClickListener { onAccomodationsClicked(city) }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.binding.cityImg)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DestinationViewHolderBinding.bind(itemView)
    }
}