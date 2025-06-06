package com.example.ecoguard

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditPersonalInfoActivity : AppCompatActivity() {
    private lateinit var firstNameEdit: TextInputEditText
    private lateinit var middleNameEdit: TextInputEditText
    private lateinit var lastNameEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var rgGender: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_personal_info)

        firstNameEdit  = findViewById(R.id.editFirstName)
        middleNameEdit = findViewById(R.id.editMiddleName)
        lastNameEdit   = findViewById(R.id.editLastName)
        emailEdit      = findViewById(R.id.editEmailEditText)
        rgGender       = findViewById(R.id.rgGender)
        saveButton     = findViewById(R.id.saveButton)
        backButton     = findViewById(R.id.backButton)
        progressBar    = findViewById(R.id.progressBar)

        // Initialize database ref for current user
        FirebaseAuth.getInstance().currentUser?.let { user ->
            dbRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
            loadUserData()
        }

        saveButton.setOnClickListener { saveUserData() }
        backButton.setOnClickListener { onBackPressed() }
    }

    private fun loadUserData() {
        progressBar.visibility = ProgressBar.VISIBLE
        dbRef.get().addOnSuccessListener { snap ->
            // Decrypt and set data
            firstNameEdit.setText(EncryptionUtil.decrypt(snap.child("firstName").getValue(String::class.java) ?: ""))
            middleNameEdit.setText(EncryptionUtil.decrypt(snap.child("middleName").getValue(String::class.java) ?: ""))
            lastNameEdit.setText(EncryptionUtil.decrypt(snap.child("lastName").getValue(String::class.java) ?: ""))
            emailEdit.setText(EncryptionUtil.decrypt(snap.child("email").getValue(String::class.java) ?: ""))

            when (EncryptionUtil.decrypt(snap.child("gender").getValue(String::class.java) ?: "")) {
                "Male"   -> rgGender.check(R.id.rbMaleEdit)
                "Female" -> rgGender.check(R.id.rbFemaleEdit)
            }
            progressBar.visibility = ProgressBar.GONE
        }.addOnFailureListener {
            progressBar.visibility = ProgressBar.GONE
            Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val firstName  = firstNameEdit.text.toString().trim()
        val middleName = middleNameEdit.text.toString().trim()
        val lastName   = lastNameEdit.text.toString().trim()
        val email      = emailEdit.text.toString().trim()
        val gender     = when (rgGender.checkedRadioButtonId) {
            R.id.rbMaleEdit   -> "Male"
            R.id.rbFemaleEdit -> "Female"
            else              -> ""
        }

        if (firstName.isEmpty() || middleName.isEmpty() || lastName.isEmpty() ||
            email.isEmpty() || gender.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        FirebaseAuth.getInstance().currentUser?.let { _ ->
            // Encrypt and save user data
            dbRef.child("firstName").setValue(EncryptionUtil.encrypt(firstName))
            dbRef.child("middleName").setValue(EncryptionUtil.encrypt(middleName))
            dbRef.child("lastName").setValue(EncryptionUtil.encrypt(lastName))
            dbRef.child("email").setValue(EncryptionUtil.encrypt(email))
            dbRef.child("gender").setValue(EncryptionUtil.encrypt(gender))
                .addOnCompleteListener {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    progressBar.visibility = ProgressBar.GONE
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
