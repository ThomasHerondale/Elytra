package tau.timentau.detau.elytra

import android.content.Intent
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dbDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
private val readableDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

val TextInputLayout.text: String
    get() = editText?.text.toString()

fun String.isNotEmail() = !Patterns.EMAIL_ADDRESS.asPredicate().test(this)

fun String.doesNotMatch(pattern: String) = !this.matches(Regex(pattern))

fun String.parseToDate(): LocalDate {
    val tokens = this.split('/', '-').reversed()

    if (tokens.size != 3) throw IllegalArgumentException()
    return LocalDate.parse("${tokens[0]}-${tokens[1]}-${tokens[2]}")
}

fun ChipGroup.noChipSelected() = checkedChipId == View.NO_ID

fun LocalDate.toDateString(): String {
    val date = dbDateFormatter.parse("$this")
    return dbDateFormatter.format(date!!)
}

fun Date.toReadable(): String = readableDateFormatter.format(this)

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

val AppCompatActivity.EMAIL_KEY: String
    get() = "email"

val AppCompatActivity.loggedEmail: String
    get() = intent.getStringExtra(EMAIL_KEY) ?:
        throw IllegalStateException("Could not retrieve logged user email")

fun AppCompatActivity.startLoggedActivity(intent: Intent) {
    try {
        intent.putExtra(EMAIL_KEY, loggedEmail)
        startActivity(intent)
    } catch (e: java.lang.IllegalStateException) {
        throw IllegalStateException(
            "This method should be called from another logged activity.", e)
    }
}

fun MaterialAutoCompleteTextView.setOnItemClickListener (
    onItemClicked: (AdapterView<*>, View, Int, Long) -> Unit
) {
    onItemClickListener = AdapterView.OnItemClickListener(onItemClicked)
}