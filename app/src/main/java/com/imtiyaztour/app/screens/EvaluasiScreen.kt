package com.imtiyaztour.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imtiyaztour.app.*
import kotlinx.coroutines.launch

// PENTING: id di setiap EQ() di bawah adalah NAMA KOLOM ASLI di tabel
// wp_imtiyaz_evaluasi_umrah milik plugin "IMTIYAZ - Formulir Evaluasi Umrah"
// yang sudah aktif di pastiumrah.com. Jangan ubah nilai id tanpa cek ulang
// plugin PHP itu (imtiyaz-form-evaluasi.php).

private enum class EType { TEXT, TEXTAREA, SINGLE }
private data class EQ(
    val id: String, val category: String, val text: String, val type: EType,
    val options: List<String> = emptyList(), val required: Boolean = true
)

private val SKALA_4 = listOf("Kurang", "Cukup", "Baik", "Baik Sekali")
private val YA_TIDAK = listOf("Tidak", "Iya")

private val EVAL_QUESTIONS = listOf(
    EQ("nama_lengkap", "Data Diri", "Nama Lengkap", EType.TEXT),
    EQ("alamat", "Data Diri", "Alamat", EType.TEXTAREA),
    EQ("no_hp", "Data Diri", "No. HP (WhatsApp)", EType.TEXT, required = false),
    EQ("tanggal_keberangkatan", "Data Diri", "Tanggal Keberangkatan (contoh: 7 November 2026)", EType.TEXT, required = false),
    EQ("nama_pembimbing", "Data Diri", "Nama Pembimbing", EType.TEXT),
    EQ("nama_muthawwif", "Data Diri", "Nama Muthawwif (Tim Pendamping 24 Jam)", EType.TEXT),

    EQ("kantor_keramahan", "Tim Kantor", "Bagaimana keramahan tim kantor kami?", EType.SINGLE, SKALA_4),
    EQ("kantor_penampilan", "Tim Kantor", "Bagaimana penampilan dan kerapian tim kantor kami?", EType.SINGLE, SKALA_4),
    EQ("kantor_respon", "Tim Kantor", "Bagaimana respon tim kantor kami dalam menanggapi keluhan, permintaan, dan/atau pemberian informasi?", EType.SINGLE, SKALA_4),

    EQ("pembimbing_keramahan", "Pembimbing", "Bagaimana keramahan Pembimbing kami?", EType.SINGLE, SKALA_4),
    EQ("pembimbing_respon_keluhan", "Pembimbing", "Bagaimanakah respon Pembimbing dalam menangani keluhan jamaah?", EType.SINGLE, SKALA_4),
    EQ("pembimbing_kedisiplinan", "Pembimbing", "Bagaimanakah kedisiplinan Pembimbing kami?", EType.SINGLE, SKALA_4),
    EQ("pembimbing_merokok", "Pembimbing", "Pernahkah Pembimbing kami merokok di depan jamaah?", EType.SINGLE, YA_TIDAK),
    EQ("pembimbing_kajian", "Pembimbing", "Apakah Pembimbing kami memberikan materi kajian/taushiyah selama program?", EType.SINGLE, YA_TIDAK),
    EQ("pembimbing_jumlah_kajian", "Pembimbing", "Seingat Anda, berapa kali kajian tersebut dilaksanakan?", EType.TEXT, required = false),

    EQ("muthawwif_keramahan", "Muthawwif", "Bagaimana keramahan Muthawwif?", EType.SINGLE, SKALA_4),
    EQ("muthawwif_kerapian", "Muthawwif", "Bagaimana kerapian pakaian Muthawwif?", EType.SINGLE, SKALA_4),
    EQ("muthawwif_respon", "Muthawwif", "Bagaimana respon Muthawwif dalam menanggapi keluhan jamaah?", EType.SINGLE, SKALA_4),
    EQ("muthawwif_merokok", "Muthawwif", "Apakah Muthawwif pernah merokok di depan jamaah?", EType.SINGLE, YA_TIDAK),
    EQ("muthawwif_kedisiplinan", "Muthawwif", "Bagaimana kedisiplinan Muthawwif selama mendampingi jamaah?", EType.SINGLE, SKALA_4),

    EQ("akomodasi_madinah", "Akomodasi", "Bagaimana akomodasi hotel Madinah (jarak dan kebersihan)?", EType.SINGLE, SKALA_4),
    EQ("catering_madinah", "Akomodasi", "Bagaimana catering hotel Madinah?", EType.SINGLE, SKALA_4),
    EQ("akomodasi_makkah", "Akomodasi", "Bagaimana akomodasi hotel Makkah (jarak dan kebersihan)?", EType.SINGLE, SKALA_4),
    EQ("catering_makkah", "Akomodasi", "Bagaimana catering hotel Makkah?", EType.SINGLE, SKALA_4),
    EQ("kualitas_umum", "Akomodasi", "Secara umum, bagaimanakah kualitas pelayanan, bimbingan, pendampingan, dan akomodasi umrah kami?", EType.SINGLE, SKALA_4),

    EQ("pesan_kesan", "Kesan & Pesan", "Tuliskan pesan dan kesan Anda", EType.TEXTAREA, required = false),
    EQ("komentar_tambahan", "Kesan & Pesan", "Komentar tambahan (opsional)", EType.TEXTAREA, required = false),
)

@Composable
fun EvaluasiScreen() {
    val scope = rememberCoroutineScope()
    val answers = remember { mutableStateMapOf<String, String>() }
    var submitting by remember { mutableStateOf(false) }
    var submitStatus by remember { mutableStateOf<String?>(null) }
    var submitErrorDetail by remember { mutableStateOf<String?>(null) }

    val grouped = EVAL_QUESTIONS.groupBy { it.category }

    fun requiredFilled() = EVAL_QUESTIONS.filter { it.required }.all { !(answers[it.id] ?: "").isBlank() }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Evaluasi Pelayanan", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Masukan Anda membantu kami terus memperbaiki layanan", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            grouped.forEach { (category, questions) ->
                item {
                    Text(category, color = BrandGoldSoft, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(top = 6.dp))
                }
                items(questions) { q ->
                    EvalQuestionCard(
                        q = q,
                        value = answers[q.id] ?: "",
                        onChange = { answers[q.id] = it }
                    )
                }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (submitting) return@Button
                        if (!requiredFilled()) {
                            submitStatus = "gagal"; submitErrorDetail = "Masih ada pertanyaan wajib (*) yang belum diisi."
                            return@Button
                        }
                        submitting = true
                        scope.launch {
                            try {
                                val body = mutableMapOf<String, Any>()
                                answers.forEach { (k, v) -> if (v.isNotBlank()) body[k] = v }
                                val res = ImtiyazApi.service.submitEvaluasi(body)
                                if (res.success == true) {
                                    submitStatus = "sukses"
                                } else {
                                    submitStatus = "gagal"; submitErrorDetail = res.error ?: "Tidak diketahui"
                                }
                            } catch (e: Exception) {
                                submitStatus = "gagal"; submitErrorDetail = "Gagal mengirim - periksa koneksi internet."
                            } finally { submitting = false }
                        }
                    },
                    enabled = !submitting,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (submitting) "Mengirim..." else "Kirim", color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        submitStatus?.let { status ->
            AlertDialog(
                onDismissRequest = { /* wajib tekan tombol */ },
                containerColor = PanelColor,
                title = { Text(if (status == "sukses") "Evaluasi Terkirim" else "Gagal Mengirim", color = Sand, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        if (status == "sukses") "Terima kasih atas evaluasi Anda! Masukan ini sangat berarti untuk kami."
                        else submitErrorDetail ?: "Terjadi kesalahan.",
                        color = Muted, fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (status == "sukses") answers.clear()
                        submitStatus = null
                    }) { Text("OK", color = BrandGold, fontWeight = FontWeight.Bold) }
                }
            )
        }
    }
}

@Composable
private fun EvalQuestionCard(q: EQ, value: String, onChange: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(q.text + if (q.required) " *" else "", color = Sand, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            when (q.type) {
                EType.TEXT -> OutlinedTextField(
                    value = value, onValueChange = onChange, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Sand, unfocusedTextColor = Sand,
                        focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                EType.TEXTAREA -> OutlinedTextField(
                    value = value, onValueChange = onChange,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Sand, unfocusedTextColor = Sand,
                        focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
                    ),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
                EType.SINGLE -> Column {
                    q.options.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            RadioButton(
                                selected = value == opt, onClick = { onChange(opt) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandGold, unselectedColor = Muted)
                            )
                            Text(opt, color = Sand, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}
