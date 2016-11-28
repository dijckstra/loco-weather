package br.ufpe.cin.droa.locoweather.entity;

import com.google.android.gms.maps.model.LatLng;

public class Place {

    private String name;
    private LatLng coord;

    public Place(String name, LatLng coord) {
        this.name = name;
        this.coord = coord;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCoord() {
        return coord;
    }

}
