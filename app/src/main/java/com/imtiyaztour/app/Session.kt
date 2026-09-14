package com.imtiyaztour.app

import android.content.Context

/** Sesi login jamaah, disimpan di SharedPreferences supaya tidak perlu login ulang tiap buka app. */
object Session {
    private const val PREFS = "imtiyaz_session"

    fun save(context: Context, jamaahId: String, nama: String, aksesKeuangan: Boolean, aksesDokumen: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("jamaah_id", jamaahId)
            .putString("nama", nama)
            .putBoolean("akses_keuangan", aksesKeuangan)
            .putBoolean("akses_dokumen", aksesDokumen)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("jamaah_id", null) != null

    fun jamaahId(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("jamaah_id", "") ?: ""

    fun nama(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("nama", "") ?: ""

    fun aksesKeuangan(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("akses_keuangan", true)

    fun aksesDokumen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("akses_dokumen", true)

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
