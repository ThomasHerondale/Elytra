package tau.timentau.detau.elytra.firstAccess

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.AvatarChoiceViewHolderBinding

private const val IS_SELECTED_KEY = "isSelected"

class AvatarChoiceAdapter(
    private val onItemSelected: (Int) -> Unit
) : ListAdapter<AvatarChoice, AvatarChoiceAdapter.ViewHolder>(diffUtil) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.avatar_choice_view_holder, parent, false)
       return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avatarChoice = getItem(position)

        Glide
            .with(holder.itemView)
            .load(avatarChoice.avatar)
            .into(holder.binding.avatarImg)

        holder.binding.avatarChoiceCard.isSelected = holder.adapterPosition == selectedPosition

        holder.binding.avatarChoiceCard.setOnClickListener {
            // se l'avatar è già selezionato non fare nulla
            if (!it.isSelected) {
                // deseleziona l'avatar precedentemente selezionato, se esiste
                if (selectedPosition != RecyclerView.NO_POSITION)
                    selectOrUnselectItem(selectedPosition, false)

                // aggiorna la selezione
                selectedPosition = holder.adapterPosition

                // seleziona il nuovo avatar
                selectOrUnselectItem(selectedPosition, true)

                // notifica al dialog esterno della selezione
                onItemSelected(selectedPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position) // bind completo
        else {
            val isSelected = (payloads[0] as Bundle).getBoolean(IS_SELECTED_KEY)
            holder.binding.avatarChoiceCard.isSelected = isSelected
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        Glide
            .with(holder.itemView)
            .clear(holder.itemView)
    }

    private fun selectOrUnselectItem(position: Int, select: Boolean) {
        getItem(position).isSelected = select

        // modifica solo il bordo, non eseguire rebind completi
        val bundle = Bundle().apply {
            putBoolean(IS_SELECTED_KEY, select)
        }
        notifyItemChanged(position, bundle)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AvatarChoice>() {
            override fun areItemsTheSame(oldItem: AvatarChoice, newItem: AvatarChoice): Boolean {
                return false
            }

            override fun areContentsTheSame(oldItem: AvatarChoice, newItem: AvatarChoice): Boolean {
                return true
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = AvatarChoiceViewHolderBinding.bind(itemView)
    }
}

class AvatarChoice(
    val avatar: Bitmap,
    var isSelected: Boolean = false
) {
    override fun toString(): String {
        return isSelected.toString()
    }
}