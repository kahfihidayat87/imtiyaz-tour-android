package com.imtiyaztour.app

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Koordinat Masjidil Haram, Makkah - waktu shalat SENGAJA tetap untuk Makkah
// (bukan lokasi HP jamaah), sesuai permintaan: mengikuti Kalender Ummul Qura.
private const val MAKKAH_LAT = 21.4225
private const val MAKKAH_LNG = 39.8262
private const val METODE_UMMUL_QURA = 4 // kode method Aladhan API untuk "Umm Al-Qura University, Makkah"

data class TimingsData(
    val Fajr: String? = null,
    val Sunrise: String? = null,
    val Dhuhr: String? = null,
    val Asr: String? = null,
    val Maghrib: String? = null,
    val Isha: String? = null
)
data class AladhanDateInfo(
    @SerializedName("readable") val readable: String? = null
)
data class AladhanResult(
    val timings: TimingsData? = null,
    val date: AladhanDateInfo? = null
)
data class AladhanResponse(val data: AladhanResult? = null)

interface AladhanApiService {
    @GET("v1/timings")
    suspend fun getTimingsMakkah(
        @Query("latitude") lat: Double = MAKKAH_LAT,
        @Query("longitude") lng: Double = MAKKAH_LNG,
        @Query("method") method: Int = METODE_UMMUL_QURA
    ): AladhanResponse
}

object AladhanApi {
    val service: AladhanApiService = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AladhanApiService::class.java)
}
