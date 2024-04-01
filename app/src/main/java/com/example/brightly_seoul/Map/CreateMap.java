package com.example.brightly_seoul.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreateMap {
    private GoogleMap googleMap; // Google Maps 객체
    private Context context; // 컨텍스트

    public CreateMap(GoogleMap googleMap, Context context) {
        this.googleMap = googleMap;
        this.context = context;
        initializeMap();
    }

    public void initializeMap() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        try {
            // 강남구 GeoJSON 파일에서 좌표를 불러와서 폴리곤 생성
            List<LatLng> gangnamCoordinates = getCoordinatesFromGeoJson("mapboundary/GangnamBoundary.geojson");
            createPolygon(gangnamCoordinates, Color.argb(30, 255, 0, 0)); // 강남구 경계 폴리곤 생성

            // 송파구 GeoJSON 파일에서 좌표를 불러와서 폴리곤 생성
            List<LatLng> songpaCoordinates = getCoordinatesFromGeoJson("mapboundary/SongpaBoundary.geojson");
            createPolygon(songpaCoordinates, Color.argb(30, 0, 255, 0)); // 송파구 경계 폴리곤 생성
        } catch (JSONException e) {
            Log.e("CreateMap", "Error processing GeoJSON", e);
        }
    }

    private List<LatLng> getCoordinatesFromGeoJson(String fileName) throws JSONException {
        List<LatLng> coordinates = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            String jsonStr = scanner.hasNext() ? scanner.next() : "";

            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray features = jsonObject.getJSONArray("features");
            JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");
            JSONArray coordsJsonArray = geometry.getJSONArray("coordinates").getJSONArray(0);

            for (int i = 0; i < coordsJsonArray.length(); i++) {
                JSONArray coord = coordsJsonArray.getJSONArray(i);
                double lon = coord.getDouble(0);
                double lat = coord.getDouble(1);
                coordinates.add(new LatLng(lat, lon));
                //Log.d("CreateMap", "Parsed coordinate: " + lat + ", " + lon);
            }
        } catch (Exception e) {
            Log.e("CreateMap", "Error reading GeoJSON file", e);
        }
        return coordinates;
    }

    private void createPolygon(List<LatLng> coordinates, int fillColor) {
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(coordinates)
                .fillColor(fillColor)
                .strokeColor(Color.RED)
                .strokeWidth(6f);
        googleMap.addPolygon(polygonOptions);
    }
}
