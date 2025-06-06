package com.example.ecoguard

data class Report(
    val subject: String = "",
    val message: String = "",  // This will store the encrypted message
    val imageUrls: List<String> = emptyList(),
    val userUid: String = "", // Store the user UID
    val encryptionKey: String = "",
    val timestamp: Long = 0L
)
