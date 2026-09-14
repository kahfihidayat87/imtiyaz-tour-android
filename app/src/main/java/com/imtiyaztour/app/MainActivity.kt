package com.imtiyaztour.app

import android.os.Bundle
import android.os.Build
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.imtiyaztour.app.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = BrandGreen, surface = PanelColor)) {
                RootApp()
            }
        }
    }
}

// 4 tab utama - Beranda, Doa & Manasik, Layanan TIDAK perlu login.
// Akun berisi Login (kalau belum masuk) atau hub fitur privat (Status/Bukti/GPS-SOS/Dokumen).
private enum class Tab(val label: String) { BERANDA("Beranda"), DOA("Doa & Manasik"), LAYANAN("Layanan"), AKUN("Akun") }

@Composable
fun RootApp() {
    val context = LocalContext.current

    val notifPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var tab by remember { mutableStateOf(Tab.BERANDA) }
    // dibaca ulang tiap kali tab Akun dikunjungi/berubah, supaya sapaan di Beranda
    // ikut update begitu user login/logout dari tab Akun.
    var sessionVersion by remember { mutableStateOf(0) }
    val namaJamaah = remember(sessionVersion) { Session.nama(context) }

    Scaffold(
        containerColor = CreamBg,
        bottomBar = {
            NavigationBar(containerColor = BrandGreen) {
                Tab.values().forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t; sessionVersion++ },
                        icon = { Icon(iconFor(t), contentDescription = null) },
                        label = { Text(t.label, maxLines = 1) },
                        colors = navColors()
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                Tab.BERANDA -> PaketScreen(
                    namaJamaah = namaJamaah,
                    onLogout = { Session.logout(context); sessionVersion++ },
                    onOpenAkun = { tab = Tab.AKUN }
                )
                Tab.DOA -> DoaManasikScreen()
                Tab.LAYANAN -> LayananScreen()
                Tab.AKUN -> AkunScreen()
            }
        }
    }
}

private fun iconFor(t: Tab) = when (t) {
    Tab.BERANDA -> Icons.Filled.Home
    Tab.DOA -> Icons.Filled.MenuBook
    Tab.LAYANAN -> Icons.Filled.Description
    Tab.AKUN -> Icons.Filled.Person
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandGreen,
    selectedTextColor = BrandGold,
    indicatorColor = BrandGold,
    unselectedIconColor = Muted,
    unselectedTextColor = Muted
)
