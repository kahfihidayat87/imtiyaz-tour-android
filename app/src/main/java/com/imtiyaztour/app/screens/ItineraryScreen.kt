package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

/**
 * Itinerary RESMI program 9 hari via YIA (Kulon Progo), diambil dari dokumen
 * "Rencana Perjalanan Umrah" yang dikonfirmasi Kahfi (bukan lagi contoh
 * generik). Dua versi: Reguler dan Ramadhan (beda rute penerbangan pulang-
 * pergi dan ada penyesuaian jadwal puasa). Kalau ada revisi jadwal resmi
 * berikutnya, edit langsung dua daftar di bawah ini.
 */
private data class ItinerariHari(val hari: String, val tgl: String, val judul: String, val isi: String)

private val ITINERARI_REGULER = listOf(
    ItinerariHari("Hari 1", "", "Yogyakarta(YIA)–Jakarta–Jeddah–Madinah",
        "Kumpul 05:30 di YIA Kulon Progo, terbang 07:00 ke Jakarta, lanjut 10:30 ke Jeddah (tiba 16:40 waktu Saudi, shalat Dzuhur-Ashar jama' di pesawat). Bus ke Madinah, pembagian kamar, makan malam, shalat Maghrib-Isya jama' takhir di Masjid Nabawi."),
    ItinerariHari("Hari 2", "", "Madinah",
        "04:00 shalat Subuh di Nabawi, ziarah makam Rasulullah ﷺ, Abu Bakar, Umar bin Khattab, dan Baqi'. Shalat Dhuha, sarapan. 16:30 (selepas Ashar): ziarah sekitar Nabawi (Tsaqifa Bani Saidah, Masjid Ghamamah, Masjid Abu Bakar, dll)."),
    ItinerariHari("Hari 3", "", "Madinah, Ziarah",
        "04:00 shalat lail, tadarus, dzikir, Subuh; bertahan hingga syuruq untuk tadarus. 08:30: ziarah kota Madinah (Masjid Quba, Jabal Uhud, Masjid Qiblatain, dll). 16:30: penjelasan teknis keberangkatan ke Makkah di restoran hotel."),
    ItinerariHari("Hari 4", "", "Madinah → Makkah",
        "07:00 koper siap di depan kamar. 09:00 berangkat ke Makkah, miqat & niat umrah di Masjid Aisyah (Bir Ali). 15:00 tiba hotel Makkah, lanjut Masjidil Haram: shalat Dzuhur-Ashar jama' ta'khir dan umrah (thawaf, sa'i, tahallul). Makan malam, shalat Maghrib-Isya di Masjidil Haram."),
    ItinerariHari("Hari 5", "", "Makkah",
        "04:00 shalat Subuh di Masjidil Haram, bertahan hingga syuruq untuk tadarus. Sarapan & istirahat. Program bebas, perbanyak ibadah mandiri."),
    ItinerariHari("Hari 6", "", "Makkah",
        "09:00 program ziarah kota Makkah (Jabal Tsur, Jabal Hira, Padang Arafah, Jabal Rahmah, dll), singgah Masjid Ju'ranah (miqat umrah kedua bagi yang ingin). 16:30: taushiyah bersama Ustadz Pembimbing."),
    ItinerariHari("Hari 7", "", "Makkah (Jumat)",
        "04:00 shalat Subuh, menunggu syuruq di lantai atas Masjidil Haram sambil taushiyah, shalat Dhuha. Sarapan, persiapan shalat Jumat. 16:30: taushiyah + penjelasan teknis kepulangan."),
    ItinerariHari("Hari 8", "", "Makkah → Jeddah",
        "Shalat Subuh, thawaf wada'. 08:00 koper terkunci di depan kamar. 10:00 menuju Jeddah, singgah Corniche Jeddah, shalat Dzuhur-Ashar jama' taqdim di Jeddah. 18:20 terbang Jeddah → Jakarta."),
    ItinerariHari("Hari 9", "", "Jakarta → Yogyakarta",
        "Mendarat Soekarno-Hatta ±08:30, imigrasi & bea cukai. 11:30 lanjut penerbangan ke YIA, Kulon Progo. Doa penutup, program selesai."),
)

private val ITINERARI_RAMADHAN = listOf(
    ItinerariHari("Hari 1", "", "Yogyakarta(YIA)–Jakarta–Jeddah–Madinah",
        "Kumpul 05:30 di YIA, terbang 07:00 ke Jakarta, lanjut 10:30 ke Jeddah (tiba 16:40, Dzuhur-Ashar jama' di pesawat). Bus ke Madinah, check-in, buka puasa, shalat Maghrib-Isya jama' takhir di Masjid Nabawi."),
    ItinerariHari("Hari 2", "", "Madinah",
        "Sahur 04:00, shalat Subuh di Nabawi, ziarah makam Rasulullah ﷺ/Abu Bakar/Umar/Baqi'. Dhuha, istirahat. 16:30: ziarah sekitar Nabawi (Tsaqifa Bani Saidah, Ghamamah, Abu Bakar). Buka puasa + shalat tarawih."),
    ItinerariHari("Hari 3", "", "Madinah, Ziarah",
        "Sahur, Subuh, tadarus sampai syuruq. 08:30: ziarah Madinah (Quba, Uhud, Qiblatain). 16:30: penjelasan teknis keberangkatan ke Makkah."),
    ItinerariHari("Hari 4", "", "Madinah → Makkah",
        "07:00 koper siap. 09:00 berangkat, miqat di Bir Ali. 15:00 tiba hotel Makkah, Dzuhur-Ashar jama' ta'khir + langsung umrah (thawaf, sa'i, tahallul). Buka puasa, shalat Maghrib-Isya-Tarawih di Masjidil Haram."),
    ItinerariHari("Hari 5", "", "Makkah",
        "Sahur, Subuh, tadarus sampai syuruq. Program bebas, istirahat memulihkan tenaga untuk puasa."),
    ItinerariHari("Hari 6", "", "Makkah",
        "09:00 ziarah (Jabal Tsur, Jabal Hira, Arafah, Jabal Rahmah), singgah Ju'ranah (miqat umrah kedua). 16:30: taushiyah. Persiapan buka puasa + tarawih."),
    ItinerariHari("Hari 7", "", "Makkah (Jumat)",
        "Sahur, Subuh, tunggu syuruq + taushiyah, Dhuha, persiapan shalat Jumat. 16:30: taushiyah + penjelasan teknis pulang. Buka puasa + tarawih."),
    ItinerariHari("Hari 8", "", "Makkah → Jeddah",
        "Sahur, Subuh, thawaf wada'. 08:00 koper terkunci. 10:00 ke Jeddah via Corniche, Dzuhur-Ashar jama' taqdim di Jeddah. 18:20 terbang Jeddah → Jakarta (buka puasa, Maghrib, Isya, Subuh dilaksanakan di pesawat)."),
    ItinerariHari("Hari 9", "", "Jakarta → Yogyakarta",
        "Mendarat Soekarno-Hatta ±08:30, imigrasi & bea cukai. 11:30 lanjut penerbangan ke YIA. Doa penutup, program selesai."),
)

@Composable
fun ItineraryScreen() {
    var tab by remember { mutableStateOf(0) } // 0 = Reguler, 1 = Ramadhan

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Itinerary Perjalanan", color = Sand, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Program Umrah 9 Hari dari Yogyakarta (YIA)",
            color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegmentBtn("Reguler", tab == 0, Modifier.weight(1f)) { tab = 0 }
            SegmentBtn("Ramadhan", tab == 1, Modifier.weight(1f)) { tab = 1 }
        }
        Spacer(Modifier.height(12.dp))

        val daftar = if (tab == 0) ITINERARI_REGULER else ITINERARI_RAMADHAN

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(daftar) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row {
                            Text(item.hari, color = BrandGold, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            Text("  •  ${item.judul}", color = BrandGoldSoft, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(item.isi, color = Sand, fontSize = 12.5.sp, lineHeight = 18.sp)
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    if (tab == 0) "Catatan resmi: program bisa berubah menyesuaikan situasi dan kondisi di lapangan."
                    else "Catatan resmi: program bisa berubah menyesuaikan situasi/kondisi lapangan; ziarah selama Ramadhan tergantung izin otoritas Saudi Arabia.",
                    color = Muted, fontSize = 11.sp, lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SegmentBtn(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) BrandGold else PanelColor,
            contentColor = if (active) BrandGreen else Muted
        )
    ) { Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold) }
}
