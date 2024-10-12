package com.example.propfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView propertyList;
    private PropertyAdapter propertyAdapter;
    private List<Property> properties;
    private ImageView filterIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        propertyList = findViewById(R.id.propertyList);
        propertyList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the properties list and add sample data
        properties = new ArrayList<>();
        properties.add(new Property("@drawable/ic_property_image", "3BHK", "Manipal", "1000"));
        properties.add(new Property("@drawable/ic_property_image", "2BHK", "Udupi", "800"));
        // Add more properties as needed

        // Set up the adapter
        propertyAdapter = new PropertyAdapter(properties, this);
        propertyList.setAdapter(propertyAdapter);

        // Find the filter icon and set an OnClickListener
        filterIcon = findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
                startActivity(intent);
            }
        });
    }
}
