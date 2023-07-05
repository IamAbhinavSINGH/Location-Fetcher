package com.Abhinav.locationfetcher;

public class LocationHelper {

    private String Latitude;
    private String Longitude;
    private String Address;


    public LocationHelper(String latitude, String longitude, String address) {
        Latitude = latitude;
        Longitude = longitude;
        Address = address;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }


    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

}
