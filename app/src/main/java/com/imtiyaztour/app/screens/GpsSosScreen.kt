package com.imtiyaztour.app.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.imtiyaztour.app.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val MECCA = GeoPoint(21.4225, 39.8262)

@Composable
fun GpsSosScreen(jamaahId: String, onJamaahIdChange: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var consentGiven by remember { mutableStateOf(false) }
    var tracking by remember { mutableStateOf(false) }
    var jamaahLive by remember { mutableStateOf<List<JamaahLive>>(emptyList()) }
    var sosStatus by remember { mutableStateOf<String?>(null) }
    var sosSending by remember { mutableStateOf(false) }

    val permissions = remember {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    var permsGranted by remember {
        mutableStateOf(permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permsGranted = result.values.all { it }
    }

    // Polling live map tiap 15 detik selagi layar ini aktif
    LaunchedEffect(Unit) {
        while (true) {
            try { jamaahLive = ImtiyazApi.service.getJamaahLive() } catch (_: Exception) { }
            delay(15_000)
        }
    }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("GPS & SOS", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Lokasi hanya dikirim ke admin selama berada di Tanah Suci", color = Muted, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

        OutlinedTextField(
            value = jamaahId,
            onValueChange = onJamaahIdChange,
            placeholder = { Text("ID Jamaah kamu", color = Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Checkbox(
                        checked = consentGiven,
                        onCheckedChange = { consentGiven = it },
                        colors = CheckboxDefaults.colors(checkedColor = BrandGold)
                    )
                    Text(
                        "Saya menyetujui lokasi saya dipantau admin/muthowif selama perjalanan umrah, untuk keamanan jika saya tersesat.",
                        color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (!permsGranted) { permLauncher.launch(permissions.toTypedArray()); return@Button }
                        tracking = !tracking
                        val intent = Intent(context, LocationForegroundService::class.java)
                            .putExtra(LocationForegroundService.EXTRA_JAMAAH_ID, jamaahId.ifBlank { "unknown" })
                        if (tracking) {
                            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
                        } else {
                            context.stopService(intent)
                        }
                    },
                    enabled = consentGiven && jamaahId.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tracking) Danger else BrandGold,
                        disabledContainerColor = LineColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (tracking) "Hentikan Tracking" else "Aktifkan Tracking GPS", color = if (tracking) Sand else BrandGreen)
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                if (jamaahId.isBlank() || sosSending) return@Button
                sosSending = true; sosStatus = "Mengirim SOS..."
                val fused = LocationServices.getFusedLocationProviderClient(context)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fused.lastLocation.addOnSuccessListener { loc: Location? ->
                        val lat = loc?.latitude ?: MECCA.latitude
                        val lng = loc?.longitude ?: MECCA.longitude
                        scope.launch {
                            try {
                                val res = ImtiyazApi.service.sendSos(SosRequest(jamaahId.trim(), lat, lng, "Jamaah menekan tombol SOS — butuh bantuan segera"))
                                sosStatus = if (res.success == true) "SOS terkirim ✓ admin & muthowif telah diberi tahu" else "Gagal mengirim SOS"
                            } catch (e: Exception) {
                                sosStatus = "Gagal mengirim SOS — periksa koneksi."
                            } finally { sosSending = false }
                        }
                    }.addOnFailureListener { sosSending = false; sosStatus = "Gagal mengambil lokasi." }
                } else {
                    permLauncher.launch(permissions.toTypedArray())
                    sosSending = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Danger),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (sosSending) "Mengirim..." else "🆘  SOS — Saya Tersesat", color = Sand, fontWeight = FontWeight.Bold)
        }
        sosStatus?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Muted, fontSize = 12.sp) }

        Spacer(Modifier.height(16.dp))
        Text("Peta Live Jamaah", color = Sand, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        AndroidView(
            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
            factory = { ctx ->
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)
                    controller.setCenter(MECCA)
                }
            },
            update = { mapView ->
                mapView.overlays.clear()
                jamaahLive.forEach { j ->
                    val lat = j.lat; val lng = j.lng
                    if (lat != null && lng != null) {
                        val marker = Marker(mapView)
                        marker.position = GeoPoint(lat, lng)
                        marker.title = j.nama ?: j.jamaah_id ?: "Jamaah"
                        mapView.overlays.add(marker)
                    }
                }
                mapView.invalidate()
            }
        )
    }
}
