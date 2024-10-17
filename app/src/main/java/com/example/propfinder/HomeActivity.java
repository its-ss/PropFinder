package com.example.propfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView propertyList;
    private PropertyAdapter propertyAdapter;
    private List<Property> properties;
    private List<Property> filteredProperties; // List to hold filtered properties
    private ImageView filterIcon;
    private ImageView profileImageView;
    private EditText searchEditText; // Search EditText
    private static final int REQUEST_FILTER_CODE = 1; // Unique integer for filter request

    // Firestore instance
    private FirebaseFirestore db;

    // ToggleButtons for property types
    private ToggleButton buttonAll, buttonBuy, buttonRent, buttonPG, buttonPlot;

    // Filter variables
    private String selectedPropertyType = "";
    private String selectedHouseType = "";
    private String selectedBedroom = "";
    private int selectedPrice = 0; // Store the selected price

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize ToggleButtons
        buttonAll = findViewById(R.id.buttonAll);
        buttonBuy = findViewById(R.id.buttonBuy);
        buttonRent = findViewById(R.id.buttonRent);
        buttonPG = findViewById(R.id.buttonPG);
        buttonPlot = findViewById(R.id.buttonPlot);

        // Set the "All" button to checked by default
        buttonAll.setChecked(true);
        updateToggleButtonUI(); // Update the UI for selected buttons

        // Initialize the property list
        properties = new ArrayList<>();
        filteredProperties = new ArrayList<>(); // Initialize the filtered list
        propertyAdapter = new PropertyAdapter(filteredProperties, this);
        propertyList = findViewById(R.id.propertyList);
        propertyList.setLayoutManager(new LinearLayoutManager(this));
        propertyList.setAdapter(propertyAdapter);

        // Set onClickListener for each ToggleButton
        setToggleButtonBehavior(buttonAll, buttonBuy, buttonRent, buttonPG, buttonPlot);
        setToggleButtonBehavior(buttonBuy, buttonAll, buttonRent, buttonPG, buttonPlot);
        setToggleButtonBehavior(buttonRent, buttonAll, buttonBuy, buttonPG, buttonPlot);
        setToggleButtonBehavior(buttonPG, buttonAll, buttonBuy, buttonRent, buttonPlot);
        setToggleButtonBehavior(buttonPlot, buttonAll, buttonBuy, buttonRent, buttonPG);

        // Initialize the EditText for search
        searchEditText = findViewById(R.id.searchEditText); // Initialize the search EditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProperties(s.toString()); // Call filter method
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Find the filter icon and set an OnClickListener
        filterIcon = findViewById(R.id.filterIcon);
        filterIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
            startActivityForResult(intent, REQUEST_FILTER_CODE); // Start the filter activity for result
        });

        // Find the profile ImageView and set an OnClickListener
        profileImageView = findViewById(R.id.ic_profile);
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Fetch properties using Firestore Realtime Listener
        fetchPropertiesRealtime();
    }

    private void fetchPropertiesRealtime() {
        db.collection("property") // Use the correct collection name
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w("FirestoreError", "Listen failed.", e);
                        Toast.makeText(HomeActivity.this, "Failed to fetch properties.", Toast.LENGTH_SHORT).show();
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
                                properties.add(property); // Add each property to the list
                                Log.d("PropertyDetails", "Property: " + property.getPropertyName());
                            }
                        }

                        // Clear the filtered list and add all properties
                        filteredProperties.clear();
                        filteredProperties.addAll(properties);
                        propertyAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("FirestoreEmpty", "No documents found.");
                        Toast.makeText(HomeActivity.this, "No properties found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isPropertyVisible(Property property) {
        // Check if any toggle button is checked
        boolean isAllChecked = buttonAll.isChecked();
        boolean isBuyChecked = buttonBuy.isChecked();
        boolean isRentChecked = buttonRent.isChecked();
        boolean isPGChecked = buttonPG.isChecked();
        boolean isPlotChecked = buttonPlot.isChecked();

        // If "All" button is checked, show all properties
        if (isAllChecked) {
            return true; // Show all properties
        }

        // Filter based on the selected toggle buttons
        return (isBuyChecked && property.getType().equalsIgnoreCase("Buy")) ||
                (isRentChecked && property.getType().equalsIgnoreCase("Rent")) ||
                (isPGChecked && property.getType().equalsIgnoreCase("PG")) ||
                (isPlotChecked && property.getType().equalsIgnoreCase("Plot"));
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
        if (filteredProperties.isEmpty()) {
            Toast.makeText(this, "No matching properties found.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the result from FilterActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILTER_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Get filter selections from the Intent
                selectedPropertyType = data.getStringExtra("selectedPropertyType");
                selectedHouseType = data.getStringExtra("selectedHouseType");
                selectedBedroom = data.getStringExtra("selectedBedroom");
                selectedPrice = data.getIntExtra("selectedPrice", 0);

                // Reapply filters
                applyFilters();
            }
        }
    }

    private void applyFilters() {
        filteredProperties.clear(); // Clear the current filtered list
        for (Property property : properties) {
            // Check if the property matches the selected filters
            boolean matchesFilters = true;

            // Check property type
            if (!selectedPropertyType.isEmpty() && !property.getType().equalsIgnoreCase(selectedPropertyType)) {
                matchesFilters = false;
            }

            // Check house type
            if (!selectedHouseType.isEmpty() && !property.getCategory().equalsIgnoreCase(selectedHouseType)) {
                matchesFilters = false;
            }

            // Check bedroom count
            if (!selectedBedroom.isEmpty() && property.getBedroomType() != Integer.parseInt(selectedBedroom.split(" ")[0])) {
                matchesFilters = false;
            }

            // Check price
            if (selectedPrice > 0 && property.getPrice() > selectedPrice) {
                matchesFilters = false;
            }

            if (matchesFilters) {
                filteredProperties.add(property); // Add matching property to filtered list
            }
        }
        propertyAdapter.notifyDataSetChanged(); // Notify the adapter about data change

        // Update ToggleButton UI after applying filters
        updateToggleButtonUI();

        if (filteredProperties.isEmpty()) {
            Toast.makeText(this, "No properties match the selected filters.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to set ToggleButton behavior for selection/deselection with mutual exclusivity
    private void setToggleButtonBehavior(final ToggleButton selectedButton, final ToggleButton... otherButtons) {
        selectedButton.setOnClickListener(v -> {
            if (selectedButton == buttonAll) {
                // Ensure "All" button is non-deselectable
                if (!buttonAll.isChecked()) {
                    buttonAll.setChecked(true); // Force it to stay selected
                }
                deselectOtherButtons(otherButtons);
                fetchPropertiesRealtime(); // Fetch and display all properties
            } else {
                if (selectedButton.isChecked()) {
                    // When a specific button is selected, deselect the "All" button and other buttons
                    buttonAll.setChecked(false);
                    deselectOtherButtons(otherButtons);
                    fetchPropertiesRealtime();
                } else {
                    // When any specific button is deselected, select "All" button again
                    buttonAll.setChecked(true);
                    updateToggleButtonUI(); // Update UI for all buttons
                    fetchPropertiesRealtime(); // Reload all properties
                }
            }
            updateToggleButtonUI(); // Always update UI after any button click
        });
    }


    private void deselectOtherButtons(ToggleButton... buttons) {
        // Loop through and deselect the other toggle buttons
        for (ToggleButton button : buttons) {
            button.setChecked(false);
            button.setBackgroundResource(android.R.color.transparent);
            button.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void updateToggleButtonUI() {
        // Update the UI for each ToggleButton based on its checked state
        if (buttonAll.isChecked()) {
            buttonAll.setBackgroundResource(R.color.purple_700);
            buttonAll.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            buttonAll.setBackgroundResource(android.R.color.transparent);
            buttonAll.setTextColor(getResources().getColor(android.R.color.black));
        }

        // Repeat for other buttons...
        if (buttonBuy.isChecked()) {
            buttonBuy.setBackgroundResource(R.color.purple_700);
            buttonBuy.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            buttonBuy.setBackgroundResource(android.R.color.transparent);
            buttonBuy.setTextColor(getResources().getColor(android.R.color.black));
        }

        if (buttonRent.isChecked()) {
            buttonRent.setBackgroundResource(R.color.purple_700);
            buttonRent.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            buttonRent.setBackgroundResource(android.R.color.transparent);
            buttonRent.setTextColor(getResources().getColor(android.R.color.black));
        }

        if (buttonPG.isChecked()) {
            buttonPG.setBackgroundResource(R.color.purple_700);
            buttonPG.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            buttonPG.setBackgroundResource(android.R.color.transparent);
            buttonPG.setTextColor(getResources().getColor(android.R.color.black));
        }

        if (buttonPlot.isChecked()) {
            buttonPlot.setBackgroundResource(R.color.purple_700);
            buttonPlot.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            buttonPlot.setBackgroundResource(android.R.color.transparent);
            buttonPlot.setTextColor(getResources().getColor(android.R.color.black));
        }
    }


    private void updateButtonUI(ToggleButton button) {
        if (button.isChecked()) {
            button.setBackgroundResource(R.color.purple_700);
            button.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            button.setBackgroundResource(android.R.color.transparent);
            button.setTextColor(getResources().getColor(android.R.color.black));
        }
    }
}
