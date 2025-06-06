package com.example.ecoguard

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ReportFragment : Fragment(R.layout.fragment_report) {

    private lateinit var messageToggleGroup: RadioGroup
    private lateinit var chatLayout: View
    private lateinit var smsLayout: View
    private lateinit var etSubject: EditText
    private lateinit var etMessage: EditText
    private lateinit var etSmsMessage: EditText
    private lateinit var btnAddImage: Button
    private lateinit var btnSend: Button
    private lateinit var buttonSendSms: Button
    private lateinit var btnHistory: Button
    private lateinit var llMultiImagePreview: LinearLayout
    private lateinit var bannedTextView: TextView
    private lateinit var lvSubjectList: ListView
    private lateinit var progressBar: ProgressBar

    private val imageUris: MutableList<Uri> = mutableListOf()
    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("reports")
    private val storageReference = FirebaseStorage.getInstance().reference
    private val userDatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    private var isUserBlocked = false

    private val subjects = listOf(
        "Pollution and Waste Dumping",
        "Illegal Fishing",
        "Wildlife Trafficking",
        "Destructive Fishing Practices",
        "Habitat Destruction",
        "Wildlife Poaching",
        "Water Contamination",
        "Harvesting of Protected Marine Species"
    )

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        uris?.let {
            imageUris.clear()
            llMultiImagePreview.removeAllViews()
            for (uri in uris) {
                imageUris.add(uri)
                addImageToPreview(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageToggleGroup = view.findViewById(R.id.messageToggleGroup)
        chatLayout = view.findViewById(R.id.chatLayout)
        smsLayout = view.findViewById(R.id.smsLayout)
        etSubject = view.findViewById(R.id.etSubject)
        etMessage = view.findViewById(R.id.etMessage)
        etSmsMessage = view.findViewById(R.id.editTextMessage)
        btnAddImage = view.findViewById(R.id.btnAddImage)
        btnSend = view.findViewById(R.id.btnSend)
        buttonSendSms = view.findViewById(R.id.buttonSendSms)
        btnHistory = view.findViewById(R.id.btnHistory)
        llMultiImagePreview = view.findViewById(R.id.llMultiImagePreview)
        bannedTextView = view.findViewById(R.id.tvBanMessage)
        lvSubjectList = view.findViewById(R.id.lvSubjectList)
        progressBar = view.findViewById(R.id.progressBarSending)

        checkUserStatus()
        setupSubjectList()
        updateLayoutVisibility()

        messageToggleGroup.setOnCheckedChangeListener { _, _ ->
            updateLayoutVisibility()
        }

        btnAddImage.setOnClickListener {
            if (!isUserBlocked) {
                pickImagesLauncher.launch(arrayOf("image/*"))
            } else {
                Toast.makeText(requireContext(), "Banned users cannot add images.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSend.setOnClickListener {
            if (!isUserBlocked) {
                progressBar.visibility = View.VISIBLE
                btnSend.isEnabled = false
                sendReport()
            } else {
                Toast.makeText(requireContext(), "You are banned from sending reports.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonSendSms.setOnClickListener {
            val smsMessage = etSmsMessage.text.toString()
            if (smsMessage.isNotEmpty()) {
                val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("smsto:09945405775")
                    putExtra("sms_body", smsMessage)
                }
                try {
                    startActivity(smsIntent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No SMS app found or permission issue.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a message.", Toast.LENGTH_SHORT).show()
            }
        }

        btnHistory.setOnClickListener {
            openReportHistory()
        }
    }

    private fun setupSubjectList() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, subjects)
        lvSubjectList.adapter = adapter

        lvSubjectList.setOnItemClickListener { _, _, position, _ ->
            etSubject.setText(subjects[position])
            lvSubjectList.visibility = View.GONE
        }

        etSubject.setOnFocusChangeListener { _, hasFocus ->
            lvSubjectList.visibility = if (hasFocus) View.VISIBLE else View.GONE
        }
    }

    private fun updateLayoutVisibility() {
        val checkedId = messageToggleGroup.checkedRadioButtonId
        if (checkedId == R.id.rbSms) {
            chatLayout.visibility = View.GONE
            smsLayout.visibility = View.VISIBLE
        } else {
            chatLayout.visibility = View.VISIBLE
            smsLayout.visibility = View.GONE
        }
    }

    private fun checkUserStatus() {
        val userUid = auth.currentUser?.uid ?: return
        userDatabaseReference.child(userUid).get().addOnSuccessListener { snapshot ->
            val statusEncrypted = snapshot.child("status").getValue(String::class.java) ?: ""
            val decryptedStatus = EncryptionUtil.decrypt(statusEncrypted)
            if (decryptedStatus == "blocked") {
                isUserBlocked = true
                bannedTextView.visibility = View.VISIBLE
                bannedTextView.text = "🚫 You are banned from sending reports."
                btnSend.isEnabled = false
                btnAddImage.isEnabled = false
            } else {
                isUserBlocked = false
                bannedTextView.visibility = View.GONE
                btnSend.isEnabled = true
                btnAddImage.isEnabled = true
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to check user status.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendReport() {
        val subject = etSubject.text.toString()
        val message = etMessage.text.toString()
        if (subject.isEmpty() || message.isEmpty() || imageUris.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields and add at least one image.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            btnSend.isEnabled = true
            return
        }
        val userUid = auth.currentUser?.uid ?: return
        uploadReport(userUid, subject, message)
    }

    private fun uploadReport(userUid: String, subject: String, message: String) {
        val uploadedImageUrls = mutableListOf<String>()
        var uploadsCompleted = 0
        val totalUploads = imageUris.size

        for (uri in imageUris) {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
            val compressedData = baos.toByteArray()

            val imageRef = storageReference.child("report_images/${System.currentTimeMillis()}_${uri.lastPathSegment}.jpg")

            imageRef.putBytes(compressedData)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        uploadedImageUrls.add(downloadUrl.toString())
                        uploadsCompleted++
                        if (uploadsCompleted == totalUploads) {
                            val timestamp = System.currentTimeMillis()
                            val report = Report(subject, message, uploadedImageUrls, userUid, timestamp)
                            val encryptedReport = encryptReport(report)
                            databaseReference.push().setValue(encryptedReport)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Report sent successfully!", Toast.LENGTH_SHORT).show()
                                    imageUris.clear()
                                    llMultiImagePreview.removeAllViews()
                                    etSubject.text.clear()
                                    etMessage.text.clear()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Failed to send report.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener {
                                    progressBar.visibility = View.GONE
                                    btnSend.isEnabled = true
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Image upload failed.", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    btnSend.isEnabled = true
                }
        }
    }

    private fun addImageToPreview(uri: Uri) {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                setMargins(8, 8, 8, 8)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
        }
        llMultiImagePreview.addView(imageView)
    }

    private fun encryptReport(report: Report): Report {
        val encryptedSubject = EncryptionUtil.encrypt(report.subject)
        val encryptedMessage = EncryptionUtil.encrypt(report.message)
        val encryptedImageUrls = report.imageUrls.map { EncryptionUtil.encrypt(it) }
        val encryptedUserUid = EncryptionUtil.encrypt(report.userUid)
        return Report(encryptedSubject, encryptedMessage, encryptedImageUrls, encryptedUserUid, report.timestamp)
    }

    private fun openReportHistory() {
        val intent = Intent(requireContext(), ReportHistoryActivity::class.java)
        startActivity(intent)
    }

    data class Report(
        val subject: String = "",
        val message: String = "",
        val imageUrls: List<String> = emptyList(),
        val userUid: String = "",
        val timestamp: Long = 0L
    )
}
