package com.example.tt.backtrack;

import android.net.Uri;

import java.io.File;

/* Defines a type class that stores all necessary information about a location, and provides
 * the appropriate methods to retrieve the information
 */
public class ItemType {
    private String itemName;
    private String imageLocation;
    private double latitude;
    private double longitude;

    public ItemType(String s, double lat, double lon, String image){
        itemName = s;
        latitude = lat;
        longitude = lon;
        imageLocation = image;
    }

    public String getName(){
        return itemName;
    }

    public double getLat(){
        return latitude;
    }

    public double getLong(){
        return longitude;
    }

    public String getPath() {
        return imageLocation;
    }

    public Uri getUri() {
        return Uri.fromFile(new File(imageLocation));
    }
}
