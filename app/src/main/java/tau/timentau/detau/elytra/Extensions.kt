package tau.timentau.detau.elytra

import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import tau.timentau.detau.elytra.database.Status
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

private val dbDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val readableDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

private val dbTimeFormatter = SimpleDateFormat("kk:mm:ss", Locale.ITALY)
private val readableTimeFormatter = SimpleDateFormat("kk:mm", Locale.ITALY)

val TextInputLayout.text: String
    get() = editText?.text.toString()

fun String.isNotEmail() = !Patterns.EMAIL_ADDRESS.asPredicate().test(this)

fun String.doesNotMatch(pattern: String) = !this.matches(Regex(pattern))

fun String.parseToDate(): LocalDate {
    val tokens = this.split('/', '-').reversed()

    if (tokens.size != 3) throw IllegalArgumentException()
    return LocalDate.parse("${tokens[0]}-${tokens[1]}-${tokens[2]}")
}

fun Date.toLocalDate() = this.toReadable().parseToDate()

fun ChipGroup.noChipSelected() = checkedChipId == View.NO_ID

fun LocalDate.toDateString(): String {
    val date = dbDateFormatter.parse("$this")
    return dbDateFormatter.format(date!!)
}

fun String.toReadableDateString(): String {
    val date = dbDateFormatter.parse(this)
    return date!!.toReadable()
}

fun Date.toReadable(): String = readableDateFormatter.format(this)

fun fromMilliToReadable(milliseconds: Long) =
    Date.from(Instant.ofEpochMilli(milliseconds)).toReadable()

fun fromMilliToDate(milliseconds: Long) =
    Date.from(Instant.ofEpochMilli(milliseconds))

fun String.toReadableTimeString(): String {
    val date = dbTimeFormatter.parse(this)
    return readableTimeFormatter.format(date!!)
}

fun hiddenPasswordString(length: Int) = "•".repeat(length)

fun AppCompatActivity.getNavController(): NavController =
    (supportFragmentManager.fragments[0] as NavHostFragment).navController

val Fragment.navHostActivity: NavHostActivity
    get() {
        try {
            return requireActivity() as NavHostActivity
        } catch (e: ClassCastException) {
            throw IllegalStateException(
                "Activity attached to this fragment is not a NavHostActivity", e
            )
        }
    }

fun <T> ViewModel.performStateful(
    observableStatus: MutableLiveData<Status<T>>,
    block: suspend () -> T,
) {
    observableStatus.value = Status.loading()

    viewModelScope.launch {
        try {
            observableStatus.value = Status.success(block())
        } catch (e: Exception) {
            observableStatus.value = Status.failure(e)
        }
    }
}

fun DialogFragment.show(fragmentManager: FragmentManager): DialogFragment {
    show(fragmentManager, null)
    return this
}

fun DialogFragment.setDialogResultListener(
    resultKey: String, listener: () -> Unit
): DialogFragment {
    setFragmentResultListener(resultKey) { _, _ -> listener() }
    return this
}

fun DialogFragment.setDialogResult(resultKey: String)  {
    setFragmentResult(resultKey, Bundle())
    dismiss()
}

fun Activity.showConfirmDialog(@StringRes title: Int, @StringRes message: Int) {
    MaterialAlertDialogBuilder(
        this,
        ThemeOverlay_Material3_MaterialAlertDialog_Centered
    )
        .setTitle(getString(title))
        .setMessage(getString(message))
        .setIcon(R.drawable.ic_check_circle_24)
        .setPositiveButton(R.string.okay) { _, _ -> }
        .show()
}

fun Activity.showNetworkErrorDialog() {
    MaterialAlertDialogBuilder(
        this,
        ThemeOverlay_Material3_MaterialAlertDialog_Centered
    )
        .setTitle(R.string.errore_connessione)
        .setMessage(R.string.imposs_connettersi_al_server)
        .setIcon(R.drawable.ic_link_off_24)
        .setPositiveButton(R.string.okay) { _, _ -> }
        .show()
}

fun Fragment.showNetworkErrorDialog() {
    val activity = activity
        ?: throw IllegalStateException("Cannot show dialog: fragment is not attached")

    activity.showNetworkErrorDialog()
}