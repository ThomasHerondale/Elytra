package tau.timentau.detau.elytra

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.databinding.AvatarChoiceViewHolderBinding

class AvatarChoiceAdapter : ListAdapter<Bitmap, AvatarChoiceAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.avatar_choice_view_holder, parent, false)
       return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val image = getItem(position)

        Glide
            .with(holder.itemView)
            .load(image)
            .into(holder.binding.avatarImg)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.itemView)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Bitmap>() {
            override fun areItemsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: Bitmap, newItem: Bitmap): Boolean {
                return true
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AvatarChoiceViewHolderBinding.bind(itemView)
    }
}