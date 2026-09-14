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

// Sumber: https://pastiumrah.com/formulir-skrining-kesehatan-calon-jamaah-umrah/
// Diambil persis dari form live di website (29 pertanyaan, kategori A-H) per 14 Sep 2026.
// Kalau admin mengubah form ini di WordPress, layar ini perlu diperbarui manual mengikuti.

private enum class QType { TEXT, TEXTAREA, SINGLE, MULTI }

private data class SQ(
    val id: String,
    val category: String,
    val text: String,
    val type: QType,
    val options: List<String> = emptyList(),
    val required: Boolean = true
)

private val QUESTIONS = listOf(
    SQ("q1", "A. Data diri & kontak darurat", "Nama lengkap calon jamaah", QType.TEXT),
    SQ("q2", "A. Data diri & kontak darurat", "Usia (tahun)", QType.TEXT),
    SQ("q3", "A. Data diri & kontak darurat", "Nama pendamping/anak yang bisa dihubungi", QType.TEXT),
    SQ("q4", "A. Data diri & kontak darurat", "Nomor HP aktif pendamping/anak", QType.TEXT),
    SQ("q5", "A. Data diri & kontak darurat", "Nama & nomor kontak pendamping yang akan ikut berangkat (isi \"Tidak ada\" jika berangkat sendiri)", QType.TEXT, required = false),
    SQ("q6", "A. Data diri & kontak darurat", "Apakah calon jamaah pernah umrah/haji sebelumnya?", QType.SINGLE,
        listOf("Belum pernah", "Pernah, tanpa kendala kesehatan", "Pernah, dengan kendala kesehatan (jelaskan di pertanyaan berikutnya)")),
    SQ("q6a", "A. Data diri & kontak darurat", "6a. Jika pernah dan ada kendala kesehatan, jelaskan singkat", QType.TEXTAREA, required = false),

    SQ("q7", "B. Riwayat penyakit & kondisi saat ini", "Apakah memiliki riwayat penyakit berikut? (boleh pilih lebih dari satu)", QType.MULTI,
        listOf(
            "Hipertensi (darah tinggi)", "Diabetes / kencing manis", "Penyakit jantung (termasuk pernah operasi/pasang ring)",
            "Stroke", "Penyakit paru (asma, PPOK, sesak napas)", "Penyakit ginjal (termasuk cuci darah/dialisis)",
            "Gangguan sendi/tulang (osteoporosis, arthritis, pernah patah tulang)", "Gangguan pendengaran/penglihatan signifikan",
            "Demensia / gangguan memori", "Tidak ada riwayat penyakit di atas"
        )),
    SQ("q7a", "B. Riwayat penyakit & kondisi saat ini", "7a. Jika ada penyakit lain yang tidak tercantum di atas, sebutkan", QType.TEXT, required = false),
    SQ("q8", "B. Riwayat penyakit & kondisi saat ini", "Apakah sedang dalam pengobatan rutin? Sebutkan nama obat dan dosisnya (isi \"Tidak ada\" jika tidak ada)", QType.TEXTAREA),
    SQ("q9", "B. Riwayat penyakit & kondisi saat ini", "Apakah pernah dirawat di rumah sakit dalam 6 bulan terakhir?", QType.SINGLE,
        listOf("Tidak pernah", "Pernah (jelaskan di pertanyaan berikutnya)")),
    SQ("q9a", "B. Riwayat penyakit & kondisi saat ini", "9a. Jika pernah dirawat, untuk kondisi apa?", QType.TEXT, required = false),

    SQ("q10", "C. Kemampuan fisik & mobilitas", "Apakah calon jamaah bisa berjalan mandiri tanpa bantuan alat?", QType.SINGLE,
        listOf("Ya, sepenuhnya mandiri", "Bisa, tapi perlu pendampingan/pegangan", "Tidak, perlu alat bantu (tongkat/walker)", "Tidak bisa berjalan, perlu kursi roda")),
    SQ("q11", "C. Kemampuan fisik & mobilitas", "Berapa lama mampu berjalan/berdiri tanpa istirahat?", QType.SINGLE,
        listOf("Lebih dari 15 menit", "5-15 menit", "Kurang dari 5 menit")),
    SQ("q12", "C. Kemampuan fisik & mobilitas", "Apakah pernah jatuh dalam 1 tahun terakhir?", QType.SINGLE,
        listOf("Tidak pernah", "Pernah 1 kali", "Pernah 2 kali atau lebih")),
    SQ("q13", "C. Kemampuan fisik & mobilitas", "Apakah mampu naik-turun tangga tanpa bantuan?", QType.SINGLE,
        listOf("Ya, mampu sendiri", "Mampu, tapi perlu pegangan/bantuan", "Tidak mampu")),
    SQ("q14", "C. Kemampuan fisik & mobilitas", "Apakah mampu duduk-berdiri dari toilet/kursi rendah secara mandiri?", QType.SINGLE,
        listOf("Ya, mandiri", "Perlu bantuan ringan", "Perlu bantuan penuh")),

    SQ("q15", "D. Kemandirian sehari-hari", "Apakah mampu mandi, berpakaian, dan ke toilet secara mandiri?", QType.SINGLE,
        listOf("Ya, sepenuhnya mandiri", "Perlu bantuan sebagian", "Perlu bantuan penuh")),
    SQ("q16", "D. Kemandirian sehari-hari", "Apakah mampu makan/minum sendiri tanpa bantuan?", QType.SINGLE,
        listOf("Ya, mandiri", "Perlu bantuan")),
    SQ("q17", "D. Kemandirian sehari-hari", "Apakah memerlukan pendamping untuk mengingatkan/membantu minum obat tepat waktu?", QType.SINGLE,
        listOf("Tidak perlu, bisa mandiri", "Perlu diingatkan", "Perlu dibantu penuh")),

    SQ("q18", "E. Kondisi mental & kognitif", "Apakah mudah bingung dengan lingkungan baru atau keramaian?", QType.SINGLE,
        listOf("Tidak", "Kadang-kadang", "Sering")),
    SQ("q19", "E. Kondisi mental & kognitif", "Apakah pernah tersesat/lupa arah dalam situasi ramai sebelumnya?", QType.SINGLE,
        listOf("Tidak pernah", "Pernah 1 kali", "Pernah lebih dari 1 kali")),
    SQ("q20", "E. Kondisi mental & kognitif", "Apakah mampu mengikuti instruksi verbal sederhana dengan baik?", QType.SINGLE,
        listOf("Ya, dengan baik", "Kadang perlu diulang", "Sering tidak paham instruksi")),

    SQ("q21", "F. Kesiapan khusus ibadah fisik", "Apakah sanggup thawaf (mengelilingi Ka'bah, ±1,5 km) dengan berjalan kaki?", QType.SINGLE,
        listOf("Sanggup berjalan penuh", "Sanggup sebagian, sisanya perlu kursi roda", "Perlu kursi roda sepenuhnya")),
    SQ("q22", "F. Kesiapan khusus ibadah fisik", "Apakah sanggup sa'i (berjalan ±3,5 km bolak-balik) dengan berjalan kaki?", QType.SINGLE,
        listOf("Sanggup berjalan penuh", "Sanggup sebagian, sisanya perlu kursi roda", "Perlu kursi roda sepenuhnya")),
    SQ("q23", "F. Kesiapan khusus ibadah fisik", "Apakah memiliki riwayat sesak napas/nyeri dada saat aktivitas fisik sedang (jalan cepat/naik tangga)?", QType.SINGLE,
        listOf("Tidak pernah", "Kadang-kadang", "Sering")),

    SQ("q24", "G. Kebutuhan khusus lainnya", "Apakah ada pantangan makanan/alergi tertentu? (isi \"Tidak ada\" jika tidak ada)", QType.TEXT),
    SQ("q25", "G. Kebutuhan khusus lainnya", "Apakah memerlukan diet khusus (rendah gula, rendah garam, dll)? (isi \"Tidak ada\" jika tidak ada)", QType.TEXT),
    SQ("q26", "G. Kebutuhan khusus lainnya", "Obat-obatan pribadi yang akan dibawa selama perjalanan (sebutkan semua, isi \"Tidak ada\" jika tidak ada)", QType.TEXTAREA),
    SQ("q27", "G. Kebutuhan khusus lainnya", "Apakah memiliki BPJS/asuransi kesehatan tambahan yang masih aktif?", QType.SINGLE,
        listOf("Ya, BPJS aktif", "Ya, asuransi swasta aktif", "Keduanya aktif", "Tidak ada yang aktif")),

    SQ("q28", "H. Persetujuan & surat keterangan", "Bersedia melampirkan surat keterangan sehat dari dokter jika diminta tim IMTIYAZ?", QType.SINGLE,
        listOf("Ya, bersedia", "Sudah punya surat keterangan sehat", "Belum bersedia / perlu diskusi lebih lanjut")),
    SQ("q29", "H. Persetujuan & surat keterangan", "Pihak keluarga memahami dan menyetujui kondisi kesehatan jamaah sesuai yang dilaporkan di atas?", QType.SINGLE,
        listOf("Ya, kami setujui dan tanggung jawab penuh atas kebenaran data ini"))
)

@Composable
fun SkriningScreen() {
    val scope = rememberCoroutineScope()
    var jamaahId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val textAnswers = remember { mutableStateMapOf<String, String>() }
    val multiAnswers = remember { mutableStateMapOf<String, MutableSet<String>>() }
    var submitting by remember { mutableStateOf(false) }
    var submitStatus by remember { mutableStateOf<String?>(null) }

    val grouped = QUESTIONS.groupBy { it.category }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Skrining Kesehatan Lansia", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "29 pertanyaan asli dari formulir skrining di pastiumrah.com (8 kategori A-H).",
            color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        FieldBox(value = jamaahId, onChange = { jamaahId = it }, placeholder = "ID Jamaah")
        Spacer(Modifier.height(8.dp))
        FieldBox(value = email, onChange = { email = it }, placeholder = "Email (sesuai form asli)")
        Spacer(Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            grouped.forEach { (category, questions) ->
                item {
                    Text(category, color = BrandGoldSoft, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(top = 6.dp))
                }
                items(questions) { q ->
                    QuestionCard(
                        q = q,
                        textValue = textAnswers[q.id] ?: "",
                        onTextChange = { textAnswers[q.id] = it },
                        selectedSingle = textAnswers[q.id],
                        onSingleSelect = { textAnswers[q.id] = it },
                        selectedMulti = multiAnswers[q.id] ?: mutableSetOf(),
                        onMultiToggle = { opt ->
                            val current = multiAnswers.getOrPut(q.id) { mutableSetOf() }
                            if (current.contains(opt)) current.remove(opt) else current.add(opt)
                            multiAnswers[q.id] = current.toMutableSet()
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (jamaahId.isBlank() || submitting) return@Button
                submitting = true; submitStatus = "Mengirim skrining..."
                scope.launch {
                    try {
                        val body = mutableMapOf<String, Any>("jamaah_id" to jamaahId.trim(), "email" to email.trim())
                        textAnswers.forEach { (k, v) -> if (v.isNotBlank()) body[k] = v }
                        multiAnswers.forEach { (k, v) -> if (v.isNotEmpty()) body[k] = v.toList() }
                        val res = ImtiyazApi.service.submitSkrining(body)
                        submitStatus = if (res.success == true) "Skrining terkirim v" else "Gagal: ${res.error ?: "tidak diketahui"}"
                    } catch (e: Exception) {
                        submitStatus = "Gagal mengirim - periksa koneksi."
                    } finally { submitting = false }
                }
            },
            enabled = jamaahId.isNotBlank() && !submitting,
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (submitting) "Mengirim..." else "Kirim Skrining", color = BrandGreen)
        }
        submitStatus?.let { Spacer(Modifier.height(8.dp)); Text(it, color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun FieldBox(value: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        placeholder = { Text(placeholder, color = Muted) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = BrandGoldSoft, unfocusedTextColor = BrandGoldSoft,
            focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuestionCard(
    q: SQ,
    textValue: String,
    onTextChange: (String) -> Unit,
    selectedSingle: String?,
    onSingleSelect: (String) -> Unit,
    selectedMulti: Set<String>,
    onMultiToggle: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = PanelColor), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(q.text + if (q.required) " *" else "", color = Sand, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            when (q.type) {
                QType.TEXT -> OutlinedTextField(
                    value = textValue, onValueChange = onTextChange, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Sand, unfocusedTextColor = Sand,
                        focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                QType.TEXTAREA -> OutlinedTextField(
                    value = textValue, onValueChange = onTextChange,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Sand, unfocusedTextColor = Sand,
                        focusedBorderColor = BrandGold, unfocusedBorderColor = LineColor, cursorColor = BrandGold
                    ),
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
                QType.SINGLE -> Column {
                    q.options.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            RadioButton(
                                selected = selectedSingle == opt, onClick = { onSingleSelect(opt) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandGold, unselectedColor = Muted)
                            )
                            Text(opt, color = Sand, fontSize = 12.5.sp)
                        }
                    }
                }
                QType.MULTI -> Column {
                    q.options.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = selectedMulti.contains(opt), onCheckedChange = { onMultiToggle(opt) },
                                colors = CheckboxDefaults.colors(checkedColor = BrandGold)
                            )
                            Text(opt, color = Sand, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}
