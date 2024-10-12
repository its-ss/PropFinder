package com.example.propfinder;

public class Property {
    private String image;
    private String bedrooms;
    private String location;
    private String price;

    public Property(String image, String bedrooms, String location, String price) {
        this.image = image;
        this.bedrooms = bedrooms;
        this.location = location;
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public String getBedrooms() {
        return bedrooms;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }
}
