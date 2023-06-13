package tau.timentau.detau.elytra.database

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class StringTypeAdapter: TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String) {
        // non necessario
    }

    override fun read(`in`: JsonReader): String {
        var result = ""

        `in`.beginObject()
        while (`in`.hasNext()) {
            val name = `in`.nextName()
            if (name == "queryset") {
                `in`.beginArray()
                while (`in`.hasNext()) {
                    `in`.beginObject()
                    while (`in`.hasNext()) {
                        val innerName = `in`.nextName()
                        if (innerName == "string") {
                            result = `in`.nextString()
                        } else {
                            `in`.skipValue()
                        }
                    }
                    `in`.endObject()
                }
                `in`.endArray()
            } else {
                `in`.skipValue()
            }
        }
        `in`.endObject()

        return result
    }
}