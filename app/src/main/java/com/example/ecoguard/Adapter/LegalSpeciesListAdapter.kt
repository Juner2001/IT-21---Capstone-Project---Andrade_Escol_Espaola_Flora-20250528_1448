package com.example.ecoguard.Adapter

import android.content.Context
import android.content.Intent
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ecoguard.Domain.Species
import com.example.ecoguard.LegalDetailActivity // <-- change to your legal details activity
import com.example.ecoguard.R

class LegalSpeciesListAdapter(private val speciesList: ArrayList<Species>) :
    RecyclerView.Adapter<LegalSpeciesListAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.viewholder_list_species, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = speciesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val species = speciesList[position]
        holder.titleTxt.text = species.title
        Glide.with(context)
            .load(species.imagePath)
            .transform(CenterCrop(), RoundedCorners(30))
            .into(holder.pic)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, LegalDetailActivity::class.java)  // open LegalDetailActivity
            intent.putExtra("object", species)
            context.startActivity(intent)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTxt: TextView = view.findViewById(R.id.titleTxt)
        val pic: ImageView = view.findViewById(R.id.img)
    }
}
