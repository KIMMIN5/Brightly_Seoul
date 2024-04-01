package com.example.brightly_seoul.Admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import java.util.HashSet;
import java.util.Set;

public class SaveMarker {
    private SharedPreferences sharedPreferences;

    // 생성자: SharedPreferences 초기화
    public SaveMarker(Context context, String tag) {
        this.sharedPreferences = context.getSharedPreferences("MarkerPref", Context.MODE_PRIVATE);
    }

    // 마커 위치를 SharedPreferences에 저장
    public void saveMarkerPosition(LatLng latLng) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> currentMarkers = new HashSet<>(sharedPreferences.getStringSet("markers", new HashSet<>()));
        String latLngString = latLng.latitude + "," + latLng.longitude;
        currentMarkers.add(latLngString);
        editor.putStringSet("markers", currentMarkers);
        editor.apply();
    }

    // 저장된 마커 위치를 SharedPreferences에서 제거
    public void removeMarkerPosition(LatLng latLngToRemove) {
        Set<String> currentMarkers = new HashSet<>(sharedPreferences.getStringSet("markers", new HashSet<>()));
        String latLngToRemoveString = latLngToRemove.latitude + "," + latLngToRemove.longitude;
        currentMarkers.remove(latLngToRemoveString);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("markers", currentMarkers);
        editor.apply();
    }

    // SharedPreferences에서 저장된 모든 마커 위치를 로드
    public Set<LatLng> loadAllMarkerPositions() {
        Set<String> markersStringSet = sharedPreferences.getStringSet("markers", new HashSet<>());
        Set<LatLng> latLngs = new HashSet<>();

        for (String markerString : markersStringSet) {
            String[] parts = markerString.split(",");
            try {
                double latitude = Double.parseDouble(parts[0]);
                double longitude = Double.parseDouble(parts[1]);
                latLngs.add(new LatLng(latitude, longitude));
            } catch (NumberFormatException e) {
                Log.e("SaveMarker", "Error parsing marker position: " + markerString, e);
            }
        }
        return latLngs;
    }
}