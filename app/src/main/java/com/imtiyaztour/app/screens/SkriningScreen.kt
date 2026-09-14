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

// PENTING: id di setiap SQ() di bawah adalah NAMA KOLOM ASLI di tabel
// wp_imtiyaz_skrining_kesehatan milik plugin "IMTIYAZ - Formulir Skrining
// Kesehatan Jamaah" yang sudah aktif di pastiumrah.com. JANGAN ubah nilai id
// tanpa mengecek ulang plugin PHP itu (imtiyaz-form-skrining-kesehatan.php),
// karena backend menulis field ini APA ADANYA ke kolom dengan nama yang sama.

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
    SQ("nama_lengkap", "A. Data diri & kontak darurat", "1. Nama lengkap calon jamaah", QType.TEXT),
    SQ("usia", "A. Data diri & kontak darurat", "2. Usia (tahun)", QType.TEXT),
    SQ("pendamping_nama", "A. Data diri & kontak darurat", "3. Nama pendamping/anak yang bisa dihubungi", QType.TEXT),
    SQ("pendamping_hp", "A. Data diri & kontak darurat", "4. Nomor HP aktif pendamping/anak", QType.TEXT),
    SQ("pendamping_ikut", "A. Data diri & kontak darurat", "5. Nama & nomor kontak pendamping yang akan ikut berangkat (isi \"Tidak ada\" jika berangkat sendiri)", QType.TEXT, required = false),
    SQ("pernah_umrah", "A. Data diri & kontak darurat", "6. Apakah calon jamaah pernah umrah/haji sebelumnya?", QType.SINGLE,
        listOf("Belum pernah", "Pernah, tanpa kendala kesehatan", "Pernah, dengan kendala kesehatan (jelaskan di pertanyaan berikutnya)")),
    SQ("pernah_umrah_kendala", "A. Data diri & kontak darurat", "6a. Jika pernah dan ada kendala kesehatan, jelaskan singkat", QType.TEXTAREA, required = false),

    SQ("riwayat_penyakit", "B. Riwayat penyakit & kondisi saat ini", "7. Apakah memiliki riwayat penyakit berikut? (boleh pilih lebih dari satu)", QType.MULTI,
        listOf(
            "Hipertensi (darah tinggi)", "Diabetes / kencing manis", "Penyakit jantung (termasuk pernah operasi/pasang ring)",
            "Stroke", "Penyakit paru (asma, PPOK, sesak napas)", "Penyakit ginjal (termasuk cuci darah/dialisis)",
            "Gangguan sendi/tulang (osteoporosis, arthritis, pernah patah tulang)", "Gangguan pendengaran/penglihatan signifikan",
            "Demensia / gangguan memori", "Tidak ada riwayat penyakit di atas"
        )),
    SQ("riwayat_penyakit_lain", "B. Riwayat penyakit & kondisi saat ini", "7a. Jika ada penyakit lain yang tidak tercantum di atas, sebutkan", QType.TEXT, required = false),
    SQ("pengobatan_rutin", "B. Riwayat penyakit & kondisi saat ini", "8. Apakah sedang dalam pengobatan rutin? Sebutkan nama obat dan dosisnya (isi \"Tidak ada\" jika tidak ada)", QType.TEXTAREA),
    SQ("dirawat_rs", "B. Riwayat penyakit & kondisi saat ini", "9. Apakah pernah dirawat di rumah sakit dalam 6 bulan terakhir?", QType.SINGLE,
        listOf("Tidak pernah", "Pernah (jelaskan di pertanyaan berikutnya)")),
    SQ("dirawat_rs_kondisi", "B. Riwayat penyakit & kondisi saat ini", "9a. Jika pernah dirawat, untuk kondisi apa?", QType.TEXT, required = false),

    SQ("jalan_mandiri", "C. Kemampuan fisik & mobilitas", "10. Apakah calon jamaah bisa berjalan mandiri tanpa bantuan alat?", QType.SINGLE,
        listOf("Ya, sepenuhnya mandiri", "Bisa, tapi perlu pendampingan/pegangan", "Tidak, perlu alat bantu (tongkat/walker)", "Tidak bisa berjalan, perlu kursi roda")),
    SQ("durasi_jalan", "C. Kemampuan fisik & mobilitas", "11. Berapa lama mampu berjalan/berdiri tanpa istirahat?", QType.SINGLE,
        listOf("Lebih dari 15 menit", "5-15 menit", "Kurang dari 5 menit")),
    SQ("pernah_jatuh", "C. Kemampuan fisik & mobilitas", "12. Apakah pernah jatuh dalam 1 tahun terakhir?", QType.SINGLE,
        listOf("Tidak pernah", "Pernah 1 kali", "Pernah 2 kali atau lebih")),
    SQ("naik_turun_tangga", "C. Kemampuan fisik & mobilitas", "13. Apakah mampu naik-turun tangga tanpa bantuan?", QType.SINGLE,
        listOf("Ya, mampu sendiri", "Mampu, tapi perlu pegangan/bantuan", "Tidak mampu")),
    SQ("duduk_berdiri_toilet", "C. Kemampuan fisik & mobilitas", "14. Apakah mampu duduk-berdiri dari toilet/kursi rendah secara mandiri?", QType.SINGLE,
        listOf("Ya, mandiri", "Perlu bantuan ringan", "Perlu bantuan penuh")),

    SQ("mandi_mandiri", "D. Kemandirian sehari-hari", "15. Apakah mampu mandi, berpakaian, dan ke toilet secara mandiri?", QType.SINGLE,
        listOf("Ya, sepenuhnya mandiri", "Perlu bantuan sebagian", "Perlu bantuan penuh")),
    SQ("makan_mandiri", "D. Kemandirian sehari-hari", "16. Apakah mampu makan/minum sendiri tanpa bantuan?", QType.SINGLE,
        listOf("Ya, mandiri", "Perlu bantuan")),
    SQ("bantuan_obat", "D. Kemandirian sehari-hari", "17. Apakah memerlukan pendamping untuk mengingatkan/membantu minum obat tepat waktu?", QType.SINGLE,
        listOf("Tidak perlu, bisa mandiri", "Perlu diingatkan", "Perlu dibantu penuh")),

    SQ("bingung_lingkungan_baru", "E. Kondisi mental & kognitif", "18. Apakah mudah bingung dengan lingkungan baru atau keramaian?", QType.SINGLE,
        listOf("Tidak", "Kadang-kadang", "Sering")),
    SQ("pernah_tersesat", "E. Kondisi mental & kognitif", "19. Apakah pernah tersesat/lupa arah dalam situasi ramai sebelumnya?", QType.SINGLE,
        listOf("Tidak pernah", "Pernah 1 kali", "Pernah lebih dari 1 kali")),
    SQ("ikuti_instruksi", "E. Kondisi mental & kognitif", "20. Apakah mampu mengikuti instruksi verbal sederhana dengan baik?", QType.SINGLE,
        listOf("Ya, dengan baik", "Kadang perlu diulang", "Sering tidak paham instruksi")),

    SQ("sanggup_thawaf", "F. Kesiapan khusus ibadah fisik", "21. Apakah sanggup thawaf (mengelilingi Ka'bah, ±1,5 km) dengan berjalan kaki?", QType.SINGLE,
        listOf("Sanggup berjalan penuh", "Sanggup sebagian, sisanya perlu kursi roda", "Perlu kursi roda sepenuhnya")),
    SQ("sanggup_sai", "F. Kesiapan khusus ibadah fisik", "22. Apakah sanggup sa'i (berjalan ±3,5 km bolak-balik) dengan berjalan kaki?", QType.SINGLE,
        listOf("Sanggup berjalan penuh", "Sanggup sebagian, sisanya perlu kursi roda", "Perlu kursi roda sepenuhnya")),
    SQ("sesak_napas_aktivitas", "F. Kesiapan khusus ibadah fisik", "23. Apakah memiliki riwayat sesak napas/nyeri dada saat aktivitas fisik sedang (jalan cepat/naik tangga)?", QType.SINGLE,
        listOf("Tidak pernah", "Kadang-kadang", "Sering")),

    SQ("pantangan_makanan", "G. Kebutuhan khusus lainnya", "24. Apakah ada pantangan makanan/alergi tertentu? (isi \"Tidak ada\" jika tidak ada)", QType.TEXT),
    SQ("diet_khusus", "G. Kebutuhan khusus lainnya", "25. Apakah memerlukan diet khusus (rendah gula, rendah garam, dll)? (isi \"Tidak ada\" jika tidak ada)", QType.TEXT),
    SQ("obat_pribadi", "G. Kebutuhan khusus lainnya", "26. Obat-obatan pribadi yang akan dibawa selama perjalanan (sebutkan semua, isi \"Tidak ada\" jika tidak ada)", QType.TEXTAREA),
    SQ("asuransi_aktif", "G. Kebutuhan khusus lainnya", "27. Apakah memiliki BPJS/asuransi kesehatan tambahan yang masih aktif?", QType.SINGLE,
        listOf("Ya, BPJS aktif", "Ya, asuransi swasta aktif", "Keduanya aktif", "Tidak ada yang aktif")),

    SQ("bersedia_surat_sehat", "H. Persetujuan & surat keterangan", "28. Bersedia melampirkan surat keterangan sehat dari dokter jika diminta tim IMTIYAZ?", QType.SINGLE,
        listOf("Ya, bersedia", "Sudah punya surat keterangan sehat", "Belum bersedia / perlu diskusi lebih lanjut")),
    SQ("persetujuan_keluarga", "H. Persetujuan & surat keterangan", "29. Pihak keluarga memahami dan menyetujui kondisi kesehatan jamaah sesuai yang dilaporkan di atas?", QType.SINGLE,
        listOf("Ya, kami setujui dan tanggung jawab penuh atas kebenaran data ini"))
)

@Composable
fun SkriningScreen() {
    val scope = rememberCoroutineScope()
    var jamaahId by remember { mutableStateOf("") } // opsional - dipakai untuk kaitkan ke profil jamaah kalau kolomnya sudah ditambahkan di WP
    var email by remember { mutableStateOf("") }
    val textAnswers = remember { mutableStateMapOf<String, String>() }
    val multiAnswers = remember { mutableStateMapOf<String, MutableSet<String>>() }
    var submitting by remember { mutableStateOf(false) }
    var submitStatus by remember { mutableStateOf<String?>(null) }
    var submitErrorDetail by remember { mutableStateOf<String?>(null) }

    val grouped = QUESTIONS.groupBy { it.category }

    fun requiredFilled(): Boolean {
        if (email.isBlank()) return false
        return QUESTIONS.filter { it.required }.all { q ->
            when (q.type) {
                QType.MULTI -> !(multiAnswers[q.id] ?: emptySet()).isEmpty()
                else -> !(textAnswers[q.id] ?: "").isBlank()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(BrandGreen).padding(16.dp)) {
        Text("Skrining Kesehatan Lansia", color = Sand, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "29 pertanyaan asli, kategori A-H. Data masuk ke sistem skrining yang sama dengan formulir web.",
            color = Muted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        FieldBox(value = email, onChange = { email = it }, placeholder = "Email *")
        Spacer(Modifier.height(8.dp))
        FieldBox(value = jamaahId, onChange = { jamaahId = it }, placeholder = "ID Jamaah (opsional, kalau sudah punya)")
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

        // Tombol "Kirim" SELALU TERLIHAT di bawah (tidak perlu scroll habis dulu).
        Spacer(Modifier.height(10.dp))
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
                        val body = mutableMapOf<String, Any>("email" to email.trim())
                        if (jamaahId.isNotBlank()) body["jamaah_id"] = jamaahId.trim()
                        textAnswers.forEach { (k, v) -> if (v.isNotBlank()) body[k] = v }
                        multiAnswers.forEach { (k, v) -> if (v.isNotEmpty()) body[k] = v.toList() }
                        val res = ImtiyazApi.service.submitSkrining(body)
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

        submitStatus?.let { status ->
            AlertDialog(
                onDismissRequest = { /* wajib tekan tombol */ },
                containerColor = PanelColor,
                title = {
                    Text(if (status == "sukses") "Skrining Terkirim" else "Gagal Mengirim", color = Sand, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        if (status == "sukses") "Data skrining kesehatan sudah diterima admin. Terima kasih."
                        else submitErrorDetail ?: "Terjadi kesalahan.",
                        color = Muted, fontSize = 13.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (status == "sukses") {
                            jamaahId = ""; email = ""
                            textAnswers.clear(); multiAnswers.clear()
                        }
                        submitStatus = null
                    }) {
                        Text("OK", color = BrandGold, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
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
