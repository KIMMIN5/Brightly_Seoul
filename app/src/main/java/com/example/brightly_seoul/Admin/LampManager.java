package com.example.brightly_seoul.Admin;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

public class LampManager implements DataFetcher.DataChangeListener {
    private GoogleMap mMap; // Google Maps 객체
    private DataFetcher dataFetcher; // 가로등 데이터를 가져오는 DataFetcher 인스턴스
    private HashMap<String, Marker> existingMarkers = new HashMap<>(); // 이미 추가된 마커를 관리하는 맵
    private Context context; // 앱 컨텍스트

    public LampManager(GoogleMap map, Context context) {
        this.mMap = map;
        this.context = context;
        this.dataFetcher = DataFetcher.getInstance();
        dataFetcher.addDataChangeListener(this);
        Log.d("LampManager", "LampManager initialized. DataFetcher instance obtained.");
    }

    @Override
    public void onDataChanged(Map<String, DataFetcher.Streetlight> updatedLights) {
        Log.d("LampManager", "onDataChanged received. Total lights: " + updatedLights.size());
        for (Map.Entry<String, DataFetcher.Streetlight> entry : updatedLights.entrySet()) {
            String id = entry.getKey();
            DataFetcher.Streetlight light = entry.getValue();
            LatLng position = new LatLng(light.getLatitude(), light.getLongitude());
            Marker marker = existingMarkers.get(id);

            if (marker != null) {
                Log.d("LampManager", "Updating marker: " + id);
                updateMarkerColorAndTag(marker, light);
            } else {
                Log.d("LampManager", "Adding marker: " + id + " at " + position);
                addMarkerToMap(id, position, light);
            }
        }
        Log.d("LampManager", "All markers updated/added based on new data.");
    }

    @Override
    public void onDataLoadComplete() {
        Toast.makeText(context, "Streetlight data loaded successfully.", Toast.LENGTH_SHORT).show();
        Log.d("LampManager", "Data load complete. Toast displayed.");
    }

    private void updateMarkerColorAndTag(Marker marker, DataFetcher.Streetlight light) {
        Log.d("LampManager", "Updating marker color and tag for " + marker.getId());
        if (light.getIsFaulty()) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Log.d("LampManager", marker.getId() + " set to RED.");
        } else if (light.getIsReport()) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            Log.d("LampManager", marker.getId() + " set to VIOLET.");
        } else {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Log.d("LampManager", marker.getId() + " set to GREEN.");
        }
        marker.setTag(light);
        Log.d("LampManager", "Marker " + marker.getId() + " tag updated.");
    }

    private void addMarkerToMap(String id, LatLng position, DataFetcher.Streetlight light) {
        Log.d("LampManager", "Adding new marker to map: " + id);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(id)
                .icon(light.getIsFaulty() ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) :
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        Marker marker = mMap.addMarker(markerOptions);
        existingMarkers.put(id, marker);
        Log.d("LampManager", "New marker added: " + id + " at " + position);
    }

    public HashMap<String, Marker> getExistingMarkers() {
        Log.d("LampManager", "Existing markers requested. Total: " + existingMarkers.size());
        return existingMarkers;
    }
}
