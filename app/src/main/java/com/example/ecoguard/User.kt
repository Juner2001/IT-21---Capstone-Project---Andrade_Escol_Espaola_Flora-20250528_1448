package com.example.ecoguard

data class User(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val email: String = "",
    val uid: String = "",
    val role: String = "user", // Default role is set to 'user'
    val gender: String = ""
)
