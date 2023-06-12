package tau.timentau.detau.elytra.database

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DatabaseAPI {

    @POST("postSelect/")
    @FormUrlEncoded
    suspend fun select(@Field("query") query: String): Response<JsonObject>

    @POST("postUpdate/")
    @FormUrlEncoded
    suspend fun update(@Field("query") query: String): Response<JsonObject>

    @POST("postRemove/")
    @FormUrlEncoded
    suspend fun remove(@Field("query") query: String): Response<JsonObject>

    @POST("postInsert/")
    @FormUrlEncoded
    suspend fun insert(@Field("query") query: String): Response<JsonObject>
}