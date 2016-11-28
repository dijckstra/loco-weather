package br.ufpe.cin.droa.locoweather.activity;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.droa.locoweather.R;
import br.ufpe.cin.droa.locoweather.entity.City;
import br.ufpe.cin.droa.locoweather.entity.Place;
import br.ufpe.cin.droa.locoweather.util.Utils;

public class CityDataActivity extends AppCompatActivity {

    private static final String TAG = "CityDataActivity";
    private City city;
    private GoogleMap mMap;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_data);
        this.city = getIntent().getParcelableExtra("city");

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(this.city.getCityName());

        mProgressBar = (ProgressBar) findViewById(R.id.progress);

        new RequestAdditionalCityDetails().execute(city.getCoord().latitude, city.getCoord().longitude);
    }

    private class RequestAdditionalCityDetails extends AsyncTask<Double, Void, ArrayList<Place>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Place> doInBackground(Double... params) {
            Geocoder gcd = new Geocoder(getApplicationContext());
            ArrayList<Place> result = new ArrayList<>();

            try {
                List<Address> addresses = gcd.getFromLocation(params[0], params[1], 1);
                if (addresses.size() > 0) {
                    // update city data with more information
                    city.setStateName(addresses.get(0).getAdminArea());
                    city.setCountryName(addresses.get(0).getCountryName());
                }

                String json = getRecommendedPlaces(params[0], params[1], city.getWeatherCode());
                Utils.parsePlaceDataJSON(result, json);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(final ArrayList<Place> places) {
            super.onPostExecute(places);

            mProgressBar.setVisibility(View.GONE);

            String s;
            s = city.getCityName();
            if (!city.getStateName().isEmpty() && !city.getCountryName().isEmpty())
                s +=  ", " + city.getStateName() + ", " + city.getCountryName();

            TextView textView = (TextView) findViewById(R.id.city_name);
            textView.setText(s);

            textView = (TextView) findViewById(R.id.city_weather);
            textView.setText(city.getWeatherCondition());

            s = getString(R.string.temp, city.getCurrTemp());
            textView = (TextView) findViewById(R.id.temperature);
            textView.setText(s);

            s = getString(R.string.temp, city.getMaxTemp());
            textView = (TextView) findViewById(R.id.max_temp);
            textView.setText(s);

            s = getString(R.string.temp, city.getMinTemp());
            textView = (TextView) findViewById(R.id.min_temp);
            textView.setText(s);

            textView = (TextView) findViewById(R.id.recommendations);
            textView.setVisibility(View.VISIBLE);

            if(places.isEmpty()) {
                textView.setText(R.string.error_ocurred);
            } else {
                FragmentManager fragmentManager = getSupportFragmentManager();
                SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        mMap = googleMap;

                        // Move the camera to start position
                        LatLng start = city.getCoord();
                        mMap.addMarker(new MarkerOptions().position(start).title(city.getCityName()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));

                        // Place markers on the recommended places
                        for (Place p : places) {
                            mMap.addMarker(new MarkerOptions().position(p.getCoord()).title(p.getName()));
                        }

                    }
                });
                fragmentManager.beginTransaction().replace(R.id.map_container, supportMapFragment).commit();
            }
        }

        private String getRecommendedPlaces(double lat, double lon, int weatherCode) throws Exception {
            URL url = new URL(Utils.getGooglePlacesURL(lat, lon, Utils.recommendations.get(weatherCode)));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept","*/*");

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                total.append(line).append('\n');
            }

            urlConnection.disconnect();

            return total.toString();
        }
    }
}
