package tau.timentau.detau.elytra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tau.timentau.detau.elytra.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        showProgressDialog()
        setContentView(binding.root)
    }

    private fun showProgressDialog() {
        MaterialAlertDialogBuilder(
            this,
            ThemeOverlay_Material3_MaterialAlertDialog_Centered
        )
            .setView(R.layout.dialog_progress_simple)
            .setCancelable(false)
            .show()
    }
}