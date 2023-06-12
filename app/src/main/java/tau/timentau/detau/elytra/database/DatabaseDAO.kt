package tau.timentau.detau.elytra.database

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

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

    suspend inline fun <reified T> selectList(query: String): List<T> {
        val response = dbInterface.select(formatQuery(query))
        // workaround per tipizzare il token senza passare la classe di T per parametro ;)
        val typeToken = object : TypeToken<List<T>>() {}.type

        return parser.fromJson(response.body()?.get("queryset"), typeToken) ?: listOf()
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend inline fun <reified T> selectValue(query: String): T? {
        val response = dbInterface.select(formatQuery(query))
        val body = response.body() ?: return null
        val jsonArray = body["queryset"].asJsonArray

        // l'oggetto json dovrebbe contenere un solo valore
        if (jsonArray.size() != 1)
            throw IllegalStateException("Query didn't return single value")

        val typeToken = typeOf<T>().javaType
        return parser.fromJson(jsonArray[0], typeToken)
    }

    fun formatQuery(query: String): String {
        // elimina spazi e indentazioni
        return query
            .trimIndent()
            .trim()
    }
}