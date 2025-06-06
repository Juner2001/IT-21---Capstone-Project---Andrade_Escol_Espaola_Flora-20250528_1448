package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.Adapter.CategoryAdapter
import com.example.ecoguard.Adapter.PopularSpeciesAdapter
import com.example.ecoguard.Adapter.SlidingAdapter
import com.example.ecoguard.Domain.BannerItem
import com.example.ecoguard.Domain.Category
import com.example.ecoguard.Domain.Species
import com.example.ecoguard.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var userDatabaseReference: DatabaseReference

    private val slidingCards = ArrayList<BannerItem>()
    private lateinit var slidingAdapter: SlidingAdapter
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())

    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            if (currentIndex < slidingCards.size - 1) {
                currentIndex += 1
                binding.sliding.smoothScrollToPosition(currentIndex)
                handler.postDelayed(this, 5000)
            } else {
                handler.removeCallbacks(this) // Stop at last item
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userDatabaseReference = database.getReference("users").child(currentUser.uid)
            loadUserProfileImage()
        }

        initSlidingCards()
        initPopularSpecies()
        initCategory()

        binding.searchButton.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }

        binding.identifierButton.setOnClickListener {
            startActivity(Intent(requireContext(), IdentifierActivity::class.java))
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.viewAllPopular.setOnClickListener {
            startActivity(Intent(requireContext(), ViewAllSpeciesActivity::class.java))
        }

        // New: Open RewardActivity on rewardButton click
        binding.coinIcon.setOnClickListener {
            startActivity(Intent(requireContext(), RewardActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        return binding.root
    }

    private fun loadUserProfileImage() {
        userDatabaseReference.child("profileImageUrl")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val imageUrl = snapshot.getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@HomeFragment)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_default_profile)
                            .error(R.drawable.ic_default_profile)
                            .into(binding.settings)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun initSlidingCards() {
        val bannerRef = database.getReference("Sliding")
        binding.progressBarSliding.visibility = View.VISIBLE
        slidingCards.clear()

        bannerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                slidingCards.clear()
                if (snapshot.exists()) {
                    for (bannerSnapshot in snapshot.children) {
                        val banner = bannerSnapshot.getValue(BannerItem::class.java)
                        banner?.let { slidingCards.add(it) }
                    }

                    if (slidingCards.isNotEmpty()) {
                        slidingAdapter = SlidingAdapter(slidingCards) { bannerItem ->
                            // OnClickListener: Open BannerDetailActivity
                            val intent = Intent(requireContext(), BannerDetailActivity::class.java)
                            intent.putExtra("imageUrl", bannerItem.imageUrl)
                            intent.putExtra("title", bannerItem.title)
                            intent.putExtra("newsMeta", bannerItem.newsMeta)
                            intent.putExtra("description", bannerItem.description)
                            startActivity(intent)
                        }

                        val layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        binding.sliding.layoutManager = layoutManager
                        binding.sliding.adapter = slidingAdapter

                        binding.sliding.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                    if (firstVisible != RecyclerView.NO_POSITION) {
                                        currentIndex = firstVisible
                                    }
                                }
                            }
                        })

                        currentIndex = 0
                        handler.removeCallbacks(autoSlideRunnable)
                        handler.postDelayed(autoSlideRunnable, 5000)
                    }
                }
                binding.progressBarSliding.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarSliding.visibility = View.GONE
            }
        })
    }

    private fun initPopularSpecies() {
        val myRef = database.getReference("Species")
        binding.progressBarpopularSpecies.visibility = View.VISIBLE
        val list = ArrayList<Species>()

        val query = myRef.orderByChild("Popular").equalTo(true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        val species = issue.getValue(Species::class.java)
                        species?.let { list.add(it) }
                    }
                    if (list.isNotEmpty()) {
                        binding.popularSpeciesView.layoutManager = LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        binding.popularSpeciesView.adapter = PopularSpeciesAdapter(list)
                    }
                }
                binding.progressBarpopularSpecies.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarpopularSpecies.visibility = View.GONE
            }
        })
    }

    private fun initCategory() {
        val myRef = database.getReference("Category")
        binding.progressBarCategory.visibility = View.VISIBLE
        val list = ArrayList<Category>()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (issue in snapshot.children) {
                        val category = issue.getValue(Category::class.java)
                        category?.let { list.add(it) }
                    }
                    if (list.isNotEmpty()) {
                        binding.categoryView.layoutManager = GridLayoutManager(requireContext(), 4)
                        binding.categoryView.adapter = CategoryAdapter(list)
                    }
                }
                binding.progressBarCategory.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarCategory.visibility = View.GONE
            }
        })
    }

    private fun refreshData() {
        loadUserProfileImage()
        initSlidingCards()
        initPopularSpecies()
        initCategory()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(autoSlideRunnable)
        _binding = null
    }
}
