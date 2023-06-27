package tau.timentau.detau.elytra.accomodations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.AccomodationViewHolderBinding
import tau.timentau.detau.elytra.model.Accomodation

class AccomodationsAdapter(
    private val accomodations: List<Accomodation>,
    private val hostCount: Int,
    private val nightCount: Int,
    private val onItemSelectedListener: (Accomodation) -> Unit
) : Adapter<AccomodationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.accomodation_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = accomodations.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val accomodation = accomodations[position]

        Glide
            .with(holder.itemView)
            .load(accomodation.image)
            .into(holder.binding.accImg)

        holder.binding.apply {
            accNameText.text = accomodation.name
            ratingScoreText.text = accomodation.rating.toString()
            accCategoryText.text = accomodation.category.stringValue
            accDescriptionText.text = accomodation.description
            accCityText.text = accomodation.city
            accAddressText.text = accomodation.description
            accPriceText.text =
                holder.itemView.context.getString(R.string.prezzo_str, accomodation.price)
            accCountsText.text = assembleCountString()
        }

        holder.binding.accomodationCard.setOnClickListener { onItemSelectedListener(accomodation) }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.binding.accImg)
    }

    private fun assembleCountString(): String {
        val hostLabel = if (hostCount == 1) "persona" else "persone"
        val nightLabel = if (nightCount == 1) "notte" else "notti"

        return "$nightCount $nightLabel, $hostCount $hostLabel"
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AccomodationViewHolderBinding.bind(itemView)
    }
}