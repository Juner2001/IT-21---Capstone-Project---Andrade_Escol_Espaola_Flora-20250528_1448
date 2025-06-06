package com.example.ecoguard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SettingsActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var changeProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var emailTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference

    private val PICK_IMAGE_REQUEST     = 1
    private val EDIT_INFO_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        profileImageView    = findViewById(R.id.profileImageView)
        changeProfileButton = findViewById(R.id.changeProfileButton)
        logoutButton        = findViewById(R.id.logoutButton)
        emailTextView       = findViewById(R.id.profileEmailTextView)
        usernameTextView    = findViewById(R.id.profileNameTextView)

        // Section click listeners
        findViewById<LinearLayout>(R.id.editPersonalInfoLayout)
            .setOnClickListener {
                startActivityForResult(
                    Intent(this, EditPersonalInfoActivity::class.java),
                    EDIT_INFO_REQUEST_CODE
                )
            }

        findViewById<LinearLayout>(R.id.editPasswordInfoLayout)
            .setOnClickListener {
                startActivity(Intent(this, ChangePasswordActivity::class.java))
            }

        findViewById<LinearLayout>(R.id.aboutUsLayout)
            .setOnClickListener {
                startActivity(Intent(this, AboutUsActivity::class.java))
            }

        findViewById<LinearLayout>(R.id.termsPoliciesLayout)
            .setOnClickListener {
                startActivity(Intent(this, TermsPoliciesActivity::class.java))
            }

        // Ensure we have an authenticated user
        FirebaseAuth.getInstance().currentUser?.let { user ->
            emailTextView.text = user.email

            // Reference to /users/{uid}
            databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(user.uid)

            loadUserData()

            changeProfileButton.setOnClickListener { openImageChooser() }
            logoutButton.setOnClickListener       { logout() }
        } ?: run {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == EDIT_INFO_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                loadUserData()
            }
            requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null -> {
                uploadImageToFirebase(data.data!!)
            }
            else -> { /* ignore */ }
        }
    }

    private fun openImageChooser() {
        Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }.also {
            startActivityForResult(
                Intent.createChooser(it, "Select Profile Picture"),
                PICK_IMAGE_REQUEST
            )
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        storageReference = FirebaseStorage
            .getInstance()
            .reference
            .child("profile_pictures/$uid/profile_image.jpg")

        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    updateProfilePicture(uri)
                    saveImageUrlToDatabase(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to upload image: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateProfilePicture(uri: Uri) {
        FirebaseAuth.getInstance().currentUser
            ?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build()
            )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadProfileImage(uri)
                    Toast.makeText(
                        this,
                        "Profile picture updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loadProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_default_profile)
            .error(R.drawable.ic_error_image)
            .into(profileImageView)
    }

    private fun loadUserData() {
        databaseReference.get()
            .addOnSuccessListener { snapshot ->
                val encFirst  = snapshot.child("firstName").getValue(String::class.java).orEmpty()
                val encMiddle = snapshot.child("middleName").getValue(String::class.java).orEmpty()
                val encLast   = snapshot.child("lastName").getValue(String::class.java).orEmpty()

                val firstName  = if (encFirst.isNotBlank()) EncryptionUtil.decrypt(encFirst) else ""
                val middleName = if (encMiddle.isNotBlank()) EncryptionUtil.decrypt(encMiddle) else ""
                val lastName   = if (encLast.isNotBlank()) EncryptionUtil.decrypt(encLast) else ""

                val fullName = listOf(firstName, middleName, lastName)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")

                usernameTextView.text = fullName

                FirebaseAuth.getInstance().currentUser
                    ?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                    )

                loadUserProfileImage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfileImage() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        storageReference = FirebaseStorage
            .getInstance()
            .reference
            .child("profile_pictures/$uid/profile_image.jpg")

        storageReference.downloadUrl
            .addOnSuccessListener { uri -> loadProfileImage(uri) }
            .addOnFailureListener { /* no image yet */ }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        databaseReference.child("profileImageUrl").setValue(imageUrl)
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            startActivity(it)
            finish()
        }
    }
}
