package br.ufpe.cin.droa.locoweather.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class City implements Parcelable {

    private String cityName;
    private String stateName;
    private String countryName;
    private LatLng coord;
    private int currTemp;
    private int maxTemp;
    private int minTemp;
    private String weatherCondition;
    private int weatherCode;

    public City(String cityName, LatLng coord, int currTemp, int minTemp, int maxTemp, String weatherCondition, int weatherCode) {
        this.cityName = cityName;
        this.stateName = "";
        this.countryName = "";
        this.coord = coord;
        this.currTemp = currTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weatherCondition = weatherCondition;
        this.weatherCode = weatherCode;
    }

    public String getCityName() {
        return cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public LatLng getCoord() {
        return coord;
    }

    public int getCurrTemp() {
        return currTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    protected City(Parcel in) {
        cityName = in.readString();
        stateName = in.readString();
        countryName = in.readString();
        coord = (LatLng) in.readValue(LatLng.class.getClassLoader());
        currTemp = in.readInt();
        maxTemp = in.readInt();
        minTemp = in.readInt();
        weatherCondition = in.readString();
        weatherCode = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cityName);
        dest.writeString(stateName);
        dest.writeString(countryName);
        dest.writeValue(coord);
        dest.writeInt(currTemp);
        dest.writeInt(maxTemp);
        dest.writeInt(minTemp);
        dest.writeString(weatherCondition);
        dest.writeInt(weatherCode);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
