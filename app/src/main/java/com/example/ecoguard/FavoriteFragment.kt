package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoguard.Domain.Species
import com.example.ecoguard.databinding.FragmentFavoriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var database: DatabaseReference
    private val favoriteList = mutableListOf<Species>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        favoriteAdapter = FavoriteAdapter(favoriteList, onUnfavoriteClick = { Species ->
            removeFromFavorites(Species)
        }, onItemClick = { Species ->
            openDetailActivity(Species)
        })

        binding.favoriteRecyclerView.adapter = favoriteAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            database = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.uid).child("favorites")

            loadFavorites()
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }

    private fun loadFavorites() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteList.clear()
                for (favoriteSnapshot in snapshot.children) {
                    val Species = favoriteSnapshot.getValue(Species::class.java)
                    if (Species != null) {
                        favoriteList.add(Species)
                    }
                }
                favoriteAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load favorites", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun removeFromFavorites(Species: Species) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            database.child(Species.title ?: "Unknown").removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    loadFavorites()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openDetailActivity(Species: Species) {
        val intent = Intent(context, DetailActivity::class.java)
        intent.putExtra("object", Species)
        startActivity(intent)
    }
}
