package com.example.ecoguard

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class IdentifierActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private lateinit var capturedImageView: ImageView
    private lateinit var cameraButton: Button
    private var capturedImageBytes: ByteArray? = null

    private val apiKey = "AIzaSyAIqldhKK3lKRAasMNsXLfBCX34wnZP4LA"
    private val client = OkHttpClient()

    companion object {
        const val CAMERA_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identifier)

        resultTextView = findViewById(R.id.resultTextView)
        capturedImageView = findViewById(R.id.capturedImageView)
        cameraButton = findViewById(R.id.cameraButton)

        cameraButton.setOnClickListener { openCamera() }

        // Auto-open ng camera kapag unang open at walang laman
        if (capturedImageBytes == null) {
            Handler(Looper.getMainLooper()).postDelayed({
                openCamera()
            }, 100)
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolveInfo = packageManager.resolveActivity(
            cameraIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfo != null) {
            val activityInfo = resolveInfo.activityInfo
            cameraIntent.component = ComponentName(
                activityInfo.packageName,
                activityInfo.name
            )
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No camera available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                // Ipakita ang na-capture na larawan
                capturedImageView.setImageBitmap(photo)
                // I-convert ang Bitmap sa ByteArray
                val stream = ByteArrayOutputStream()
                photo.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                capturedImageBytes = stream.toByteArray()
                Toast.makeText(this, "Image captured", Toast.LENGTH_SHORT).show()
                // Awtomatikong ipadala ang larawan para sa description
                sendImageForDescription()
            }
        }
    }

    private fun sendImageForDescription() {
        if (capturedImageBytes == null) {
            Toast.makeText(this, "No image to describe", Toast.LENGTH_SHORT).show()
            return
        }

        // Gumawa ng JSON payload na may image data
        val base64String = Base64.encodeToString(capturedImageBytes, Base64.NO_WRAP)
        val inlineData = JSONObject().apply {
            put("mimeType", "image/jpeg")
            put("data", base64String)
        }
        val imagePart = JSONObject().apply { put("inlineData", inlineData) }
        val parts = JSONArray().apply { put(imagePart) }
        val contents = JSONArray().apply {
            put(JSONObject().apply { put("parts", parts) })
        }
        val requestData = JSONObject().apply { put("contents", contents) }

        // I-build ang API URL kasama ang API key
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=$apiKey"
        val requestBody = requestData.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Asynchronously tawagin ang API gamit ang Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(url).post(requestBody).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                var description = "No description received."
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    description = jsonResponse
                        .optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text") ?: description
                }
                withContext(Dispatchers.Main) {
                    resultTextView.text = description
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultTextView.text = "Error: ${e.localizedMessage}"
                }
            }
        }
    }
}
