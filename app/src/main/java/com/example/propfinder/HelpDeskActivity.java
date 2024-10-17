package com.example.propfinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HelpDeskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_desk); // Set the Help Desk layout

        Button submitButton = findViewById(R.id.buttonSubmit);

        // Set click listener for the Submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Thank You page
                Intent intent = new Intent(HelpDeskActivity.this, ThankYouActivity.class);
                startActivity(intent);
            }
        });
    }
}
