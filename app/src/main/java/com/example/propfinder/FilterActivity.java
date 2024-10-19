package com.example.propfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FilterActivity extends AppCompatActivity {

    private ToggleButton buyButton, rentButton, pgButton, plotButton;
    private ToggleButton apartmentButton, villaButton, independentHouseButton;
    private ToggleButton oneBhkButton, twoBhkButton, threeBhkButton, fourBhkButton;
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
        backButton.setOnClickListener(v -> onBackPressed()); // Navigate back when the back button is pressed

        // Initialize UI components
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
        priceText = findViewById(R.id.priceValue);

        // Set SeekBar max to the number of price steps minus 1
        priceRange.setMax(priceSteps.length - 1);

        // Set up the seek bar listener
        priceRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedPrice = priceSteps[progress];
                priceText.setText("Selected Price: ₹" + formatPrice(selectedPrice));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Set listeners for toggle buttons to capture the selection
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

        // Submit button logic with validation
        submitButton.setOnClickListener(v -> {
            if (!isAnyFilterSelected()) {
                // Show a toast message if no filter is selected
                Toast.makeText(FilterActivity.this, "Please select at least one filter or set the price.", Toast.LENGTH_SHORT).show();
            } else {
                // Create an Intent to send data back to HomeActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedPropertyType", selectedPropertyType);
                resultIntent.putExtra("selectedHouseType", selectedHouseType);
                resultIntent.putExtra("selectedBedroom", selectedBedroom);
                resultIntent.putExtra("selectedPrice", selectedPrice);
                setResult(RESULT_OK, resultIntent);
                finish(); // Close the activity and return to HomeActivity
            }
        });
    }

    // Method to check if at least one toggle button or seek bar is selected
    private boolean isAnyFilterSelected() {
        // Check if any filters are selected or the price slider is moved (including zero)
        return buyButton.isChecked() || rentButton.isChecked() || pgButton.isChecked() || plotButton.isChecked() ||
                apartmentButton.isChecked() || villaButton.isChecked() || independentHouseButton.isChecked() ||
                oneBhkButton.isChecked() || twoBhkButton.isChecked() || threeBhkButton.isChecked() || fourBhkButton.isChecked() ||
                selectedPrice > 0;  // Price can be zero
    }


    // Method to set OnClickListener and update selection based on category
    private void setFilterClickListener(final ToggleButton button, final String category) {
        button.setOnClickListener(v -> {
            if (button.isChecked()) {
                deselectAllButtons(category);
                button.setChecked(true);
                button.setBackgroundResource(R.color.button_selected_color);
                button.setTextColor(getResources().getColor(R.color.white));

                String selectedOption = button.getText().toString();
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
            } else {
                button.setBackgroundResource(R.drawable.button_selector);
                button.setTextColor(getResources().getColor(R.color.black));
            }
        });
    }

    private void deselectAllButtons(String category) {
        switch (category) {
            case "PropertyType":
                resetToggleButton(buyButton);
                resetToggleButton(rentButton);
                resetToggleButton(pgButton);
                resetToggleButton(plotButton);
                break;

            case "HouseType":
                resetToggleButton(apartmentButton);
                resetToggleButton(villaButton);
                resetToggleButton(independentHouseButton);
                break;

            case "Bedroom":
                resetToggleButton(oneBhkButton);
                resetToggleButton(twoBhkButton);
                resetToggleButton(threeBhkButton);
                resetToggleButton(fourBhkButton);
                break;
        }
    }

    private void resetToggleButton(ToggleButton button) {
        button.setChecked(false);
        button.setBackgroundResource(R.drawable.button_selector);
        button.setTextColor(getResources().getColor(R.color.black));
    }

    private String formatPrice(int price) {
        if (price == 0) {
            return "All prices";  // Special message for zero price
        } else if (price >= 10000000) {
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
