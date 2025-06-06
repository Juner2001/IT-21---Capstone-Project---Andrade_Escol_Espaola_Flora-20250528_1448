package com.example.ecoguard.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryBook(
    val title: String = "",
    val author: String = "",
    val content: String = "",
    val coverUrl: String = ""
) : Parcelable

@Parcelize
data class DocumentaryVideo(
    val title: String = "",
    val description: String = "",
    val youtubeUrl: String = ""
) : Parcelable