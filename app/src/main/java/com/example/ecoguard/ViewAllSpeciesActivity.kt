package com.example.ecoguard

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ecoguard.Adapter.PopularSpeciesAdapter
import com.example.ecoguard.Domain.Species
import com.example.ecoguard.databinding.ActivityViewAllSpeciesBinding
import com.google.firebase.database.*

class ViewAllSpeciesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAllSpeciesBinding
    private lateinit var database: DatabaseReference
    private val speciesList = ArrayList<Species>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAllSpeciesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set RecyclerView to a Grid with 2 columns (left and right layout)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        database = FirebaseDatabase.getInstance().getReference("Species")

        binding.progressBar.visibility = View.VISIBLE

        // Get only the species with Popular = true
        val query = database.orderByChild("Popular").equalTo(true)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                speciesList.clear()
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val species = child.getValue(Species::class.java)
                        species?.let { speciesList.add(it) }
                    }
                    binding.recyclerView.adapter = PopularSpeciesAdapter(speciesList)
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }
}
