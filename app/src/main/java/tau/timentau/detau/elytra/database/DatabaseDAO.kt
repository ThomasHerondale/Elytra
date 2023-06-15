package tau.timentau.detau.elytra.database

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

const val QUERYSET_KEY = "queryset"

object DatabaseDAO {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/webmobile/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val dbInterface: DatabaseAPI by lazy { retrofit.create(DatabaseAPI::class.java) }

    val valueParser: Gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter())
        .registerTypeAdapter(String::class.java, StringTypeAdapter())
        .create()

    val listParser = Gson()

    suspend inline fun <reified T> selectList(query: String): List<T> {
        val response = dbInterface.select(formatQuery(query))
        // workaround per tipizzare il token senza passare la classe di T per parametro ;)
        val typeToken = object : TypeToken<List<T>>() {}.type

        return listParser.fromJson(response.body()?.get(QUERYSET_KEY), typeToken) ?: listOf()
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend inline fun <reified T> selectValue(query: String): T? {
        val response = dbInterface.select(formatQuery(query))
        val body = response.body() ?: return null
        val jsonArray = body[QUERYSET_KEY].asJsonArray

        println(jsonArray)

        // l'oggetto json dovrebbe contenere un solo valore
        if (jsonArray.size() != 1)
            throw IllegalStateException("Query didn't return single value")

        val typeToken = typeOf<T>().javaType
        return valueParser.fromJson(jsonArray[0], typeToken)
    }

    suspend inline fun insert(query: String) {
        val response = dbInterface.insert(formatQuery(query))
        val body = response.body() ?: throw NetworkException("Server did not respond on insert")
        val message = body[QUERYSET_KEY].asString

        if (message != "insert executed!") throw NetworkException("Insert not succesful")
    }

    suspend inline fun update(query: String) {
        val response = dbInterface.update(formatQuery(query))
        val body = response.body() ?: throw NetworkException("Server did not respond on update")
        val message = body[QUERYSET_KEY].asString

        if (message != "update executed!") throw NetworkException("Update not succesful")
    }

    fun formatQuery(query: String): String {
        // elimina spazi e indentazioni
        return query
            .trimIndent()
            .trim()
    }
}