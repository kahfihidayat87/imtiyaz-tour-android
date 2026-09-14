package com.imtiyaztour.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val CHANNEL_ID = "imtiyaz_notifikasi"

class ImtiyazFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token baru (mis. app baru diinstal ulang) - daftarkan ulang KALAU sedang login.
        if (Session.isLoggedIn(this)) {
            registerToken(Session.jamaahId(this), token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val judul = message.notification?.title ?: message.data["title"] ?: "Imtiyaz Tour"
        val isi = message.notification?.body ?: message.data["body"] ?: ""
        tampilkanNotifikasi(judul, isi)
    }

    private fun tampilkanNotifikasi(judul: String, isi: String) {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Notifikasi Imtiyaz Tour", NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(judul)
            .setContentText(isi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(System.currentTimeMillis().toInt(), notif)
    }

    companion object {
        /** Kirim token FCM saat ini ke backend, dikaitkan ke jamaah_id yang sedang login. */
        fun registerToken(jamaahId: String, token: String) {
            if (jamaahId.isBlank()) return
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ImtiyazApi.service.registerFcmToken(FcmTokenRequest(jamaahId, token))
                } catch (e: Exception) {
                    // Gagal daftar token tidak fatal - notifikasi kali ini saja tidak sampai,
                    // akan dicoba lagi saat token berikutnya diperbarui atau login ulang.
                }
            }
        }
    }
}
