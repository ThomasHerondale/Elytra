package tau.timentau.detau.elytra.bookings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.BookingViewHolderBinding
import tau.timentau.detau.elytra.model.Booking

class BookingAdapter : ListAdapter<Booking, BookingAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.booking_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = getItem(position)

        Glide
            .with(holder.itemView)
            .load(booking.accomodation.image)
            .into(holder.binding.accImg)

        holder.binding.apply {
            accName.text = booking.accomodation.name

            accCity.text = booking.accomodation.city
            accAddress.text = booking.accomodation.address

            bookingDates.text = holder.itemView.context.getString(
                R.string.range_date_str, booking.checkInDate, booking.checkOutDate
            )

            bookingPrice.text = holder.itemView.context.getString(
                R.string.prezzo_str, booking.price
            )
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
                return true
            }

        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.binding.accImg)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = BookingViewHolderBinding.bind(itemView)
    }
}