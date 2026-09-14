package com.imtiyaztour.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*
import com.imtiyaztour.app.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().background(BrandGreen).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_imtiyaz),
            contentDescription = "Logo Imtiyaz Tour",
            modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Imtiyaz Tour", color = Sand, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            "Masuk sebagai jamaah terdaftar", color = Muted, fontSize = 13.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp, bottom = 28.dp)
        )

        OutlinedTextField(
            value = username, onValueChange = { username = it; errorMsg = null },
            label = { Text("Username", color = Muted) }, singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it; errorMsg = null },
            label = { Text("Password", color = Muted) }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) { errorMsg = "Isi username dan password."; return@Button }
                loading = true; errorMsg = null
                scope.launch {
                    try {
                        val res = ImtiyazApi.service.login(LoginRequest(username.trim(), password))
                        if (res.success == true && res.jamaah_id != null) {
                            Session.save(context, res.jamaah_id, res.nama ?: "", res.akses_keuangan, res.akses_dokumen)
                            try {
                                val token = com.google.firebase.messaging.FirebaseMessaging.getInstance().token.await()
                                ImtiyazFcmService.registerToken(res.jamaah_id, token)
                            } catch (e: Exception) { /* token FCM opsional, tidak menggagalkan login */ }
                            onLoginSuccess()
                        } else {
                            errorMsg = "Username atau password salah."
                        }
                    } catch (e: Exception) {
                        errorMsg = "Username/password salah, atau akun belum dikonfirmasi admin."
                    } finally { loading = false }
                }
            },
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Memeriksa..." else "Masuk", color = BrandGreen, fontWeight = FontWeight.Bold)
        }

        errorMsg?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = Danger, fontSize = 12.5.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Belum punya akun? Hubungi admin setelah pendaftaran umrah untuk mendapatkan username & password.",
            color = Muted, fontSize = 11.5.sp, modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
