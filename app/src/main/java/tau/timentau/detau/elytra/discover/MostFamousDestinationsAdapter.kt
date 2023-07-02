package tau.timentau.detau.elytra.discover

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.MostFamousDestinationViewHolderBinding
import tau.timentau.detau.elytra.model.City

class MostFamousDestinationsAdapter(
    private val destinations: List<Pair<City, Bitmap>>,
) : RecyclerView.Adapter<MostFamousDestinationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.most_famous_destination_view_holder, parent, false)
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
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.binding.cityImg)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = MostFamousDestinationViewHolderBinding.bind(itemView)
    }
}