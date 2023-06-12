package tau.timentau.detau.elytra.database

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

private val valueTypeKeys = mapOf(
    "%bool%" to "boolean"
)

object DatabaseDAO {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/webmobile/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val dbInterface: DatabaseAPI by lazy { retrofit.create(DatabaseAPI::class.java) }

    val parser: Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter())
        .create()

    suspend inline fun <T> selectList(query: String): List<T> {
        val response = dbInterface.select(formatQuery(query))
        // workaround per tipizzare il token senza passare la classe di T per parametro ;)
        val typeToken = object : TypeToken<List<T>>() {}.type

        return parser.fromJson(response.body()?.get("queryset"), typeToken) ?: listOf()
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend inline fun <reified T> selectValue(query: String): T? {
        val response = dbInterface.select(formatQuery(query))
        val body = response.body() ?: return null
        val typeToken = typeOf<T>().javaType

        return parser.fromJson(body["queryset"], typeToken)
    }

    fun formatQuery(query: String): String {
        // elimina spazi e indentazioni
        var formattedQuery = query
            .trimIndent()
            .trim()

        // sostituisci i placeholder per i tipi
        for ((key, value) in valueTypeKeys) {
            formattedQuery = formattedQuery.replace(key, value)
        }
        return formattedQuery
    }
}