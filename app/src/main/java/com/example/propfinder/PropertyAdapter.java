package com.example.propfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private List<Property> propertyList;
    private Context context;

    public PropertyAdapter(List<Property> propertyList, Context context) {
        this.propertyList = propertyList;
        this.context = context;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);

        holder.bedroomText.setText(property.getBedroomType() + " BHK");
        holder.locationText.setText(property.getLocation());
        holder.priceText.setText("₹" + property.getPrice());
        holder.propertyNameText.setText(property.getPropertyName()); // Set property name

        // Load image using Glide
        Glide.with(context)
                .load(property.getImageUrl())
                .placeholder(R.drawable.ic_property_image)
                .into(holder.propertyImage);
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    public static class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView propertyImage;
        TextView bedroomText;
        TextView locationText;
        TextView priceText;
        TextView propertyNameText; // Add this line for the property name

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            propertyImage = itemView.findViewById(R.id.propertyImage);
            bedroomText = itemView.findViewById(R.id.bedroomText);
            locationText = itemView.findViewById(R.id.locationText);
            priceText = itemView.findViewById(R.id.priceText);
            propertyNameText = itemView.findViewById(R.id.propertyNameText); // Initialize property name
        }
    }
}
