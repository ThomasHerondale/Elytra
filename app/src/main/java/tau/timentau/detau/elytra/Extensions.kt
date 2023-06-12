package tau.timentau.detau.elytra

import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout

val TextInputLayout.text: String
    get() = editText?.text.toString()

fun String.isNotEmail() = !Patterns.EMAIL_ADDRESS.asPredicate().test(this)