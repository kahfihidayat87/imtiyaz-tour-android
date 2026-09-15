package com.imtiyaztour.app

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// PENTING: teks Al-Quran diambil LANGSUNG dari equran.id (API publik resmi,
// gratis, tanpa API key) - BUKAN diketik ulang dari sini, supaya akurasinya
// terjamin. Kalau API ini suatu saat berubah struktur/mati, layar Quran akan
// menampilkan pesan gagal memuat (lihat QuranScreen.kt), bukan data palsu.

data class QuranWrapper<T>(val code: Int? = null, val message: String? = null, val data: T? = null)

data class SuratRingkas(
    val nomor: Int? = null,
    val nama: String? = null,
    val namaLatin: String? = null,
    val jumlahAyat: Int? = null,
    val tempatTurun: String? = null,
    val arti: String? = null,
    val deskripsi: String? = null
)

data class AyatDetail(
    val nomorAyat: Int? = null,
    val teksArab: String? = null,
    val teksLatin: String? = null,
    val teksIndonesia: String? = null,
    val audio: Map<String, String>? = null
)

data class SuratDetail(
    val nomor: Int? = null,
    val nama: String? = null,
    val namaLatin: String? = null,
    val jumlahAyat: Int? = null,
    val tempatTurun: String? = null,
    val arti: String? = null,
    val deskripsi: String? = null,
    val ayat: List<AyatDetail>? = null
)

interface QuranApiService {
    @GET("api/v2/surat")
    suspend fun getDaftarSurat(): QuranWrapper<List<SuratRingkas>>

    @GET("api/v2/surat/{nomor}")
    suspend fun getSuratDetail(@Path("nomor") nomor: Int): QuranWrapper<SuratDetail>
}

object QuranApi {
    val service: QuranApiService = Retrofit.Builder()
        .baseUrl("https://equran.id/")
        .client(OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(QuranApiService::class.java)
}
