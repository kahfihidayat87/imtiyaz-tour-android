package com.imtiyaztour.app

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.concurrent.TimeUnit

// Sesuai README backend yang diupload: ganti kalau alamat deploy Hostinger-nya beda.
private const val BASE_URL = "https://api.pastiumrah.com/"

// ---------- Model data (mengikuti struktur PAKET_EXISTING & endpoint di app.js) ----------
data class Paket(
    val id: String?,
    val nama: String?,
    val subtitle: String?,
    val url: String?,
    val tipe: String?,
    val harga: String?,
    val badge: String?,
    val icon: String?,
    val warna: String?
)

// Bentuk respons /api/jamaah/:id belum pasti persis (tergantung WP), field dibuat nullable
// supaya app tidak crash kalau ada field tambahan/kurang dari WordPress.
data class DokumenStatus(
    val ktp: Boolean = false, val kk: Boolean = false, val paspor: Boolean = false,
    val foto: Boolean = false, val buku_nikah: Boolean = false, val vaksin_meningitis: Boolean = false
)
data class JamaahStatus(
    val nama: String? = null,
    val status: String? = null,          // "Lunas" / "Belum Lunas"
    val sisa_tagihan: String? = null,
    val akses_keuangan: Boolean = true,
    val akses_dokumen: Boolean = true,
    val dokumen: DokumenStatus? = null,
    val error: String? = null
)

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val success: Boolean? = null,
    val jamaah_id: String? = null,
    val nama: String? = null,
    val akses_keuangan: Boolean = true,
    val akses_dokumen: Boolean = true
)

data class FcmTokenRequest(val jamaah_id: String, val fcm_token: String)
data class FcmTokenResponse(val success: Boolean? = null)

data class RadioBroadcastRequest(val channel: String, val mime: String, val audio: String)
data class RadioClip(val id: Int? = null, val mime: String? = null, val audio: String? = null, val ts: Long? = null)
data class RadioHistoryResponse(val history: List<RadioClip> = emptyList())

data class JamaahLive(
    val jamaah_id: String? = null,
    val nama: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val updated_at: String? = null
)

data class UpdateLocationRequest(
    val jamaah_id: String,
    val lat: Double,
    val lng: Double,
    val gps_consent: Boolean,
    val battery: Int,
    val accuracy: Float
)
data class UpdateLocationResponse(val success: Boolean? = null, val message: String? = null)

data class SosRequest(val jamaah_id: String, val lat: Double, val lng: Double, val message: String)
data class SosResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val maps_url: String? = null,
    val admin_notified: Boolean? = null
)

data class UploadBuktiResponse(
    val success: Boolean? = null,
    val bukti_url: String? = null,
    val status: String? = null,
    val error: String? = null
)

data class SkriningResponse(val success: Boolean? = null, val error: String? = null)

// Trip live individual (jadwal keberangkatan asli), dari GET /api/paket-live
data class TripLive(
    val id: Int? = null,
    val judul: String? = null,
    val tipe: String? = null,
    val tipe_slug: String? = null,
    val tanggal: String? = null,
    val durasi: String? = null,
    val harga: String? = null,
    val harga_asli: String? = null, // harga sebelum diskon, kalau ada (untuk tampilan coret)
    val url: String? = null,
    val status: String? = null // "OPEN" / "CLOSED"
)
data class PaketLiveResponse(
    val existing_types: List<Paket> = emptyList(),
    val latest_trips: List<TripLive> = emptyList()
)

interface ImtiyazApiService {
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/register-fcm-token")
    suspend fun registerFcmToken(@Body body: FcmTokenRequest): FcmTokenResponse

    @POST("api/radio-broadcast")
    suspend fun radioBroadcast(@Body body: RadioBroadcastRequest): FcmTokenResponse

    @GET("api/radio-history")
    suspend fun radioHistory(@Query("channel") channel: String): RadioHistoryResponse

    @GET("api/paket")
    suspend fun getPaket(): List<Paket>

    @GET("api/paket-live")
    suspend fun getPaketLive(): PaketLiveResponse

    @GET("api/jamaah/{id}")
    suspend fun getJamaahStatus(@Path("id") jamaahId: String): JamaahStatus

    @GET("api/jamaah-live")
    suspend fun getJamaahLive(): List<JamaahLive>

    @POST("api/update-location")
    suspend fun updateLocation(@Body body: UpdateLocationRequest): UpdateLocationResponse

    @POST("api/sos")
    suspend fun sendSos(@Body body: SosRequest): SosResponse

    @Multipart
    @POST("api/upload-bukti")
    suspend fun uploadBukti(
        @Part("jamaah_id") jamaahId: okhttp3.RequestBody,
        @Part bukti: MultipartBody.Part
    ): UploadBuktiResponse

    @POST("api/skrining")
    suspend fun submitSkrining(@Body body: Map<String, @JvmSuppressWildcards Any>): SkriningResponse

    @POST("api/evaluasi")
    suspend fun submitEvaluasi(@Body body: Map<String, @JvmSuppressWildcards Any>): SkriningResponse
}

object ImtiyazApi {
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val service: ImtiyazApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImtiyazApiService::class.java)

    /** Helper untuk membungkus file bukti transfer jadi multipart part. */
    fun buktiPart(file: File): MultipartBody.Part {
        val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("bukti", file.name, reqBody)
    }

    fun textPart(value: String): okhttp3.RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())
}
