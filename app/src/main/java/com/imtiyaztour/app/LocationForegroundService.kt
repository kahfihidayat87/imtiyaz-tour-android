package com.imtiyaztour.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service GPS tracking sesuai spesifikasi backend:
 * - update tiap 30 detik
 * - kirim lat/lng/battery/accuracy ke POST /api/update-location
 * - HANYA aktif kalau posisi ada di dalam wilayah Arab Saudi (bounding box kasar),
 *   supaya tidak boros baterai & kuota jamaah saat masih di Indonesia
 */
class LocationForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "gps_tracking_channel"
        const val NOTIF_ID = 1001
        const val EXTRA_JAMAAH_ID = "jamaah_id"
        private const val INTERVAL_MS = 30_000L

        // Bounding box kasar wilayah Arab Saudi
        private const val SAUDI_LAT_MIN = 16.0
        private const val SAUDI_LAT_MAX = 32.5
        private const val SAUDI_LNG_MIN = 34.5
        private const val SAUDI_LNG_MAX = 55.7

        fun isInSaudiArabia(lat: Double, lng: Double): Boolean =
            lat in SAUDI_LAT_MIN..SAUDI_LAT_MAX && lng in SAUDI_LNG_MIN..SAUDI_LNG_MAX
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedClient: FusedLocationProviderClient
    private var jamaahId: String = "unknown"
    private var callback: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        jamaahId = intent?.getStringExtra(EXTRA_JAMAAH_ID) ?: "unknown"
        startForeground(NOTIF_ID, buildNotification("Memantau lokasi untuk keamanan jamaah"))
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVAL_MS).build()
        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { handleNewLocation(it) }
            }
        }
        fusedClient.requestLocationUpdates(request, callback as LocationCallback, mainLooper)
    }

    private fun handleNewLocation(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        if (!isInSaudiArabia(lat, lng)) {
            // Sesuai spesifikasi: GPS hanya aktif di Tanah Suci, tidak kirim kalau di luar area itu.
            return
        }
        val battery = getBatteryPercent()
        scope.launch {
            try {
                ImtiyazApi.service.updateLocation(
                    UpdateLocationRequest(
                        jamaah_id = jamaahId,
                        lat = lat,
                        lng = lng,
                        gps_consent = true,
                        battery = battery,
                        accuracy = location.accuracy
                    )
                )
            } catch (e: Exception) {
                // Gagal kirim satu update tidak fatal — akan dicoba lagi di siklus berikutnya.
            }
        }
    }

    private fun getBatteryPercent(): Int {
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Imtiyaz Tour")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "GPS Tracking Jamaah", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        callback?.let { fusedClient.removeLocationUpdates(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
