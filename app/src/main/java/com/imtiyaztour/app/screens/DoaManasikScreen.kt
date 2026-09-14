package com.imtiyaztour.app.screens

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

/**
 * CATATAN PENTING soal audio:
 * URL audio di bawah ("audioUrl") adalah PLACEHOLDER pola penamaan - saya tidak
 * menyertakan file MP3 sungguhan karena tidak bisa memverifikasi/menyediakan
 * rekaman doa yang sah (hak cipta rekaman, kualitas bacaan, dsb - sebaiknya
 * direkam ustadz/pembimbing manasik kalian sendiri). Yang SUDAH bekerja penuh
 * di sini adalah PEMUTARNYA (tombol play/pause) - begitu file MP3 asli diupload
 * ke Media Library WordPress kalian, tinggal ganti nilai audioUrl di bawah,
 * tidak perlu ubah kode lain. Teks Arab/Latin/terjemahan adalah teks umum doa
 * umrah yang lazim dipakai (bukan kutipan dari sumber berhak cipta tertentu).
 */
private data class Doa(
    val judul: String,
    val arab: String,
    val latin: String,
    val terjemahan: String,
    val audioUrl: String
)

private val DAFTAR_DOA = listOf(
    Doa(
        "Niat Ihram Umrah",
        "نَوَيْتُ الْعُمْرَةَ وَأَحْرَمْتُ بِهَا لِلّٰهِ تَعَالَى",
        "Nawaitul umrata wa ahramtu bihaa lillaahi ta'aalaa",
        "Aku niat umrah dan berihram karenanya, karena Allah Ta'ala.",
        "https://pastiumrah.com/wp-content/uploads/doa/01-niat-ihram.mp3"
    ),
    Doa(
        "Talbiyah",
        "لَبَّيْكَ اللّٰهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
        "Labbaikallaahumma labbaik, labbaika laa syarika laka labbaik, innal hamda wan-ni'mata laka wal mulk, laa syarika lak",
        "Aku penuhi panggilan-Mu ya Allah. Sesungguhnya segala puji, nikmat, dan kerajaan adalah milik-Mu, tiada sekutu bagi-Mu.",
        "https://pastiumrah.com/wp-content/uploads/doa/02-talbiyah.mp3"
    ),
    Doa(
        "Doa Masuk Kota Makkah",
        "اللّٰهُمَّ هٰذَا حَرَمُكَ وَأَمْنُكَ فَحَرِّمْنِي عَلَى النَّارِ",
        "Allahumma haadzaa haramuka wa amnuka fa harrimnii 'alan-naar",
        "Ya Allah, ini adalah tanah haram dan aman-Mu, maka haramkanlah diriku dari api neraka.",
        "https://pastiumrah.com/wp-content/uploads/doa/03-masuk-makkah.mp3"
    ),
    Doa(
        "Doa Melihat Ka'bah",
        "اللّٰهُمَّ زِدْ هٰذَا الْبَيْتَ تَشْرِيفًا وَتَعْظِيمًا وَتَكْرِيمًا وَمَهَابَةً",
        "Allahumma zid haadzal baita tasyriifan wa ta'ziiman wa takriiman wa mahaabah",
        "Ya Allah, tambahkanlah kemuliaan, keagungan, kehormatan, dan kewibawaan pada Baitullah ini.",
        "https://pastiumrah.com/wp-content/uploads/doa/04-melihat-kabah.mp3"
    ),
    Doa(
        "Doa Antara Rukun Yamani & Hajar Aswad",
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "Rabbanaa aatinaa fid-dunyaa hasanah wa fil-aakhirati hasanah wa qinaa 'adzaaban-naar",
        "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, dan lindungilah kami dari siksa neraka.",
        "https://pastiumrah.com/wp-content/uploads/doa/05-rukun-yamani.mp3"
    ),
    Doa(
        "Doa Sa'i (Shafa & Marwah)",
        "إِنَّ الصَّفَا وَالْمَرْوَةَ مِنْ شَعَائِرِ اللّٰهِ",
        "Innash-shafaa wal marwata min sya'aa'irillaah",
        "Sesungguhnya Shafa dan Marwah termasuk syiar-syiar Allah.",
        "https://pastiumrah.com/wp-content/uploads/doa/06-sai.mp3"
    ),
    Doa(
        "Doa Tahallul (Selesai Umrah)",
        "اللّٰهُمَّ اغْفِرْ لِلْمُحَلِّقِينَ وَالْمُقَصِّرِينَ",
        "Allahummaghfir lil-muhalliqiina wal-muqashshiriin",
        "Ya Allah, ampunilah orang-orang yang mencukur habis rambutnya dan yang memendekkannya.",
        "https://pastiumrah.com/wp-content/uploads/doa/07-tahallul.mp3"
    ),
    Doa(
        "Doa Safar (Perjalanan)",
        "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ. سُبْحَانَ الَّذِي سَخَّرَ لَنَا هٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ. اللّٰهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هٰذَا الْبِرَّ وَالتَّقْوَىٰ، وَمِنَ الْعَمَلِ مَا تَرْضَىٰ. اللّٰهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هٰذَا وَاطْوِ عَنَّا بُعْدَهُ",
        "Allahu akbar, Allahu akbar, Allahu akbar. Subhaanalladzii sakhkhara lanaa haadzaa wa maa kunnaa lahu muqriniin wa innaa ilaa rabbinaa lamunqalibuun. Allahumma innaa nas-aluka fii safarinaa haadzal birra wat-taqwaa, wa minal 'amali maa tardhaa. Allahumma hawwin 'alainaa safaranaa haadzaa wathwi 'annaa bu'dahu",
        "Allah Maha Besar (3x). Maha Suci Allah yang telah menundukkan ini untuk kami padahal kami tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami. Ya Allah, kami mohon kepada-Mu dalam perjalanan ini kebaikan dan ketakwaan, dan amal yang Engkau ridhai. Ya Allah, mudahkanlah perjalanan kami ini dan dekatkanlah jaraknya yang jauh.",
        "https://pastiumrah.com/wp-content/uploads/doa/08-safar.mp3"
    ),
    Doa(
        "Doa Naik Kendaraan",
        "بِسْمِ اللّٰهِ، الْحَمْدُ لِلّٰهِ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ",
        "Bismillaah, alhamdulillaah, subhaanalladzii sakhkhara lanaa haadzaa wa maa kunnaa lahu muqriniin, wa innaa ilaa rabbinaa lamunqalibuun",
        "Dengan nama Allah, segala puji bagi Allah. Maha Suci Allah yang telah menundukkan kendaraan ini untuk kami padahal kami sebelumnya tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami.",
        "https://pastiumrah.com/wp-content/uploads/doa/09-naik-kendaraan.mp3"
    )
)

private data class ManasikStep(val judul: String, val isi: String)

private val PANDUAN_MANASIK = listOf(
    ManasikStep("1. Ihram", "Mandi sunnah, kenakan pakaian ihram (kain putih tanpa jahitan untuk pria), niat umrah dari miqat, lalu perbanyak talbiyah selama perjalanan menuju Makkah. Sejak ihram, hindari larangan ihram: memotong kuku/rambut, memakai wangi-wangian, berhubungan suami-istri, dan bertengkar/berkata kotor."),
    ManasikStep("2. Tawaf", "Mengelilingi Ka'bah 7 kali putaran berlawanan arah jarum jam, dimulai dan diakhiri sejajar Hajar Aswad. Perbanyak doa dan dzikir selama tawaf, terutama di antara Rukun Yamani dan Hajar Aswad."),
    ManasikStep("3. Shalat di Belakang Maqam Ibrahim", "Setelah tawaf, shalat sunnah 2 rakaat di belakang/dekat Maqam Ibrahim (kalau ramai, boleh di area mana saja dalam Masjidil Haram). Rakaat pertama: setelah Al-Fatihah, membaca Surat Al-Kafirun. Rakaat kedua: setelah Al-Fatihah, membaca Surat Al-Ikhlas."),
    ManasikStep("4. Sa'i", "Berjalan/berlari kecil antara Bukit Shafa dan Marwah sebanyak 7 kali (dihitung Shafa ke Marwah = 1 kali)."),
    ManasikStep("5. Tahallul", "Mencukur habis rambut (bagi pria, lebih utama) atau memendekkan rambut (minimal seukuran ruas jari). Setelah tahallul, seluruh larangan ihram menjadi halal kembali - umrah selesai."),
)

@Composable
fun DoaManasikScreen() {
    var tab by remember { mutableStateOf(0) } // 0 = Doa, 1 = Manasik

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Doa & Panduan Manasik", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Bisa diakses kapan saja, tanpa perlu login", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegmentButton("Kumpulan Doa", tab == 0, Modifier.weight(1f)) { tab = 0 }
            SegmentButton("Panduan Manasik", tab == 1, Modifier.weight(1f)) { tab = 1 }
        }
        Spacer(Modifier.height(12.dp))

        if (tab == 0) DoaList() else ManasikList()
    }
}

@Composable
private fun SegmentButton(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) BrandGold else PanelColor,
            contentColor = if (active) BrandGreen else Muted
        )
    ) { Text(label, fontSize = 12.5.sp) }
}

@Composable
private fun DoaList() {
    var playingIndex by remember { mutableStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    fun stopPlaying() {
        mediaPlayer?.release(); mediaPlayer = null; playingIndex = -1
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(DAFTAR_DOA) { index, doa ->
            Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(doa.judul, color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        IconButtonPlay(
                            playing = playingIndex == index,
                            onClick = {
                                errorMsg = null
                                if (playingIndex == index) {
                                    stopPlaying()
                                } else {
                                    stopPlaying()
                                    try {
                                        val mp = MediaPlayer()
                                        mp.setDataSource(doa.audioUrl)
                                        mp.setOnPreparedListener { it.start() }
                                        mp.setOnCompletionListener { playingIndex = -1 }
                                        mp.setOnErrorListener { _, _, _ -> errorMsg = "Audio belum tersedia untuk doa ini."; playingIndex = -1; true }
                                        mp.prepareAsync()
                                        mediaPlayer = mp
                                        playingIndex = index
                                    } catch (e: Exception) {
                                        errorMsg = "Audio belum tersedia untuk doa ini."
                                    }
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(doa.arab, color = BrandGoldSoft, fontSize = 18.sp, lineHeight = 30.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(doa.latin, color = Sand, fontSize = 12.5.sp, fontStyle = FontStyle.Italic)
                    Spacer(Modifier.height(6.dp))
                    Text(doa.terjemahan, color = Muted, fontSize = 12.sp)
                }
            }
        }
        item {
            errorMsg?.let { Text(it, color = Danger, fontSize = 11.5.sp, modifier = Modifier.padding(top = 4.dp)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun IconButtonPlay(playing: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).background(if (playing) Danger else BrandGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Text(if (playing) "⏸" else "▶", color = BrandGreen, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ManasikList() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(PANDUAN_MANASIK) { step ->
            Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text(step.judul, color = BrandGoldSoft, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(step.isi, color = Sand, fontSize = 12.5.sp, lineHeight = 18.sp)
                }
            }
        }
        item {
            Spacer(Modifier.height(6.dp))
            Text(
                "Panduan ini ringkasan umum, bukan pengganti bimbingan manasik langsung dari pembimbing IMTIYAZ.",
                color = Muted, fontSize = 11.sp
            )
        }
    }
}
