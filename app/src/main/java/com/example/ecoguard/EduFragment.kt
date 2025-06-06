package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class EduFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edu, container, false)

        val btnStoryBooks = view.findViewById<Button>(R.id.btnStoryBooks)
        val btnDocumentaryVideos = view.findViewById<Button>(R.id.btnDocumentaryVideos)
        val btnFunQuiz = view.findViewById<Button>(R.id.btnFunQuiz)


        btnStoryBooks.setOnClickListener {
            startActivity(Intent(activity, StoryBooksActivity::class.java))
        }

        btnDocumentaryVideos.setOnClickListener {
            startActivity(Intent(activity, DocumentaryVideosActivity::class.java))
        }

        btnFunQuiz.setOnClickListener {
            startActivity(Intent(activity, SplashActivity::class.java))
        }



        return view
    }
}
