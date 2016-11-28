package br.ufpe.cin.droa.locoweather.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import br.ufpe.cin.droa.locoweather.R;
import br.ufpe.cin.droa.locoweather.entity.City;
import br.ufpe.cin.droa.locoweather.util.CitiesListAdapter;
import br.ufpe.cin.droa.locoweather.util.Utils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private Marker mMarker;
    private FloatingActionButton mFAB;

    private LinearLayout linearLayout;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ProgressBar mProgress;
    private ListView mListView;
    private CitiesListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFAB = (FloatingActionButton) findViewById(R.id.search);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchNearbyCities();
            }
        });

        linearLayout = (LinearLayout) findViewById(R.id.bottom_sheet_container);

        mProgress = (ProgressBar) findViewById(R.id.circular_indicator);

        adapter = new CitiesListAdapter(this, R.layout.cities_item_layout, new ArrayList<City>());
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), CityDataActivity.class);
                i.putExtra("city", (City) parent.getItemAtPosition(position));

                startActivity(i);
            }
        });
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        mMap.getUiSettings().setAllGesturesEnabled(false);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mMap.getUiSettings().setAllGesturesEnabled(true);

                        mFAB.show();
                        adapter.clear();
                        mListView.setAdapter(adapter);

                        linearLayout.setVisibility(View.INVISIBLE);
                        mMap.setPadding(0, 0, 0, 0);
                        mMap.clear();
                        mMarker = null;
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior != null) {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                super.onBackPressed();
            }
        } else
            super.onBackPressed();
    }


    // Manipulates the map once available.
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mMap.getUiSettings().isRotateGesturesEnabled()) {
                    mMap.clear();
                    mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
                }
            }
        });

        // Move the camera to start position
        LatLng start = new LatLng(-8.085433, -34.886164);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
    }

    public void searchNearbyCities() {
        if (mMarker == null) {
            Toast.makeText(this, "Place a marker on the map first.", Toast.LENGTH_SHORT).show();
        } else {
            LatLng position = mMarker.getPosition();

            try {
                new RetrieveCitiesTask().execute(position.latitude, position.longitude);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class RetrieveCitiesTask extends AsyncTask<Double, Void, ArrayList<City>> {
        @Override
        protected void onPreExecute() {
            linearLayout.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);

            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mFAB.hide();
        }

        @Override
        protected ArrayList<City> doInBackground(Double... params) {
            HttpURLConnection urlConnection = null;
            ArrayList<City> result = new ArrayList<>();

            try {
                URL url = new URL(Utils.getOpenWeatherURL(params[0], params[1]));
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Accept","*/*");

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    total.append(line).append('\n');
                }

                Utils.parseWeatherDataJSON(result, total.toString());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) urlConnection.disconnect();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<City> cities) {
            super.onPostExecute(cities);

            if (cities.isEmpty()) {
                Toast.makeText(getApplication(), "An error has ocurred. Check your internet connection and try again.", Toast.LENGTH_SHORT).show();
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                // limit map bounding box to the screen's top half
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                mMap.setPadding(0, 0, 0, (displayMetrics.heightPixels) / 2);

                // obtain the minimum map bounds to display every marker
                Marker marker;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (City c : cities) {
                    adapter.add(c);

                    marker = mMap.addMarker(new MarkerOptions().position(c.getCoord()).title(c.getCityName()));
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);

                mProgress.setVisibility(View.GONE);
                mMap.animateCamera(cameraUpdate);
            }
        }
    }
}
