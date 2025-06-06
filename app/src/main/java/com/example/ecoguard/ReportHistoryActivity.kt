package com.example.ecoguard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ReportHistoryActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar

    private val reportsList = mutableListOf<Report>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_history)

        listView = findViewById(R.id.lvReports)
        progressBar = findViewById(R.id.progressBar)

        databaseReference = FirebaseDatabase.getInstance().getReference("reports")

        listView.setOnItemClickListener { _, _, position, _ ->
            val report = reportsList[position]

            val intent = Intent(this, ViewReportActivity::class.java).apply {
                putExtra("subject", report.subject)
                putExtra("message", report.message)
                putStringArrayListExtra("imageUrls", ArrayList(report.imageUrls))
            }
            startActivity(intent)
        }

        loadUserReports()
    }

    override fun onResume() {
        super.onResume()
        loadUserReports()
    }

    private fun loadUserReports() {
        progressBar.visibility = View.VISIBLE

        val userUid = auth.currentUser?.uid
        if (userUid == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reportsList.clear()

                for (reportSnap in snapshot.children) {
                    val encrypted = reportSnap.getValue(Report::class.java) ?: continue
                    val decryptedUid = EncryptionUtil.decrypt(encrypted.userUid)

                    if (decryptedUid == userUid) {
                        val decryptedReport = Report(
                            subject = EncryptionUtil.decrypt(encrypted.subject),
                            message = EncryptionUtil.decrypt(encrypted.message),
                            imageUrls = encrypted.imageUrls.map { EncryptionUtil.decrypt(it) },
                            userUid = decryptedUid,
                            seen = encrypted.seen,
                            status = encrypted.status ?: "under_review",
                            timestamp = encrypted.timestamp
                        )

                        reportsList.add(decryptedReport)
                    }
                }

                val adapter = ReportAdapter(this@ReportHistoryActivity, reportsList)
                listView.adapter = adapter

                progressBar.visibility = View.GONE

                if (reportsList.isEmpty()) {
                    Toast.makeText(this@ReportHistoryActivity, "No reports found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ReportHistoryActivity, "Failed to load reports: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Custom Adapter class
    class ReportAdapter(
        private val context: android.content.Context,
        private val reports: List<Report>
    ) : ArrayAdapter<Report>(context, 0, reports) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: android.view.LayoutInflater.from(context)
                .inflate(R.layout.item_report, parent, false)

            val report = reports[position]

            val tvSubject = view.findViewById<TextView>(R.id.tvSubject)
            val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
            val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
            val tvTimestamp = view.findViewById<TextView>(R.id.tvTimestamp)

            tvSubject.text = "Subject: ${report.subject}"
            tvMessage.text = "Message: ${report.message}"

            val formattedDate = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault())
                .format(Date(report.timestamp))
            tvTimestamp.text = "Reported on: $formattedDate"

            when (report.status.lowercase()) {
                "approved" -> {
                    tvStatus.text = "APPROVED"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                }
                "declined" -> {
                    tvStatus.text = "DECLINED"
                    tvStatus.setTextColor(Color.parseColor("#F44336"))
                }
                else -> {
                    tvStatus.text = "UNDER REVIEW"
                    tvStatus.setTextColor(Color.parseColor("#FFC107"))
                }
            }

            return view
        }
    }

    data class Report(
        val subject: String = "",
        val message: String = "",
        val imageUrls: List<String> = emptyList(),
        val userUid: String = "",
        val seen: Boolean = false,
        val status: String = "under_review",
        val timestamp: Long = 0L
    )
}
