package tau.timentau.detau.elytra.profile

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

class PaymentMethodAdapter(
    private val onRemoveClicked: (String) -> Unit
) :
    ListAdapter<PaymentMethod, PaymentMethodAdapter.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_method_view_holder, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val method = getItem(position)

        Glide
            .with(holder.itemView)
            .load(method.circuit.logo)
            .into(holder.binding.circuitLogo)

        holder.binding.cardNumberLabel.text = method.number
        holder.binding.ownerNameLabel.text = method.ownerFullname

        holder.binding.removeCardBttn.setOnClickListener { onRemoveClicked(method.number) }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<PaymentMethod>() {
            override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
                return oldItem.number == newItem.number
            }

            override fun areContentsTheSame(
                oldItem: PaymentMethod,
                newItem: PaymentMethod,
            ): Boolean {
                return true
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = PaymentMethodViewHolderBinding.bind(itemView)
    }

}