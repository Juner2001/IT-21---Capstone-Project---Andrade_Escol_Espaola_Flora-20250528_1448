package com.example.ecoguard

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loadingDialog = Dialog(this, R.style.DialogCustomTheme).apply {
            setContentView(R.layout.loading_dialog)
            window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
        }

        val signUpABtn = findViewById<Button>(R.id.signUpABtn)
        signUpABtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val edEmail = findViewById<TextInputEditText>(R.id.edEmail)
        val edEmailL = findViewById<TextInputLayout>(R.id.edEmailL)
        val edPassword = findViewById<TextInputEditText>(R.id.edPassword)
        val edPasswordL = findViewById<TextInputLayout>(R.id.edPasswordL)

        edEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateEmail(edEmail, edEmailL)
            }
        })

        edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validatePassword(edPassword, edPasswordL)
            }
        })

        val signInBtn = findViewById<Button>(R.id.signInBtn)
        signInBtn.setOnClickListener {
            if (validateEmail(edEmail, edEmailL) && validatePassword(edPassword, edPasswordL)) {
                if (isConnected(this)) {
                    loadingDialog.show()
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(
                            edEmail.text.toString().trim(),
                            edPassword.text.toString().trim()
                        )
                        .addOnSuccessListener { authResult ->
                            val user = authResult.user
                            if (user != null && user.isEmailVerified) {
                                showToast("Login Successful")
                                loadingDialog.dismiss()
                                val mainIntent = Intent(this, MainActivity::class.java)
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(mainIntent)
                                finish()
                            } else {
                                loadingDialog.dismiss()
                                showToast("Email is not verified. Please check your email to verify your account.")
                                val verifyIntent = Intent(this, VerifyActivity::class.java)
                                verifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(verifyIntent)
                                finish()
                            }
                        }
                        .addOnFailureListener { exception ->
                            loadingDialog.dismiss()
                            if (exception.message?.contains("password is invalid") == true) {
                                showToast("Wrong credentials. Please check your email and password.")
                            } else {
                                showToast("Login failed. Please try again.")
                            }
                        }
                } else {
                    showToast("No Internet Connection!")
                }
            }
        }

        val forgotPasswordTxt = findViewById<TextView>(R.id.forgotPasswordTxt)
        forgotPasswordTxt.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun validateEmail(edEmail: TextInputEditText, edEmailL: TextInputLayout): Boolean {
        val email = edEmail.text.toString().trim()
        return if (email.isEmpty()) {
            edEmailL.error = "Email is required."
            false
        } else {
            edEmailL.error = null
            true
        }
    }

    private fun validatePassword(edPassword: TextInputEditText, edPasswordL: TextInputLayout): Boolean {
        val password = edPassword.text.toString().trim()
        return if (password.isEmpty()) {
            edPasswordL.error = "Password is required."
            false
        } else {
            edPasswordL.error = null
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isConnected(context: Context): Boolean {
        // Implement your network connection check logic here
        return true
    }
}
