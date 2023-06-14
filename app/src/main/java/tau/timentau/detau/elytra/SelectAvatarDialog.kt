package tau.timentau.detau.elytra

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.database.Repository
import tau.timentau.detau.elytra.databinding.DialogSelectAvatarBinding

class SelectAvatarDialog : DialogFragment() {

    private lateinit var binding: DialogSelectAvatarBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = MaterialAlertDialogBuilder(activity)

            val layoutInflater = requireActivity().layoutInflater
            binding = DialogSelectAvatarBinding.inflate(layoutInflater)

            builder
                .setTitle("Selezione avatar")
                .setView(binding.root)
                .create()
                .also {
                    it.setCanceledOnTouchOutside(false)
                }

        } ?: throw IllegalStateException("No activity to attach dialog")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = AvatarChoiceAdapter()
        binding.avatarList.adapter = adapter
        binding.avatarList.layoutManager =
            GridLayoutManager(context, 1, HORIZONTAL, false)

        CoroutineScope(Dispatchers.Main).launch {
            val images = Repository.getAvatars().await()
            adapter.submitList(images)
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}