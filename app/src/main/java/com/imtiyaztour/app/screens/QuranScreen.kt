package com.imtiyaztour.app.screens

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

@Composable
fun QuranScreen() {
    var suratTerpilih by remember { mutableStateOf<SuratRingkas?>(null) }

    val terpilih = suratTerpilih
    if (terpilih != null) {
        SuratReaderScreen(terpilih) { suratTerpilih = null }
        return
    }

    SuratListScreen(onPilihSurat = { suratTerpilih = it })
}

@Composable
private fun SuratListScreen(onPilihSurat: (SuratRingkas) -> Unit) {
    var daftar by remember { mutableStateOf<List<SuratRingkas>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val res = QuranApi.service.getDaftarSurat()
            daftar = res.data ?: emptyList()
        } catch (e: Exception) {
            errorMsg = "Gagal memuat daftar surah - periksa koneksi internet."
        } finally { loading = false }
    }

    val filtered = if (query.isBlank()) daftar else daftar.filter {
        (it.namaLatin ?: "").contains(query, ignoreCase = true) || (it.arti ?: "").contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Al-Qur'an", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("114 surah, teks dan audio dari equran.id", color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Cari nama surah...", color = Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Sand, unfocusedTextColor = Sand,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))

        when {
            loading -> Box(Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGold)
            }
            errorMsg != null -> Text(errorMsg!!, color = Danger, fontSize = 13.sp)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { surat ->
                    SuratRow(surat) { onPilihSurat(surat) }
                }
            }
        }
    }
}

@Composable
private fun SuratRow(surat: SuratRingkas, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).background(BrandGold.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("${surat.nomor}", color = BrandGoldSoft, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(surat.namaLatin ?: "-", color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    "${surat.arti ?: ""} • ${surat.jumlahAyat ?: 0} ayat • ${surat.tempatTurun ?: ""}",
                    color = Muted, fontSize = 10.5.sp
                )
            }
            Text(surat.nama ?: "", color = BrandGoldSoft, fontSize = 15.sp)
        }
    }
}

@Composable
private fun SuratReaderScreen(surat: SuratRingkas, onBack: () -> Unit) {
    var detail by remember { mutableStateOf<SuratDetail?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var playingAyat by remember { mutableStateOf<Int?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(surat.nomor) {
        try {
            val res = QuranApi.service.getSuratDetail(surat.nomor ?: 1)
            detail = res.data
        } catch (e: Exception) {
            errorMsg = "Gagal memuat isi surah - periksa koneksi internet."
        } finally { loading = false }
    }

    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    fun stopAudio() { mediaPlayer?.release(); mediaPlayer = null; playingAyat = null }

    fun playAyat(nomorAyat: Int, audioMap: Map<String, String>?) {
        val url = audioMap?.values?.firstOrNull() ?: return
        if (playingAyat == nomorAyat) { stopAudio(); return }
        stopAudio()
        try {
            val mp = MediaPlayer()
            mp.setDataSource(url)
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener { playingAyat = null }
            mp.prepareAsync()
            mediaPlayer = mp
            playingAyat = nomorAyat
        } catch (e: Exception) { }
    }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        TextButton(onClick = { stopAudio(); onBack() }) {
            Text("< Kembali ke Daftar Surah", color = BrandGoldSoft, fontSize = 13.sp)
        }
        Text(surat.namaLatin ?: "-", color = Sand, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Text("${surat.arti ?: ""} • ${surat.jumlahAyat ?: 0} ayat • ${surat.tempatTurun ?: ""}", color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(bottom = 10.dp))

        when {
            loading -> Box(Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGold)
            }
            errorMsg != null -> Text(errorMsg!!, color = Danger, fontSize = 13.sp)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(detail?.ayat ?: emptyList()) { ayat ->
                    AyatCard(
                        ayat = ayat,
                        playing = playingAyat == ayat.nomorAyat,
                        onPlayClick = { playAyat(ayat.nomorAyat ?: 0, ayat.audio) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AyatCard(ayat: AyatDetail, playing: Boolean, onPlayClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(26.dp).background(BrandGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("${ayat.nomorAyat}", color = BrandGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(30.dp).background(if (playing) Danger else BrandGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onPlayClick, modifier = Modifier.fillMaxSize()) {
                        Text(if (playing) "⏸" else "▶", color = BrandGreen, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(ayat.teksArab ?: "", color = BrandGoldSoft, fontSize = 19.sp, lineHeight = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(ayat.teksLatin ?: "", color = Sand, fontSize = 12.sp, fontStyle = FontStyle.Italic, lineHeight = 17.sp)
            Spacer(Modifier.height(6.dp))
            Text(ayat.teksIndonesia ?: "", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
