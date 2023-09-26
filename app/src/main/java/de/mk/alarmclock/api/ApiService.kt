package de.mk.alarmclock.api

import de.mk.alarmclock.data.Alarm
import de.mk.alarmclock.data.CurrentDateTime
import de.mk.alarmclock.data.Light
import de.mk.alarmclock.data.LightSensor
import de.mk.alarmclock.data.Player
import de.mk.alarmclock.data.Sound
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("on_time")
    suspend fun getOnTime(): Response<Long>

    @Headers("Accept: application/json")
    @GET("current_datetime")
    suspend fun getCurrentDateTime(): Response<CurrentDateTime>

    @Headers("Accept: application/json")
    @GET("alarm/{alarm}")
    suspend fun getAlarm(@Path("alarm") alarm: Int): Response<Alarm>

    @Headers("Accept: application/json")
    @GET("light")
    suspend fun getLight(): Response<Light>

    @Headers("Accept: application/json")
    @GET("player")
    suspend fun getPlayer(): Response<Player>

    @Headers("Accept: application/json")
    @GET("light_sensor")
    suspend fun getLightSensor(): Response<LightSensor>

    @Headers("Accept: application/json")
    @GET("sounds")
    suspend fun getSounds(): Response<Set<Sound>>

    @GET("play")
    suspend fun play(@Query("sound") sound: Int): Response<Void>

    @GET("stop")
    suspend fun stop(): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("alarm")
    suspend fun setAlarm(@Body alarmData: Alarm): Response<Void>

    @Headers("Content-Type: application/json", "Content-Length: 0")
    @PUT("alarm/in8h/{alarm}")
    suspend fun setAlarmIn8h(@Path("alarm") alarm: Int): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("light")
    suspend fun setLight(@Body lightData: Light): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("player")
    suspend fun setPlayer(@Body playerData: Player): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("sounds")
    suspend fun setSounds(@Body soundsData: Set<Sound>): Response<Void>

    @Headers("Content-Type: application/json")
    @PUT("sound")
    suspend fun setSound(@Body soundData: Sound): Response<Void>

    @Headers("Content-Type: application/json")
    @POST("sound")
    suspend fun addSound(@Body soundData: Sound): Response<Void>

    @Headers("Content-Type: application/json")
    @DELETE("sound/{sound}")
    suspend fun deleteSound(@Path("sound") sound: Int): Response<Void>

    companion object {
        fun create(url: HttpUrl): ApiService = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()
            )
            .build()
            .create(ApiService::class.java)
    }
}