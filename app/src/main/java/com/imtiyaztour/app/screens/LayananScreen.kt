package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

/**
 * Halaman "Layanan" - berisi Skrining Kesehatan dan Evaluasi Pelayanan,
 * KEDUANYA sekarang native (bukan browser eksternal lagi) - datanya masuk
 * ke tabel WordPress yang sama persis dengan yang dipakai plugin form
 * masing-masing yang sudah aktif. Tidak perlu login untuk mengakses keduanya.
 */
private enum class LayananSub { HUB, SKRINING, EVALUASI, FASILITAS }

@Composable
fun LayananScreen() {
    var sub by remember { mutableStateOf(LayananSub.HUB) }

    when (sub) {
        LayananSub.SKRINING -> WithBackToLayanan({ sub = LayananSub.HUB }) { SkriningScreen() }
        LayananSub.EVALUASI -> WithBackToLayanan({ sub = LayananSub.HUB }) { EvaluasiScreen() }
        LayananSub.FASILITAS -> WithBackToLayanan({ sub = LayananSub.HUB }) { FasilitasPaketScreen() }
        LayananSub.HUB -> LayananHub(onOpen = { sub = it })
    }
}

@Composable
private fun WithBackToLayanan(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Text("< Kembali ke Layanan", color = BrandGoldSoft, fontSize = 13.sp)
        }
        content()
    }
}

@Composable
private fun LayananHub(onOpen: (LayananSub) -> Unit) {
    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Layanan", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Tidak perlu login untuk mengakses layanan ini", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

        LayananCard(
            icon = "🩺", judul = "Skrining Kesehatan",
            deskripsi = "Isi kuesioner kesehatan 29 pertanyaan sebelum keberangkatan umrah.",
            onClick = { onOpen(LayananSub.SKRINING) }
        )
        Spacer(Modifier.height(12.dp))
        LayananCard(
            icon = "🏨", judul = "Fasilitas & Akomodasi Paket",
            deskripsi = "Bandingkan hotel dan transportasi tiap paket umrah.",
            onClick = { onOpen(LayananSub.FASILITAS) }
        )
        Spacer(Modifier.height(12.dp))
        LayananCard(
            icon = "📋", judul = "Evaluasi Pelayanan",
            deskripsi = "Beri masukan tentang pelayanan IMTIYAZ selama perjalanan umrah kamu.",
            onClick = { onOpen(LayananSub.EVALUASI) }
        )
    }
}

@Composable
private fun LayananCard(icon: String, judul: String, deskripsi: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 26.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(judul, color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(deskripsi, color = Muted, fontSize = 12.sp)
            }
            Text("›", color = BrandGoldSoft, fontSize = 20.sp)
        }
    }
}
