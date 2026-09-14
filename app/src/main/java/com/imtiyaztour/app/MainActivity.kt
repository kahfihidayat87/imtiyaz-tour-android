package com.imtiyaztour.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

private enum class Tab(val label: String) { PAKET("Paket"), STATUS("Status"), BUKTI("Bukti"), GPS("GPS/SOS"), SKRINING("Skrining") }

@Composable
fun RootApp() {
    var tab by remember { mutableStateOf(Tab.PAKET) }
    var jamaahId by remember { mutableStateOf("") } // dipakai bersama layar GPS/SOS

    Scaffold(
        containerColor = BrandGreen,
        bottomBar = {
            NavigationBar(containerColor = PanelColor) {
                NavigationBarItem(
                    selected = tab == Tab.PAKET, onClick = { tab = Tab.PAKET },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) }, label = { Text(Tab.PAKET.label) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = tab == Tab.STATUS, onClick = { tab = Tab.STATUS },
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) }, label = { Text(Tab.STATUS.label) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = tab == Tab.BUKTI, onClick = { tab = Tab.BUKTI },
                    icon = { Icon(Icons.Filled.Upload, contentDescription = null) }, label = { Text(Tab.BUKTI.label) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = tab == Tab.GPS, onClick = { tab = Tab.GPS },
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }, label = { Text(Tab.GPS.label) },
                    colors = navColors()
                )
                NavigationBarItem(
                    selected = tab == Tab.SKRINING, onClick = { tab = Tab.SKRINING },
                    icon = { Icon(Icons.Filled.MedicalServices, contentDescription = null) }, label = { Text(Tab.SKRINING.label) },
                    colors = navColors()
                )
            }
        }
    ) { padding ->
        Modifier.padding(padding) // reserved, masing-masing screen sudah fillMaxSize
        when (tab) {
            Tab.PAKET -> PaketScreen()
            Tab.STATUS -> StatusScreen()
            Tab.BUKTI -> UploadBuktiScreen()
            Tab.GPS -> GpsSosScreen(jamaahId = jamaahId, onJamaahIdChange = { jamaahId = it })
            Tab.SKRINING -> SkriningScreen()
        }
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = BrandGreen,
    selectedTextColor = BrandGold,
    indicatorColor = BrandGold,
    unselectedIconColor = Muted,
    unselectedTextColor = Muted
)
