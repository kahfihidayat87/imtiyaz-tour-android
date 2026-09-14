package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

/**
 * Konten statis (tidak perlu API) - diambil dari materi promosi "Akomodasi Paket"
 * yang dikonfirmasi Kahfi. Kalau akomodasi/hotel berubah di kemudian hari, edit
 * langsung daftar PAKET_FASILITAS di bawah ini.
 */
private data class FasilitasPaket(
    val icon: String,
    val nama: String,
    val subjudul: String,
    val poin: List<String>,
    val catatanAdmin: String? = null // khusus Kamulyan: fasilitas lain belum dirilis publik
)

private val PAKET_FASILITAS = listOf(
    FasilitasPaket(
        "🟢", "Paket Slamet", "Ekonomis",
        listOf(
            "Hotel Madinah: Maysan Taqwa / Hayah Plaza atau setaraf — bintang 3",
            "Hotel Makkah: Al Massa Badr atau setaraf — bintang 3",
            "Transportasi Jogja–Jakarta PP: Bus",
        )
    ),
    FasilitasPaket(
        "🌿", "Paket Ayem Tentrem", "Hemat",
        listOf(
            "Hotel Madinah: Hayah Golden / Maysan Taqwa atau setaraf — bintang 3",
            "Hotel Makkah: Nada Ajyad / Emaar Andalusia — bintang 3",
            "Transportasi Jogja–Jakarta PP: Pesawat",
        )
    ),
    FasilitasPaket(
        "⭐", "Paket Linuwih", "Reguler",
        listOf(
            "Hotel Madinah: Maysan Rehab Al Misk atau setaraf — bintang 3",
            "Hotel Makkah: Mira Ajyad / Royal Majestic atau setaraf — bintang 4",
            "Pesawat Jakarta–Saudi PP: tanpa transit",
            "Transportasi Jogja–Jakarta PP: Pesawat",
        )
    ),
    FasilitasPaket(
        "👑", "Paket Kamulyan", "Nyaman Lansia (Premium)",
        listOf(
            "Seluruh fasilitas Paket Linuwih, ditambah:",
            "Hotel Makkah & Madinah: naik ke bintang 5",
            "Pesawat: langsung Jakarta → Madinah (tanpa transit)",
            "Transportasi Jogja–Jakarta PP: Pesawat",
        ),
        catatanAdmin = "Ada beberapa fasilitas tambahan lain di paket ini yang belum dirilis ke materi publik."
    ),
)

@Composable
fun FasilitasPaketScreen() {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Fasilitas & Akomodasi Paket", color = Sand, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "Perbandingan hotel dan transportasi tiap paket umrah",
            color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(PAKET_FASILITAS) { paket ->
                FasilitasCard(paket) {
                    bukaWhatsapp(context, "Assalamu'alaikum, saya ingin tanya detail lengkap fasilitas ${paket.nama}.")
                }
            }
        }
    }
}

@Composable
private fun FasilitasCard(paket: FasilitasPaket, onTanyaAdmin: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(PanelColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(paket.icon, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(paket.nama, color = Sand, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(paket.subjudul, color = BrandGoldSoft, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        paket.poin.forEach { line ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("• ", color = BrandGold, fontSize = 12.sp)
                Text(line, color = Sand, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        paket.catatanAdmin?.let { catatan ->
            Spacer(Modifier.height(10.dp))
            Text(catatan, color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF25D366), RoundedCornerShape(9.dp))
                    .clickable(onClick = onTanyaAdmin)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("💬 Tanya Detail Lengkap via WA", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
