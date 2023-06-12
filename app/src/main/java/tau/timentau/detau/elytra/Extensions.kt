package tau.timentau.detau.elytra

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

val TextInputLayout.text: String
    get() = editText?.text.toString()