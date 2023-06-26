package tau.timentau.detau.elytra.flights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tau.timentau.detau.elytra.R
import tau.timentau.detau.elytra.databinding.PaymentMethodViewHolderBinding
import tau.timentau.detau.elytra.model.PaymentMethod

private const val IS_SELECTED_KEY = "isSelected"

class PaymentMethodAdapter(
    private val onItemSelected: (Int) -> Unit
) : ListAdapter<PaymentMethodChoice, PaymentMethodAdapter.ViewHolder>(diffCallback) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_method_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val method = getItem(position)

        holder.binding.paymentMethodCard.isSelected = holder.adapterPosition == selectedPosition

        holder.binding.removeCardBttn.visibility = View.GONE

        Glide
            .with(holder.itemView)
            .load(method.paymentMethod.circuit.logo)
            .into(holder.binding.circuitLogo)

        holder.binding.cardNumberLabel.text = method.paymentMethod.number
        holder.binding.ownerNameLabel.text = method.paymentMethod.ownerFullname

        holder.binding.paymentMethodCard.setOnClickListener {
            if (!it.isSelected) {
                if (selectedPosition != RecyclerView.NO_POSITION)
                    selectOrUnselectItem(selectedPosition, false)

                selectedPosition = holder.adapterPosition

                selectOrUnselectItem(selectedPosition, true)

                onItemSelected(selectedPosition)
            }
        }

    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<PaymentMethodChoice>() {
            override fun areItemsTheSame(
                oldItem: PaymentMethodChoice,
                newItem: PaymentMethodChoice,
            ): Boolean {
                return oldItem.paymentMethod.number == newItem.paymentMethod.number
            }

            override fun areContentsTheSame(
                oldItem: PaymentMethodChoice,
                newItem: PaymentMethodChoice,
            ): Boolean {
                return true
            }
        }
    }

    private fun selectOrUnselectItem(position: Int, select: Boolean) {
        getItem(position).isSelected = select

        // modifica solo il bordo, non eseguire rebind completi
        val bundle = Bundle().apply {
            putBoolean(IS_SELECTED_KEY, select)
        }
        notifyItemChanged(position, bundle)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PaymentMethodViewHolderBinding.bind(itemView)
    }

}

class PaymentMethodChoice(
    val paymentMethod: PaymentMethod,
    var isSelected: Boolean = false
)