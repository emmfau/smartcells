package com.lesfriches.smartcells.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lesfriches.smartcells.R;
import com.lesfriches.smartcells.model.Location;

import java.util.ArrayList;

/**
 * Adapter for bidirectional binding.
 * Used in Dashboard activity for location list, and personalize location matches
 */
public class LocationAdapter extends ArrayAdapter<Location> {

    private Context context;
    private int resourceId;

    public LocationAdapter(Context c, int r, ArrayList<Location> ll) {
        super(c, r, ll);
        context = c;
        resourceId = r;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Location location = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }
        TextView bt = (TextView) convertView.findViewById(R.id.locationName);
        String locationText = location.name;
        if (location.locationMatch.currentCommonLocationsPercent != -1) {
            locationText += " : C-" + location.locationMatch.currentCommonLocationsPercent + "%";
        }
        if (location.locationMatch.currentMatchPercent != -1) {
            locationText += " : M-" + location.locationMatch.currentMatchPercent + "%";
        }
        bt.setText(locationText);

        ImageView iv = (ImageView) convertView.findViewById(R.id.locationMatch);
        if (location.locationMatch.currentMatchPercent > 80 && location.locationMatch.currentCommonLocationsPercent > 60) {
            iv.setVisibility(View.VISIBLE);
        }
        else {
            iv.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
