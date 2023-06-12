package tau.timentau.detau.elytra

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormatter =  SimpleDateFormat("yyyy-MM-dd", Locale.US)

val TextInputLayout.text: String
    get() = editText?.text.toString()

fun LocalDate.toDateString(): String {
    val date = dateFormatter.parse("$this")
    return dateFormatter.format(date!!)
}