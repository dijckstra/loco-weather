package br.ufpe.cin.droa.locoweather.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufpe.cin.droa.locoweather.entity.City;
import br.ufpe.cin.droa.locoweather.entity.Place;

public class Utils {
    private static final String TAG = "Utils";
    private static final String APPID = "8c6156b6212129f47b1f665c4a330cfc";
    private static final String KEY = "AIzaSyAL_ceLXF8R6IFN_n1AVQho3g4V7fFfSxc";
    public static final HashMap<Integer, String > recommendations = new HashMap<Integer, String>() {{
        put(1, "park|amusement_park|campground|zoo|aquaerium"); // clear sky
        put(2, "park|amusement_park|campground|zoo|aquarium"); // few clouds
        put(3, "park|amusement_park|zoo|aquarium"); // scattered clouds
        put(4, "shopping_mall|bowling_alley|night_club"); // broken clouds
        put(9, "museum|art_gallery|movie_theater|library|night_club"); // shower rain
        put(10, "museum|art_gallery|movie_theater|library"); // rain
        put(11, "museum|art_gallery|movie_theater|library"); // thunderstorm
        put(13, "museum|art_gallery|movie_theater|library"); // snow
        put(50, "museum|art_gallery|movie_theater|library"); // mist
    }};

    public static String getOpenWeatherURL(double lat, double lon) {
        return "http://api.openweathermap.org/data/2.5/find?lat=" + lat + "&lon=" + lon +
                "&cnt=15&units=metric&APPID=" + APPID;
    }

    public static String getGooglePlacesURL(double lat, double lon, String type) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                lat + "," + lon + "&radius=2000" + "&rankBy=prominence&types=" + type + "&key=" + KEY;
        Log.d(TAG, url);

        return url;
    }

    public static void parseWeatherDataJSON(ArrayList<City> cities, String json) {
        String name, description;
        double lat, lon;
        int temp, temp_min, temp_max, code;

        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonObject().getAsJsonArray("list");
        JsonObject obj;

        for (JsonElement elm : array) {
            obj = elm.getAsJsonObject();

            name = obj.get("name").getAsString();
            lat = obj.get("coord").getAsJsonObject().get("lat").getAsDouble();
            lon = obj.get("coord").getAsJsonObject().get("lon").getAsDouble();
            temp = obj.get("main").getAsJsonObject().get("temp").getAsInt();
            temp_min = obj.get("main").getAsJsonObject().get("temp_min").getAsInt();
            temp_max = obj.get("main").getAsJsonObject().get("temp_max").getAsInt();
            description = obj.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("description").getAsString();
            description = WordUtils.capitalize(description);
            code = Integer.parseInt(obj.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString().substring(0, 2));

            cities.add(new City(name, new LatLng(lat, lon), temp, temp_min, temp_max, description, code));
        }

    }

    public static void parsePlaceDataJSON(ArrayList<Place> places, String json) {
        String name;
        double lat, lon;

        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonObject().getAsJsonArray("results");
        JsonObject obj;

        for (JsonElement elm : array) {
            obj = elm.getAsJsonObject();

            name = obj.get("name").getAsString();
            lat = obj.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat").getAsDouble();
            lon = obj.get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng").getAsDouble();

            places.add(new Place(name, new LatLng(lat, lon)));
        }
    }
}
