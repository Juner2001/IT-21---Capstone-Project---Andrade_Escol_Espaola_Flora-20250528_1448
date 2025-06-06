package com.example.ecoguard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ecoguard.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

open class BaseActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    val TAG: String = "uilover"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()

        window.statusBarColor = resources.getColor(R.color.white, theme)
    }
}
