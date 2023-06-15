package tau.timentau.detau.elytra

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.HORIZONTAL
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tau.timentau.detau.elytra.databinding.DialogSelectAvatarBinding

private const val TAG = "SELECT_AVATAR"

class SelectAvatarDialog : DialogFragment() {

    private lateinit var binding: DialogSelectAvatarBinding
    private lateinit var handler: SelectAvatarHandler

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        networkError()
        Log.e(TAG, e.stackTraceToString())
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + coroutineExceptionHandler)

    private var currentSelectedAvatar: Int? = null

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

        // disabilita il pulsante di conferma all'avvio
        binding.selectAvatarDialogBottomButtons.positiveButton.isEnabled = false

        binding.selectAvatarDialogBottomButtons.negativeButton.text = getString(R.string.non_ora)
        binding.selectAvatarDialogBottomButtons.negativeButton.setOnClickListener {
            dismiss()
        }

        binding.selectAvatarDialogBottomButtons.positiveButton.setOnClickListener {
            coroutineScope.launch {
                handler.avatarSelected(currentSelectedAvatar!!)
                Log.v(TAG, "Selected and set avatar n. $currentSelectedAvatar")
            }
                .invokeOnCompletion { dismiss() }
        }

        initRecyclerView()

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun initRecyclerView() {
        val adapter = AvatarChoiceAdapter {// ogni volta che un avatar viene selezionato

            // se è stato selezionato un avatar per la prima volta, abilita la conferma
            if (currentSelectedAvatar == null)
                enableConfirmButton()
            // modifica la selezione
            currentSelectedAvatar = it
        }
        binding.avatarList.adapter = adapter
        binding.avatarList.layoutManager =
            GridLayoutManager(context, 1, HORIZONTAL, false)

        coroutineScope.launch {
            val images = handler.fetchAvatars().await()
            Log.i(TAG, "Fetching avatars from database")

            adapter.submitList(images.map { AvatarChoice(it) })
        }
    }

    private fun enableConfirmButton() {
        binding.selectAvatarDialogBottomButtons.positiveButton.isEnabled = true
    }

    private fun networkError() {
        MaterialAlertDialogBuilder(
            requireActivity(),
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setTitle(R.string.errore_connessione)
            .setMessage(R.string.imposs_connettersi_al_server)
            .setIcon(R.drawable.ic_link_off_24)
            .setPositiveButton(R.string.okay) { _, _ -> }
            .show()
    }

    interface SelectAvatarHandler {
        suspend fun fetchAvatars(): Deferred<List<Bitmap>>

        suspend fun avatarSelected(id: Int)

        fun toAvatarSetConfirm()
    }
}