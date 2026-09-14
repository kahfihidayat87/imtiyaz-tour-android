package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

private enum class AkunSub { HUB, STATUS, BUKTI, GPS, DOKUMEN }

@Composable
fun AkunScreen() {
    val context = LocalContext.current
    var loggedIn by remember { mutableStateOf(Session.isLoggedIn(context)) }

    if (!loggedIn) {
        LoginScreen(onLoginSuccess = { loggedIn = true })
        return
    }

    var sub by remember { mutableStateOf(AkunSub.HUB) }
    var jamaahId by remember { mutableStateOf(Session.jamaahId(context)) }

    when (sub) {
        AkunSub.STATUS -> WithBackHeader({ sub = AkunSub.HUB }) { StatusScreen() }
        AkunSub.BUKTI -> WithBackHeader({ sub = AkunSub.HUB }) { UploadBuktiScreen() }
        AkunSub.GPS -> WithBackHeader({ sub = AkunSub.HUB }) { GpsSosScreen(jamaahId = jamaahId, onJamaahIdChange = { jamaahId = it }) }
        AkunSub.DOKUMEN -> WithBackHeader({ sub = AkunSub.HUB }) { DocumentScreen(jamaahId = Session.jamaahId(context)) }
        AkunSub.HUB -> AkunHub(
            aksesKeuangan = Session.aksesKeuangan(context),
            aksesDokumen = Session.aksesDokumen(context),
            nama = Session.nama(context),
            onOpen = { sub = it },
            onLogout = { Session.logout(context); loggedIn = false }
        )
    }
}

@Composable
private fun WithBackHeader(onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().background(BrandGreen)) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Text("< Kembali ke Akun", color = BrandGoldSoft, fontSize = 13.sp)
        }
        content()
    }
}

@Composable
private fun AkunHub(
    aksesKeuangan: Boolean, aksesDokumen: Boolean, nama: String,
    onOpen: (AkunSub) -> Unit, onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).background(BrandGold, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 20.sp) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Akun Saya", color = Muted, fontSize = 11.sp)
                Text(nama.ifBlank { "Jamaah" }, color = Sand, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))

        if (aksesKeuangan) HubCard("💳", "Status Pembayaran", "Cek Lunas/Belum Lunas & sisa tagihan") { onOpen(AkunSub.STATUS) }
        Spacer(Modifier.height(10.dp))
        HubCard("📤", "Upload Bukti Transfer", "Kirim bukti pembayaran ke admin") { onOpen(AkunSub.BUKTI) }
        Spacer(Modifier.height(10.dp))
        HubCard("📍", "GPS & SOS", "Tracking lokasi dan tombol darurat") { onOpen(AkunSub.GPS) }
        if (aksesDokumen) {
            Spacer(Modifier.height(10.dp))
            HubCard("📄", "Kelengkapan Dokumen", "Cek status KTP, paspor, dan dokumen lain") { onOpen(AkunSub.DOKUMEN) }
        }

        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = onLogout,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
            border = androidx.compose.foundation.BorderStroke(1.dp, Danger),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚪  Keluar", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HubCard(icon: String, judul: String, deskripsi: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PanelColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(judul, color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(deskripsi, color = Muted, fontSize = 11.5.sp)
            }
            Text("›", color = BrandGoldSoft, fontSize = 20.sp)
        }
    }
}
