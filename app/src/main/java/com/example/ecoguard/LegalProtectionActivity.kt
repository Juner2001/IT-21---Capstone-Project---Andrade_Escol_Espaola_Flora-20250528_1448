package com.example.ecoguard

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoguard.Adapter.LegalSpeciesListAdapter
import com.example.ecoguard.Domain.Category
import com.example.ecoguard.Domain.Species
import com.google.firebase.database.*

class LegalProtectionFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var categoryRef: DatabaseReference
    private lateinit var speciesRef: DatabaseReference

    private lateinit var categoryContainer: LinearLayout
    private lateinit var speciesRecyclerView: RecyclerView
    private lateinit var speciesListAdapter: LegalSpeciesListAdapter
    private lateinit var searchEditText: EditText

    private val speciesList = ArrayList<Species>()
    private val allSpeciesList = ArrayList<Species>()
    private var selectedButton: Button? = null  // Track selected category button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_legal_protection, container, false)

        categoryContainer = view.findViewById(R.id.categoryContainer)
        speciesRecyclerView = view.findViewById(R.id.speciesRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)

        speciesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        speciesListAdapter = LegalSpeciesListAdapter(speciesList)
        speciesRecyclerView.adapter = speciesListAdapter

        database = FirebaseDatabase.getInstance()
        categoryRef = database.getReference("Category")
        speciesRef = database.getReference("Species")

        loadCategories()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSpecies(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun loadCategories() {
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryContainer.removeAllViews()
                var firstCategoryId: Double? = null

                for ((index, catSnap) in snapshot.children.withIndex()) {
                    val category = catSnap.getValue(Category::class.java)
                    category?.let {
                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(16, 0, 16, 0) // Space between buttons
                        }

                        val button = Button(requireContext()).apply {
                            text = it.name ?: "Unnamed"
                            setPadding(32, 8, 32, 8)
                            this.layoutParams = layoutParams
                            setTextColor(Color.WHITE)
                            textSize = 14f
                            background = resources.getDrawable(R.drawable.button_category_background, null)

                            setOnClickListener {
                                selectedButton?.background = resources.getDrawable(
                                    R.drawable.button_category_background,
                                    null
                                )
                                selectedButton = this
                                background = resources.getDrawable(
                                    R.drawable.button_category_selected_background,
                                    null
                                )
                                loadSpeciesByCategory(category.id?.toDouble() ?: 0.0)
                            }
                        }

                        categoryContainer.addView(button)

                        if (index == 0) {
                            firstCategoryId = it.id?.toDouble()
                            selectedButton = button
                            button.background = resources.getDrawable(
                                R.drawable.button_category_selected_background,
                                null
                            )
                        }
                    }
                }

                firstCategoryId?.let { loadSpeciesByCategory(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadSpeciesByCategory(categoryId: Double) {
        speciesList.clear()
        allSpeciesList.clear()

        speciesRef.orderByChild("CategoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val species = snap.getValue(Species::class.java)
                        species?.let {
                            speciesList.add(it)
                            allSpeciesList.add(it)
                        }
                    }
                    speciesListAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load species", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterSpecies(query: String) {
        val filtered = if (query.isEmpty()) {
            allSpeciesList
        } else {
            allSpeciesList.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }
        }

        speciesList.clear()
        speciesList.addAll(filtered)
        speciesListAdapter.notifyDataSetChanged()
    }
}
