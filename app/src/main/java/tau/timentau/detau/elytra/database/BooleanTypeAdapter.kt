package tau.timentau.detau.elytra.database

import android.util.Log
    import com.google.gson.TypeAdapter
    import com.google.gson.stream.JsonReader
    import com.google.gson.stream.JsonToken
    import com.google.gson.stream.JsonWriter
    import java.io.IOException

class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        value?.let { out.value(if (it) 1 else 0) }
    }

    override fun read(input: JsonReader): Boolean {
        if (input.peek() == JsonToken.BEGIN_OBJECT) {
            input.beginObject()
            while (input.hasNext()) {
                if (input.nextName() == "boolean") {
                    val booleanValue = input.nextInt() == 1
                    input.endObject()
                    return booleanValue
                } else {
                    input.skipValue()
                }
            }
            input.endObject()
        }
        return false
    }
}

