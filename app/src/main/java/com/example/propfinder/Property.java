package com.example.propfinder;

public class Property {
    private String imageUrl;
    private String propertyName;
    private String location;
    private int price;  // Change price to String
    private int bedroomType;
    private String type;  // Add this line for the property type
    private String category; // Add this line if not already present
    private String description; // Add this line if not already present
    private int bathrooms; // Add this line if not already present
    private int balconies; // Add this line if not already present
    private int carpetArea; // Add this line if not already present

    // Empty constructor for Firestore
    public Property() {}

    // Getters
    public String getImageUrl() {
        return imageUrl;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getLocation() {
        return location;
    }

    public int getPrice() {
        return price;  // Return as String
    }

    public int getBedroomType() {
        return bedroomType;
    }

    public String getType() { // Add this method
        return type;
    }

    public String getCategory() { // Add this method
        return category;
    }

    public String getDescription() { // Add this method
        return description;
    }

    public int getBathrooms() { // Add this method
        return bathrooms;
    }

    public int getBalconies() { // Add this method
        return balconies;
    }

    public int getCarpetArea() { // Add this method
        return carpetArea;
    }
}
