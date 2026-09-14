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
import kotlinx.coroutines.launch

@Composable
fun StatusScreen() {
    val scope = rememberCoroutineScope()
    var jamaahId by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<JamaahStatus?>(null) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Status Pembayaran", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Cek status Lunas / Belum Lunas berdasarkan ID jamaah", color = Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

        OutlinedTextField(
            value = jamaahId,
            onValueChange = { jamaahId = it },
            placeholder = { Text("Masukkan ID Jamaah", color = Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                if (jamaahId.isBlank()) return@Button
                loading = true; errorMsg = null; result = null
                scope.launch {
                    try {
                        result = ImtiyazApi.service.getJamaahStatus(jamaahId.trim())
                    } catch (e: Exception) {
                        errorMsg = "Jamaah tidak ditemukan atau gagal terhubung."
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = jamaahId.isNotBlank() && !loading,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold)
        ) {
            Text(if (loading) "Memeriksa..." else "Cek Status", color = BrandGreen)
        }

        Spacer(Modifier.height(18.dp))

        errorMsg?.let { Text(it, color = Danger, fontSize = 13.sp) }

        result?.let { r ->
            Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    r.nama?.let { Text(it, color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                    Spacer(Modifier.height(8.dp))
                    val statusColor = if (r.status?.contains("Belum", ignoreCase = true) == true) Danger else SafeColor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).background(statusColor, androidx.compose.foundation.shape.CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(r.status ?: "Status tidak diketahui", color = Sand, fontSize = 14.sp)
                    }
                    r.sisa_tagihan?.let {
                        Spacer(Modifier.height(6.dp))
                        Text("Sisa tagihan: $it", color = Muted, fontSize = 13.sp)
                    }
                    r.error?.let { Text(it, color = Danger, fontSize = 13.sp) }
                }
            }
        }
    }
}
