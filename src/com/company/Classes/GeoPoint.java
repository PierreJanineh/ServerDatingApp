package com.company.Classes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;

public class GeoPoint {

    /*
    GeoPoint class in Android project contains:
                  1. lat        float
                  2. lng        float
     */

    private float lat, lng;

    public GeoPoint(float lat, float lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public GeoPoint(InputStream inputStream) throws IOException{
        int jsonLength = inputStream.read();
        if (jsonLength == -1)
            throw new IOException("json hasn't been sent");
        byte[] jsonBytes = new byte[jsonLength];
        int actuallyRead = inputStream.read(jsonBytes);
        if (actuallyRead != jsonLength)
            throw new IOException("");
        GeoPoint jsonGeoPoint = getGeoPointFromJson(new String(jsonBytes));
        this.lat = jsonGeoPoint.getLat();
        this.lng = jsonGeoPoint.getLng();
    }

    /**
     * Get GeoPoint object from Json String.
     * @param json
     * Json String.
     * @return
     * GeoPoint Object.
     */
    public static GeoPoint getGeoPointFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, GeoPoint.class);
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }
}
