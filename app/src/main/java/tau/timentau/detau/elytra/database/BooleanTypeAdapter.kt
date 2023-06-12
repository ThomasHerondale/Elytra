package tau.timentau.detau.elytra.database

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter?, value: Boolean?) {
        // non necessario
    }

    override fun read(`in`: JsonReader): Boolean {
        when (val token = `in`.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                `in`.beginArray()
                val result = if (`in`.hasNext()) {
                    `in`.beginObject()
                    val fieldName = `in`.nextName()
                    if (fieldName == "boolean") {
                        val numberValue = `in`.nextInt()
                        `in`.endObject()
                        numberValue == 1
                    } else {
                        throw IOException("Invalid field name: $fieldName")
                    }
                } else {
                    throw IOException("Empty array")
                }
                `in`.endArray()
                return result
            }
            JsonToken.BOOLEAN -> {
                return `in`.nextBoolean()
            }
            else -> {
                throw IOException("Invalid token: $token")
            }
        }
    }
}