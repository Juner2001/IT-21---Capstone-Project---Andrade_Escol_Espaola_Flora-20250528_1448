package com.example.ecoguard.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.ecoguard.Domain.Category;
import com.example.ecoguard.ListSpeciesActivity;
import com.example.ecoguard.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<Category> items;
    private Context context;

    // Array ng background resource IDs para sa mga category
    private final int[] backgroundResources = {
            R.drawable.cat_0_background,
            R.drawable.cat_1_background,
            R.drawable.cat_2_background,
            R.drawable.cat_3_background,
            R.drawable.cat_4_background,
            R.drawable.cat_5_background,
            R.drawable.cat_6_background,
            R.drawable.cat_7_background,
            R.drawable.cat_8_background
    };

    public CategoryAdapter(ArrayList<Category> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.viewholder_category, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = items.get(position);
        holder.titleTxt.setText(category.getName());

        if (position < backgroundResources.length) {
            holder.pic.setBackgroundResource(backgroundResources[position]);
        } else {
            holder.pic.setBackgroundResource(backgroundResources[backgroundResources.length - 1]);
        }

        // Glide with rounded corners
        RequestOptions requestOptions = new RequestOptions()
                .transform(new RoundedCorners(10)) // 10 pixels radius
                .placeholder(R.drawable.species_icon)
                .error(R.drawable.species_icon);

        Glide.with(context)
                .load(category.getImagePath())
                .apply(requestOptions)
                .into(holder.pic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ListSpeciesActivity.class);
            intent.putExtra("CategoryId", category.getId());
            intent.putExtra("CategoryName", category.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.catNameTxt);
            pic = itemView.findViewById(R.id.imgCat);
        }
    }
}
