package com.example.ecoguard

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class VerifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        // Initialize Firebase Authentication
        val auth = FirebaseAuth.getInstance()

        // Assuming you have a Button with id sigBtn in your XML
        val proceedToLoginButton: Button = findViewById(R.id.sigBtn)

        proceedToLoginButton.setOnClickListener {
            // Show loading dialog if needed
            val loadingDialog = Dialog(this, R.style.DialogCustomTheme).apply {
                setContentView(R.layout.loading_dialog)
                window!!.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setCancelable(false)
                show()
            }

            // Navigate to login
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val mainIntent = Intent(this, LoginActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(mainIntent)
        finish()
    }
}
