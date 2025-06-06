package com.example.ecoguard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.database.*

class PuzzleActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var displayImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar

    private val tileCount = 4
    private val tiles = mutableListOf<ImageView>()
    private val correctOrder = mutableListOf<Bitmap>()
    private lateinit var databaseRef: DatabaseReference
    private var emptyTileIndex = -1
    private lateinit var blackTileBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Image Puzzle"

        gridLayout = findViewById(R.id.gridLayout)
        displayImageView = findViewById(R.id.displayImageView)
        progressBar = findViewById(R.id.loadingIndicator)

        gridLayout.rowCount = tileCount
        gridLayout.columnCount = tileCount

        databaseRef = FirebaseDatabase.getInstance().getReference("Species")

        loadRandomImage()
    }

    private fun loadRandomImage() {
        progressBar.visibility = View.VISIBLE
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val speciesList = snapshot.children.mapNotNull {
                    it.child("ImagePath").getValue(String::class.java)
                }.filter { it.isNotEmpty() }

                if (speciesList.isNotEmpty()) {
                    val randomUrl = speciesList.random()
                    loadAndSliceImage(randomUrl)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@PuzzleActivity, "No images found in database.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@PuzzleActivity, "Error loading image: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadAndSliceImage(imageUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    progressBar.visibility = View.GONE
                    displayImageView.setImageBitmap(resource)
                    createPuzzle(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun createPuzzle(originalBitmap: Bitmap) {
        val tileSize = originalBitmap.width / tileCount
        tiles.clear()
        correctOrder.clear()
        gridLayout.removeAllViews()

        // Create black empty tile bitmap once
        blackTileBitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            canvas.drawColor(Color.parseColor("#222222"))
        }

        // Create tiles for the puzzle
        for (row in 0 until tileCount) {
            for (col in 0 until tileCount) {
                val index = row * tileCount + col
                val imageView = ImageView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(tileSize, tileSize)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    elevation = 8f // subtle shadow for tiles
                    setPadding(4,4,4,4)
                    background = getDrawable(android.R.color.white)
                }

                if (index < tileCount * tileCount - 1) {
                    val tileBitmap = Bitmap.createBitmap(
                        originalBitmap,
                        col * tileSize,
                        row * tileSize,
                        tileSize,
                        tileSize
                    )
                    imageView.setImageBitmap(tileBitmap)
                    correctOrder.add(tileBitmap)
                } else {
                    // empty black tile
                    imageView.setImageBitmap(blackTileBitmap)
                    emptyTileIndex = index
                }
                tiles.add(imageView)
            }
        }

        shuffleTiles()
        displayTiles()
    }

    private fun shuffleTiles() {
        val shuffledTiles = tiles.subList(0, tiles.size - 1).shuffled()
        for (i in 0 until shuffledTiles.size) {
            tiles[i] = shuffledTiles[i]
        }
        // Set last tile as empty
        tiles[tiles.size - 1] = ImageView(this).apply {
            val tileSize = blackTileBitmap.width
            layoutParams = ViewGroup.LayoutParams(tileSize, tileSize)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageBitmap(blackTileBitmap)
            elevation = 8f
            background = getDrawable(android.R.color.white)
        }
        emptyTileIndex = tiles.size - 1
    }


    private fun displayTiles() {
        gridLayout.removeAllViews()
        tiles.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { handleTileClick(index) }
            gridLayout.addView(imageView)
        }
    }

    private fun handleTileClick(clickedIndex: Int) {
        if (canMove(clickedIndex, emptyTileIndex)) {
            val clickedTile = tiles[clickedIndex]
            tiles[clickedIndex] = tiles[emptyTileIndex]
            tiles[emptyTileIndex] = clickedTile
            emptyTileIndex = clickedIndex

            displayTiles()
            checkPuzzleCompletion()
        }
    }

    private fun canMove(fromIndex: Int, toIndex: Int): Boolean {
        val fromRow = fromIndex / tileCount
        val fromCol = fromIndex % tileCount
        val toRow = toIndex / tileCount
        val toCol = toIndex % tileCount
        return (fromRow == toRow && kotlin.math.abs(fromCol - toCol) == 1) ||
                (fromCol == toCol && kotlin.math.abs(fromRow - toRow) == 1)
    }

    private fun checkPuzzleCompletion() {
        for (i in 0 until tileCount * tileCount - 1) {
            val img = tiles[i]
            val current = (img.drawable as? BitmapDrawable)?.bitmap
            val expected = correctOrder[i]
            if (current != expected) return
        }
        Toast.makeText(this, "🎉 Puzzle Solved! Great job!", Toast.LENGTH_LONG).show()
    }
}
