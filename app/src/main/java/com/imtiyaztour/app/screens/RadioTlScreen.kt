package com.imtiyaztour.app.screens

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*
import kotlinx.coroutines.*
import java.io.File

/**
 * Radio TL: TL bicara lewat tombol tekan-bicara, jamaah di channel (kode
 * rombongan) yang sama dengar otomatis. Ini pengganti alat tour-guide fisik
 * yang kadang dilarang dibawa masuk Masjidil Haram - HP jamaah tetap boleh.
 * Perlu login karena ini fitur operasional untuk jamaah yang sedang berjalan.
 */
@Composable
fun RadioTlScreen() {
    var channelCode by remember { mutableStateOf("") }
    var isTl by remember { mutableStateOf(true) }

    fun cleanCode(raw: String) = raw.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(12)

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Radio TL", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Dengarkan suara TL langsung dari HP, tanpa alat tambahan", color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

        OutlinedTextField(
            value = channelCode,
            onValueChange = { channelCode = cleanCode(it) },
            placeholder = { Text("Kode Rombongan, mis. BUS-07", color = Muted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
                focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text("TL menentukan kode ini dan mengumumkannya ke jamaah sebelum berangkat.", color = Muted, fontSize = 10.5.sp)
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RoleBtn("🎙️ Mode TL", isTl, Modifier.weight(1f)) { isTl = true }
            RoleBtn("🎧 Mode Jamaah", !isTl, Modifier.weight(1f)) { isTl = false }
        }
        Spacer(Modifier.height(16.dp))

        if (isTl) TlView(channelCode) else JamaahView(channelCode)
    }
}

@Composable
private fun RoleBtn(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) BrandGold.copy(alpha = 0.18f) else PanelColor,
            contentColor = if (active) Sand else Muted
        )
    ) { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
private fun TlView(channelCode: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRecording by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Isi kode rombongan untuk mulai bicara") }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }

    fun startRecording() {
        if (channelCode.isBlank()) return
        val file = File(context.cacheDir, "radiotl_${System.currentTimeMillis()}.m4a")
        outputFile = file
        try {
            val r = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = r
            isRecording = true
            status = "Sedang bicara..."
        } catch (e: Exception) {
            status = "Gagal mengakses mikrofon: ${e.message}"
        }
    }

    fun stopAndSend() {
        isRecording = false
        try {
            recorder?.apply { stop(); release() }
        } catch (e: Exception) { }
        recorder = null
        val file = outputFile ?: return
        status = "Mengirim..."
        scope.launch {
            try {
                val bytes = file.readBytes()
                file.delete()
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                ImtiyazApi.service.radioBroadcast(RadioBroadcastRequest(channelCode, "audio/mp4", b64))
                status = "Terkirim ✓ ke channel $channelCode"
            } catch (e: Exception) {
                status = "Gagal mengirim - periksa koneksi."
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(if (isRecording) Danger.copy(alpha = 0.25f) else PanelColor, CircleShape)
                .pointerInput(channelCode) {
                    detectTapGestures(
                        onPress = {
                            if (channelCode.isNotBlank()) {
                                startRecording()
                                val released = tryAwaitRelease()
                                if (released) stopAndSend()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isRecording) "🎙️\nBICARA..." else "🎙️\nTAHAN UNTUK\nBICARA",
                color = Sand, textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(status, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun JamaahView(channelCode: String) {
    val context = LocalContext.current
    var listening by remember { mutableStateOf(false) }
    var liveText by remember { mutableStateOf("Belum terhubung") }
    var liveOk by remember { mutableStateOf(false) }
    var lastPlayedId by remember { mutableStateOf(0) }
    val queue = remember { mutableStateListOf<RadioClip>() }
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var pollJob by remember { mutableStateOf<Job?>(null) }

    fun processQueue() {
        if (isPlaying || queue.isEmpty()) return
        val clip = queue.removeAt(0)
        val bytes = try { Base64.decode(clip.audio, Base64.NO_WRAP) } catch (e: Exception) { return }
        val tmp = File(context.cacheDir, "clip_${clip.id}.tmp")
        tmp.writeBytes(bytes)
        try {
            val mp = MediaPlayer()
            mp.setDataSource(tmp.absolutePath)
            mp.setOnCompletionListener {
                tmp.delete(); it.release(); isPlaying = false
                liveText = "Tersambung, menunggu siaran..."
                processQueue()
            }
            mp.setOnPreparedListener { it.start() }
            mp.prepare()
            mediaPlayer = mp
            isPlaying = true
            liveText = "Menerima siaran..."
        } catch (e: Exception) {
            tmp.delete(); isPlaying = false
        }
    }

    fun stopListening() {
        pollJob?.cancel(); pollJob = null
        listening = false; liveOk = false; liveText = "Belum terhubung"
    }

    DisposableEffect(channelCode) { onDispose { stopListening(); mediaPlayer?.release() } }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(if (liveOk) SafeColor else Muted, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(liveText, color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                if (listening) { stopListening(); return@Button }
                if (channelCode.isBlank()) return@Button
                listening = true
                liveText = "Menyambungkan..."
                lastPlayedId = 0
                pollJob = CoroutineScope(Dispatchers.Main).launch {
                    var fails = 0
                    while (isActive) {
                        try {
                            val res = ImtiyazApi.service.radioHistory(channelCode)
                            fails = 0; liveOk = true
                            if (liveText.startsWith("Koneksi") || liveText.startsWith("Menyambungkan")) liveText = "Tersambung, menunggu siaran..."
                            val baru = res.history.filter { (it.id ?: 0) > lastPlayedId }.sortedBy { it.id }
                            if (baru.isNotEmpty()) {
                                lastPlayedId = baru.mapNotNull { it.id }.maxOrNull() ?: lastPlayedId
                                queue.addAll(baru)
                                processQueue()
                            }
                        } catch (e: Exception) {
                            fails++
                            if (fails >= 2) { liveOk = false; liveText = "Koneksi terputus, mencoba lagi..." }
                        }
                        delay(1500)
                    }
                }
            },
            enabled = channelCode.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold, disabledContainerColor = LineColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (listening) "Berhenti Dengarkan" else "Mulai Dengarkan", color = BrandGreen, fontWeight = FontWeight.Bold)
        }
    }
}
