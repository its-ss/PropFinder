package com.example.propfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView propertyList;
    private PropertyAdapter propertyAdapter;
    private List<Property> properties;
    private List<Property> filteredProperties; // New list to hold filtered properties
    private ImageView filterIcon;
    private ImageView profileImageView;
    private EditText searchEditText; // Add this line
    private static final int REQUEST_FILTER_CODE = 1; // or any unique integer

    // Firestore instance
    private FirebaseFirestore db;

    // ToggleButtons for Buy, Rent, PG, Plot
    private ToggleButton buttonBuy, buttonRent, buttonPG, buttonPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize ToggleButtons
        buttonBuy = findViewById(R.id.buttonBuy);
        buttonRent = findViewById(R.id.buttonRent);
        buttonPG = findViewById(R.id.buttonPG);
        buttonPlot = findViewById(R.id.buttonPlot);

        // Initialize the property list
        properties = new ArrayList<>();
        filteredProperties = new ArrayList<>(); // Initialize the filtered list
        propertyAdapter = new PropertyAdapter(filteredProperties, this);
        propertyList = findViewById(R.id.propertyList);
        propertyList.setLayoutManager(new LinearLayoutManager(this));
        propertyList.setAdapter(propertyAdapter);

        // Set onClickListener for each ToggleButton and pass the buttons to be managed
        setToggleButtonBehavior(buttonBuy, buttonRent, buttonPG, buttonPlot);
        setToggleButtonBehavior(buttonRent, buttonBuy, buttonPG, buttonPlot);
        setToggleButtonBehavior(buttonPG, buttonBuy, buttonRent, buttonPlot);
        setToggleButtonBehavior(buttonPlot, buttonBuy, buttonRent, buttonPG);

        // Initialize the EditText for search
        searchEditText = findViewById(R.id.searchEditText); // Initialize the search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProperties(s.toString()); // Call filter method
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });

        // Find the filter icon and set an OnClickListener
        filterIcon = findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
                startActivityForResult(intent, REQUEST_FILTER_CODE); // Use this for starting the activity for result
            }
        });

        // Find the profile ImageView and set an OnClickListener
        profileImageView = findViewById(R.id.ic_profile);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Fetch properties using Firestore Realtime Listener
        fetchPropertiesRealtime();
    }

    private void fetchPropertiesRealtime() {
        db.collection("property") // Use the correct collection name
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w("FirestoreError", "Listen failed.", e);
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        properties.clear();  // Clear the list to avoid duplicates

                        Log.d("FirestoreSuccess", "Total documents fetched: " + value.size());

                        // Iterate through all documents and add to the properties list
                        for (QueryDocumentSnapshot document : value) {
                            Property property = document.toObject(Property.class);

                            // Check if any toggle button is selected
                            if (isPropertyVisible(property)) {
                                properties.add(property);  // Add each property to the list
                                Log.d("PropertyDetails", "Property: " + property.getPropertyName());
                            }
                        }

                        // Clear the filtered list and add all properties
                        filteredProperties.clear();
                        filteredProperties.addAll(properties);
                        propertyAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("FirestoreEmpty", "No documents found.");
                    }
                });
    }

    private boolean isPropertyVisible(Property property) {
        // Check if any toggle button is checked
        boolean isBuyChecked = buttonBuy.isChecked();
        boolean isRentChecked = buttonRent.isChecked();
        boolean isPGChecked = buttonPG.isChecked();
        boolean isPlotChecked = buttonPlot.isChecked();

        // If no button is checked, show all properties
        if (!isBuyChecked && !isRentChecked && !isPGChecked && !isPlotChecked) {
            return true; // Show all properties
        }

        // Filter based on the selected toggle buttons
        if (isBuyChecked && property.getType().equalsIgnoreCase("Buy")) {
            return true;
        } else if (isRentChecked && property.getType().equalsIgnoreCase("Rent")) {
            return true;
        } else if (isPGChecked && property.getType().equalsIgnoreCase("PG")) {
            return true;
        } else if (isPlotChecked && property.getType().equalsIgnoreCase("Plot")) {
            return true;
        }

        return false; // If no button is selected or property doesn't match, don't display it
    }

    private void filterProperties(String query) {
        filteredProperties.clear(); // Clear the current filtered list
        if (query.isEmpty()) {
            // If the search query is empty, show all properties
            filteredProperties.addAll(properties);
        } else {
            // Filter properties based on the search query
            for (Property property : properties) {
                if (property.getPropertyName().toLowerCase().contains(query.toLowerCase()) ||
                        property.getLocation().toLowerCase().contains(query.toLowerCase())) {
                    filteredProperties.add(property);
                }
            }
        }
        propertyAdapter.notifyDataSetChanged(); // Notify the adapter about data change
    }

    // Method to set ToggleButton behavior for selection/deselection with mutual exclusivity
    private void setToggleButtonBehavior(final ToggleButton selectedButton, final ToggleButton... otherButtons) {
        selectedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedButton.isChecked()) {
                    // Deselect all other buttons
                    for (ToggleButton button : otherButtons) {
                        button.setChecked(false);
                        button.setBackgroundResource(android.R.color.transparent);
                        button.setTextColor(getResources().getColor(android.R.color.black));
                    }

                    // Change background to purple and text color to white when selected
                    selectedButton.setBackgroundResource(R.color.purple_700);
                    selectedButton.setTextColor(getResources().getColor(android.R.color.white));
                } else {
                    // Revert to transparent background and black text when deselected
                    selectedButton.setBackgroundResource(android.R.color.transparent);
                    selectedButton.setTextColor(getResources().getColor(android.R.color.black));
                }
                fetchPropertiesRealtime(); // Refresh properties based on the selected type
            }
        });
    }
}
