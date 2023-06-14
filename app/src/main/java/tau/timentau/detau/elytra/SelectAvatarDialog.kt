package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogSelectAvatarBinding

class SelectAvatarDialog : DialogFragment() {

    private lateinit var binding: DialogSelectAvatarBinding
    private lateinit var handler: SelectAvatarHandler

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        handler = context as SelectAvatarHandler
    }

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

        binding.selectAvatarDialogBottomButtons.negativeButton.text = getString(R.string.non_ora)
        binding.selectAvatarDialogBottomButtons.negativeButton.setOnClickListener {
            dismiss()
        }

        initRecyclerView()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun initRecyclerView() {
        val adapter = AvatarChoiceAdapter()
        binding.avatarList.adapter = adapter
        binding.avatarList.layoutManager =
            GridLayoutManager(context, 1, HORIZONTAL, false)

        coroutineScope.launch {
            val images = handler.fetchAvatars().await()

            adapter.submitList(images)
        }
    }

    interface SelectAvatarHandler {
        suspend fun fetchAvatars(): Deferred<List<Bitmap>>
    }
}