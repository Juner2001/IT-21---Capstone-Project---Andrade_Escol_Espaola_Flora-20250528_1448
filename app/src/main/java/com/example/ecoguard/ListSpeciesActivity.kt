package com.example.ecoguard

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoguard.Adapter.SpeciesListAdapter
import com.example.ecoguard.Domain.Species

import com.example.ecoguard.databinding.ActivityListSpeciesBinding
import com.google.firebase.database.*

class ListSpeciesActivity : BaseActivity() {
    private lateinit var binding: ActivityListSpeciesBinding
    private var adapterListSpecies: RecyclerView.Adapter<*>? = null
    private var categoryId = 0
    private var categoryName: String? = ""
    private var searchText: String? = ""
    private var isSearch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListSpeciesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentExtra()
        initList()
        setVariable()
    }

    private fun setVariable() {
        // Additional variable setup logic here if needed
    }

    private fun initList() {
        val myRef = FirebaseDatabase.getInstance().getReference("Species")
        binding.progressBar.visibility = View.VISIBLE
        val list = ArrayList<Species>()

        val query: Query = if (isSearch) {
            myRef.orderByChild("Title").startAt(searchText).endAt(searchText + '\uf8ff')
        } else {
            myRef.orderByChild("CategoryId").equalTo(categoryId.toDouble())
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        list.add(issue.getValue(Species::class.java)!!)
                    }
                    if (list.isNotEmpty()) {
                        binding.foodListView.layoutManager = GridLayoutManager(this@ListSpeciesActivity, 2)
                        adapterListSpecies =
                            SpeciesListAdapter(list)
                        binding.foodListView.adapter = adapterListSpecies
                    }
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun getIntentExtra() {
        categoryId = intent.getIntExtra("CategoryId", 0)
        categoryName = intent.getStringExtra("CategoryName")
        searchText = intent.getStringExtra("text")
        isSearch = intent.getBooleanExtra("isSearch", false)

        binding.titleTxt.text = categoryName
        binding.backBtn.setOnClickListener { finish() }
    }
}
