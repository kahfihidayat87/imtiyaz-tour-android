package com.imtiyaztour.app

import android.content.Context
import android.content.Intent
import android.net.Uri

const val WA_ADMIN_NUMBER = "628112776543" // 0811-277-6543 dalam format internasional

/**
 * Buka WhatsApp untuk kirim pesan ke admin. Prioritaskan APLIKASI WhatsApp yang
 * terinstall (reguler lalu Business) - HANYA fallback ke wa.me (browser/WA Web)
 * kalau memang tidak ada satu pun yang terinstall di HP.
 */
fun bukaWhatsapp(context: Context, pesan: String, nomor: String = WA_ADMIN_NUMBER) {
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$nomor&text=" + Uri.encode(pesan))
    for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage(pkg))
            return
        } catch (e: Exception) { /* app itu tidak terinstall, coba berikutnya */ }
    }
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$nomor?text=" + Uri.encode(pesan))))
}
