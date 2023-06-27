package tau.timentau.detau.elytra.model

import android.graphics.Bitmap
import java.util.Date

data class PaymentMethod(
    val number: String,
    val circuit: PaymentCircuit,
    val expiryDate: Date,
    val safetyCode: String,
    val ownerFullname: String
)

data class PaymentCircuit(
    val name: String,
    val logo: Bitmap,
    val startDigit: Char
)