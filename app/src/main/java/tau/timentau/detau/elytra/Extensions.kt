package tau.timentau.detau.elytra

import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormatter =  SimpleDateFormat("yyyy-MM-dd", Locale.US)

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
    val date = dateFormatter.parse("$this")
    return dateFormatter.format(date!!)
}

fun AppCompatActivity.getNavController(): NavController =
    (supportFragmentManager.fragments[0] as NavHostFragment).navController

val Fragment.navHostActivity: NavHostActivity
    get() {
        try {
            return requireActivity() as NavHostActivity
        } catch (e: ClassCastException) {
            System.err.println("Activity attached to this fragment is not a NavHostActivity")
            throw e
        }
    }