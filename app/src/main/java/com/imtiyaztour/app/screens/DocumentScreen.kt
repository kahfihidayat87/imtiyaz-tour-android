package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*

@Composable
fun DocumentScreen(jamaahId: String) {
    var dokumen by remember { mutableStateOf<DokumenStatus?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(jamaahId) {
        try {
            val res = ImtiyazApi.service.getJamaahStatus(jamaahId)
            dokumen = res.dokumen ?: DokumenStatus()
        } catch (e: Exception) {
            errorMsg = "Gagal memuat data dokumen - periksa koneksi."
        } finally { loading = false }
    }

    val items = dokumen?.let {
        listOf(
            "KTP" to it.ktp, "Kartu Keluarga (KK)" to it.kk, "Paspor" to it.paspor,
            "Foto" to it.foto, "Buku Nikah" to it.buku_nikah, "Vaksin Meningitis" to it.vaksin_meningitis
        )
    } ?: emptyList()
    val lengkapCount = items.count { it.second }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Kelengkapan Dokumen", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Dicek dan diperbarui oleh admin", color = Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

        when {
            loading -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandGold)
            }
            errorMsg != null -> Text(errorMsg!!, color = Danger, fontSize = 13.sp)
            else -> {
                Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("$lengkapCount dari ${items.size} dokumen lengkap", color = BrandGoldSoft, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                items.forEach { (label, done) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Box(
                            Modifier.size(22.dp).background(if (done) SafeColor else LineColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (done) "✓" else "-", color = Sand, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = Sand, fontSize = 14.sp)
                        Spacer(Modifier.weight(1f))
                        Text(if (done) "Lengkap" else "Belum", color = if (done) SafeColor else Muted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Kalau ada dokumen yang belum lengkap, segera hubungi admin untuk melengkapi sebelum jadwal keberangkatan.",
                    color = Muted, fontSize = 11.5.sp
                )
            }
        }
    }
}
