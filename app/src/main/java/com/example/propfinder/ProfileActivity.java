package com.example.propfinder;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhoneNumber, etPassword;
    private Button btnUpdate, btnLogout;
    private CircleImageView imageView;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImage;
    private ProgressDialog dialog;
    private DatabaseReference profileReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize FirebaseAuth, FirebaseDatabase, and FirebaseStorage instances
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        profileReference = database.getReference("Profiles");

        // Initialize UI elements
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etPassword = findViewById(R.id.etPassword);
        btnUpdate = findViewById(R.id.btnSignUp);
        btnLogout = findViewById(R.id.etlgt);
        imageView = findViewById(R.id.ImageViewId);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);

        // Prepopulate fields with existing user data
        if (user != null) {
            etFullName.setText(user.getDisplayName());
            etEmail.setText(user.getEmail());
            loadUserProfileImage(user.getEmail());  // Load image when the page opens
        }

        // Set image click listener to choose an image
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 45);
        });

        // Update button listener
        btnUpdate.setOnClickListener(v -> {
            dialog.show();
            updateUserProfile();
        });

        // Logout button listener
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            finish(); // Close the profile activity
        });
    }

    // Load the user's profile image from Firebase when the page opens
    private void loadUserProfileImage(String email) {
        profileReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot profileSnapshot : dataSnapshot.getChildren()) {
                        String imageName = profileSnapshot.child("image_name").getValue(String.class);

                        if (imageName != null && !imageName.isEmpty()) {
                            // Fetch the image from Firebase Storage using the image name
                            StorageReference imageRef = storage.getReference().child("Profiles").child(imageName);
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Load the image using Picasso into the ImageView
                                Picasso.get().load(uri).into(imageView);
                            }).addOnFailureListener(e -> {
                                // Handle failure to retrieve the image
                                Toast.makeText(ProfileActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Image name is empty or not found
                            Toast.makeText(ProfileActivity.this, "Please upload a profile image", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // No profile found with matching email
                    Toast.makeText(ProfileActivity.this, "Please upload a profile image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to retrieve profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            dialog.dismiss();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            dialog.dismiss();
            return;
        }

        // Update display name
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile name updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Profile update failed", Toast.LENGTH_SHORT).show();
                }
            });

            // Update email
            user.updateEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Email update failed", Toast.LENGTH_SHORT).show();
                }
            });

            // Update password if provided
            if (!TextUtils.isEmpty(password)) {
                user.updatePassword(password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Password update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Upload image if selected and save profile info
            if (selectedImage != null) {
                uploadImageAndSaveUserProfile();
            } else {
                dialog.dismiss();
            }
        }
    }

    private void uploadImageAndSaveUserProfile() {
        // Get the current authenticated user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Ensure the user is authenticated
        if (currentUser == null) {
            dialog.dismiss();
            Toast.makeText(ProfileActivity.this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the image name from the URI (file path)
        String imageName = getFileName(selectedImage);

        // Ensure imageName is not null
        if (imageName == null) {
            dialog.dismiss();
            Toast.makeText(ProfileActivity.this, "Invalid image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the storage path where the image will be uploaded
        StorageReference reference = storage.getReference().child("Profiles").child(imageName);

        // Upload the image to Firebase Storage
        reference.putFile(selectedImage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String email = etEmail.getText().toString().trim();

                    // Ensure email is valid
                    if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        dialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Please provide a valid email", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if email already exists in Firebase Realtime Database
                    DatabaseReference profilesRef = database.getReference().child("Profiles");
                    profilesRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Email exists, update image_name
                                for (DataSnapshot profileSnapshot : dataSnapshot.getChildren()) {
                                    // Update the existing image_name field
                                    profileSnapshot.getRef().child("image_name").setValue(imageName)
                                            .addOnSuccessListener(aVoid -> {
                                                dialog.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                dialog.dismiss();
                                                Toast.makeText(ProfileActivity.this, "Failed to update profile image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                }
                            } else {
                                // Email does not exist, create a new record
                                DatabaseReference newProfileRef = profilesRef.child(currentUser.getUid());
                                newProfileRef.child("email").setValue(email)
                                        .addOnSuccessListener(aVoid -> {
                                            newProfileRef.child("image_name").setValue(imageName)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(ProfileActivity.this, "Profile created successfully", Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(ProfileActivity.this, "Failed to save image name: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(ProfileActivity.this, "Failed to save email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                });
            } else {
                dialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to get the file name from URI
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
        }
    }
}
