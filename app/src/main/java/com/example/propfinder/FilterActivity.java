package com.example.propfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FilterActivity extends AppCompatActivity {

    private ImageButton backButton;
    private Button buyButton, rentButton, pgButton, plotButton;
    private Button apartmentButton, villaButton, independentHouseButton;
    private Button oneBhkButton, twoBhkButton, threeBhkButton, fourBhkButton;
    private Button submitButton;
    private SeekBar priceRange;
    private TextView priceText;

    private String selectedPropertyType = "";
    private String selectedHouseType = "";
    private String selectedBedroom = "";
    private int selectedPrice = 0; // Store the selected price

    // Define price steps that correspond to the labels
    private final int[] priceSteps = {0, 100000, 500000, 1000000, 5000000, 100000000, 500000000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide the default title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Back button handling
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Navigate back when the back button is pressed
            }
        });
        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        buyButton = findViewById(R.id.buyButton);
        rentButton = findViewById(R.id.rentButton);
        pgButton = findViewById(R.id.pgButton);
        plotButton = findViewById(R.id.plotButton);

        apartmentButton = findViewById(R.id.apartmentButton);
        villaButton = findViewById(R.id.villaButton);
        independentHouseButton = findViewById(R.id.independentHouseButton);

        oneBhkButton = findViewById(R.id.oneBhkButton);
        twoBhkButton = findViewById(R.id.twoBhkButton);
        threeBhkButton = findViewById(R.id.threeBhkButton);
        fourBhkButton = findViewById(R.id.fourBhkButton);

        submitButton = findViewById(R.id.submitButton);
        priceRange = findViewById(R.id.priceRange);

        // Corrected initialization of priceText
        priceText = findViewById(R.id.priceValue);

        // Set SeekBar max to the number of price steps minus 1
        priceRange.setMax(priceSteps.length - 1);

        // Set up the seek bar listener
        priceRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Get the price from the priceSteps array based on SeekBar progress
                selectedPrice = priceSteps[progress];

                // Update the price TextView with formatted price
                priceText.setText("Selected Price: ₹" + formatPrice(selectedPrice));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set listeners for buttons to capture the selection
        setFilterClickListener(buyButton, "PropertyType");
        setFilterClickListener(rentButton, "PropertyType");
        setFilterClickListener(pgButton, "PropertyType");
        setFilterClickListener(plotButton, "PropertyType");

        setFilterClickListener(apartmentButton, "HouseType");
        setFilterClickListener(villaButton, "HouseType");
        setFilterClickListener(independentHouseButton, "HouseType");

        setFilterClickListener(oneBhkButton, "Bedroom");
        setFilterClickListener(twoBhkButton, "Bedroom");
        setFilterClickListener(threeBhkButton, "Bedroom");
        setFilterClickListener(fourBhkButton, "Bedroom");

        // Submit button logic
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensure all fields are filled before proceeding
                if (selectedPropertyType.isEmpty() || selectedHouseType.isEmpty() || selectedBedroom.isEmpty() || selectedPrice == 0) {
                    // Show toast if any required field is missing
                    Toast.makeText(FilterActivity.this, "Please select all options.", Toast.LENGTH_SHORT).show();
                } else {
                    // Show toast with all selected data if everything is filled
                    String message = "Selected: Property Type: " + selectedPropertyType +
                            ", House Type: " + selectedHouseType +
                            ", Bedroom: " + selectedBedroom +
                            ", Price: ₹" + formatPrice(selectedPrice);
                    Toast.makeText(FilterActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Method to set OnClickListener and update selection based on category
    private void setFilterClickListener(Button button, final String category) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedOption = button.getText().toString();

                // Update the respective selection
                switch (category) {
                    case "PropertyType":
                        selectedPropertyType = selectedOption;
                        break;
                    case "HouseType":
                        selectedHouseType = selectedOption;
                        break;
                    case "Bedroom":
                        selectedBedroom = selectedOption;
                        break;
                }
            }
        });
    }

    // Helper function to format the price as a string (k for thousands, L for lakhs, Cr for crores)
    private String formatPrice(int price) {
        if (price >= 10000000) {
            return (price / 10000000) + " Cr";
        } else if (price >= 100000) {
            return (price / 100000) + " L";
        } else if (price >= 1000) {
            return (price / 1000) + "k";
        } else {
            return String.valueOf(price);
        }
    }
}
