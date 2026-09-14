package com.imtiyaztour.app.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun UploadBuktiScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jamaahId by remember { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pickedUri = uri
        status = null
    }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Upload Bukti Transfer", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Foto/screenshot bukti transfer akan dikirim ke admin untuk diverifikasi", color = Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

        OutlinedTextField(
            value = jamaahId,
            onValueChange = { jamaahId = it },
            placeholder = { Text("ID Jamaah", color = Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedButton(onClick = { pickImage.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(if (pickedUri == null) "Pilih Foto Bukti Transfer" else "Ganti Foto", color = BrandGoldSoft)
        }

        pickedUri?.let {
            Spacer(Modifier.height(10.dp))
            Text("File dipilih ✓", color = SafeColor, fontSize = 12.5.sp)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val uri = pickedUri ?: return@Button
                if (jamaahId.isBlank()) return@Button
                loading = true; status = "Mengunggah..."
                scope.launch {
                    try {
                        val file = copyUriToCacheFile(context, uri)
                        val result = ImtiyazApi.service.uploadBukti(
                            ImtiyazApi.textPart(jamaahId.trim()),
                            ImtiyazApi.buktiPart(file)
                        )
                        file.delete()
                        status = if (result.success == true)
                            "Terkirim ✓ — status: ${result.status ?: "Menunggu Verifikasi"}"
                        else
                            "Gagal: ${result.error ?: "tidak diketahui"}"
                    } catch (e: Exception) {
                        status = "Gagal mengunggah — periksa koneksi."
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = pickedUri != null && jamaahId.isNotBlank() && !loading,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Mengunggah..." else "Kirim Bukti Transfer", color = BrandGreen)
        }

        status?.let {
            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(10.dp)) {
                Text(it, color = Sand, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }
    }
}

private fun copyUriToCacheFile(context: android.content.Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, "bukti_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { output -> input.copyTo(output) }
    input.close()
    return file
}
