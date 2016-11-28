package br.ufpe.cin.droa.locoweather.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.ufpe.cin.droa.locoweather.R;
import br.ufpe.cin.droa.locoweather.entity.City;

public class CitiesListAdapter extends ArrayAdapter<City> {

    private Context mContext;
    private int mResource;
    private ArrayList<City> cities;

    public CitiesListAdapter(Context context, int resource, ArrayList<City> cities) {
        super(context, resource, cities);
        this.mContext = context;
        this.mResource = resource;
        this.cities = cities;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final City currentCity = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = ((Activity) this.mContext).getLayoutInflater().inflate(this.mResource, parent, false);

            viewHolder.cityName = (TextView) convertView.findViewById(R.id.city_name);
            viewHolder.cityWeather = (TextView) convertView.findViewById(R.id.city_weather);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.cityName.setText(currentCity.getCityName());
        viewHolder.cityWeather.setText(currentCity.getWeatherCondition());

        return convertView;
    }

    private class ViewHolder {
        TextView cityName;
        TextView cityWeather;
    }
}
