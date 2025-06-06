// File: app/src/main/java/com/example/ecoguard/RegisterActivity.kt
package com.example.ecoguard

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var rgGender: RadioGroup
    private lateinit var cbTerms: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = FirebaseDatabase.getInstance()

        val signUpBtn     = findViewById<Button>(R.id.signUpBtn)
        val signInBtn     = findViewById<Button>(R.id.signInBtn)
        val edFirstName   = findViewById<TextInputEditText>(R.id.edFirstName)
        val edMiddleName  = findViewById<TextInputEditText>(R.id.edMiddleName)
        val edLastName    = findViewById<TextInputEditText>(R.id.edLastName)
        val edEmail       = findViewById<TextInputEditText>(R.id.edEmail)
        val edPassword    = findViewById<TextInputEditText>(R.id.edPassword)
        val edConPassword = findViewById<TextInputEditText>(R.id.edConPassword)
        rgGender          = findViewById(R.id.rgGender)
        cbTerms           = findViewById(R.id.cbTerms)

        cbTerms.text = HtmlCompat.fromHtml(
            "I agree to the <u><font color='#0000FF'>Terms & Policies</font></u>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        cbTerms.setOnClickListener {
            if (cbTerms.isPressed) showTermsDialog()
        }

        signUpBtn.setOnClickListener {
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please agree to the Terms & Policies", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (validateInputs(
                    edFirstName, edMiddleName, edLastName,
                    edEmail, edPassword, edConPassword, rgGender
                )) {
                val email    = edEmail.text.toString().trim()
                val password = edPassword.text.toString().trim()
                val gender   = when (rgGender.checkedRadioButtonId) {
                    R.id.rbMale   -> "Male"
                    R.id.rbFemale -> "Female"
                    else          -> ""
                }

                val loadingDialog = showLoadingDialog()

                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(
                                "${edFirstName.text.toString().trim()} " +
                                        "${edMiddleName.text.toString().trim()} " +
                                        "${edLastName.text.toString().trim()}"
                            )
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    user.sendEmailVerification()
                                        .addOnCompleteListener { emailTask ->
                                            loadingDialog.dismiss()
                                            if (emailTask.isSuccessful) {
                                                addUserToDatabase(
                                                    edFirstName.text.toString().trim(),
                                                    edMiddleName.text.toString().trim(),
                                                    edLastName.text.toString().trim(),
                                                    email,
                                                    user.uid,
                                                    gender
                                                )
                                                redirectToVerifyActivity()
                                            } else {
                                                longToastShow("Failed to send verification email.")
                                            }
                                        }
                                } else {
                                    loadingDialog.dismiss()
                                    longToastShow("Failed to update profile.")
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        longToastShow("Registration failed: ${e.message}")
                    }
            }
        }

        signInBtn.setOnClickListener {
            redirectToLoginActivity()
        }
    }

    private fun showTermsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_terms)
        dialog.findViewById<Button>(R.id.btnCloseTerms).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addUserToDatabase(
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        uid: String,
        gender: String
    ) {
        val role = "user"
        val status = "active" // auto-set unblock status
        val ref  = database.reference.child("users").child(uid)

        val encryptedData = mapOf(
            "firstName"  to EncryptionUtil.encrypt(firstName),
            "middleName" to EncryptionUtil.encrypt(middleName),
            "lastName"   to EncryptionUtil.encrypt(lastName),
            "email"      to EncryptionUtil.encrypt(email),
            "uid"        to EncryptionUtil.encrypt(uid),
            "role"       to EncryptionUtil.encrypt(role),
            "gender"     to EncryptionUtil.encrypt(gender),
            "status"     to EncryptionUtil.encrypt(status)
        )

        ref.setValue(encryptedData)
            .addOnSuccessListener { /* optional: show success */ }
            .addOnFailureListener { e ->
                longToastShow("Failed to save user data: ${e.message}")
            }
    }

    private fun validateInputs(
        edFirstName: TextInputEditText,
        edMiddleName: TextInputEditText,
        edLastName: TextInputEditText,
        edEmail: TextInputEditText,
        edPassword: TextInputEditText,
        edConPassword: TextInputEditText,
        rgGender: RadioGroup
    ): Boolean {
        var isValid = true
        if (edFirstName.text.isNullOrBlank())    isValid = false
        if (edMiddleName.text.isNullOrBlank())   isValid = false
        if (edLastName.text.isNullOrBlank())     isValid = false
        if (edEmail.text.isNullOrBlank())        isValid = false
        if (edPassword.text.isNullOrBlank() ||
            edPassword.text.toString() != edConPassword.text.toString()
        ) isValid = false
        if (rgGender.checkedRadioButtonId == -1)  isValid = false
        return isValid
    }

    private fun showLoadingDialog(): Dialog {
        val dialog = Dialog(this, R.style.DialogCustomTheme).apply {
            setContentView(R.layout.loading_dialog)
            window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
            show()
        }
        return dialog
    }

    private fun redirectToVerifyActivity() {
        Intent(this, VerifyActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
        finish()
    }

    private fun redirectToLoginActivity() {
        Intent(this, LoginActivity::class.java).also { startActivity(it) }
        finish()
    }

    private fun longToastShow(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
