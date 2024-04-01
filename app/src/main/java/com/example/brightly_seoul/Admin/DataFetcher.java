package com.example.brightly_seoul.Admin;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DataFetcher {
    private static DataFetcher instance;
    private FirebaseDatabase database;
    private Map<String, Streetlight> streetlights = new HashMap<>();
    private List<DataChangeListener> listeners = new ArrayList<>();

    public interface DataChangeListener {
        void onDataChanged(Map<String, Streetlight> streetlights);
        void onDataLoadComplete();
    }

    private DataFetcher() {
        Log.d("DataFetcher", "Initializing DataFetcher...");
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        database = FirebaseDatabase.getInstance();
        Log.d("DataFetcher", "FirebaseDatabase instance obtained.");
        loadStreetlightData();
    }

    public static synchronized DataFetcher getInstance() {
        if (instance == null) {
            Log.d("DataFetcher", "Creating new DataFetcher instance...");
            instance = new DataFetcher();
        } else {
            Log.d("DataFetcher", "Returning existing DataFetcher instance.");
        }
        return instance;
    }

    public void addDataChangeListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Log.d("DataFetcher", "DataChangeListener added: " + listener);
        } else {
            Log.d("DataFetcher", "Attempted to add existing DataChangeListener: " + listener);
        }
    }

    public void removeDataChangeListener(DataChangeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
            Log.d("DataFetcher", "DataChangeListener removed: " + listener);
        } else {
            Log.d("DataFetcher", "Attempted to remove non-existent DataChangeListener: " + listener);
        }
    }

    private void loadStreetlightData() {
        DatabaseReference streetlightsRef = database.getReference("streetlights");
        Log.d("DataFetcher", "Setting up ValueEventListener for /streetlights");

        streetlightsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("DataFetcher", "onDataChange received: " + dataSnapshot.getChildrenCount() + " items");
                Map<String, Streetlight> updatedLights = new HashMap<>();

                for (DataSnapshot lightSnapshot : dataSnapshot.getChildren()) {
                    Streetlight light = lightSnapshot.getValue(Streetlight.class);
                    if (light != null) {
                        updatedLights.put(lightSnapshot.getKey(), light);
                        Log.d("DataFetcher", "Streetlight loaded: " + lightSnapshot.getKey());
                    } else {
                        Log.d("DataFetcher", "Null Streetlight object at: " + lightSnapshot.getKey());
                    }
                }

                streetlights.clear();
                streetlights.putAll(updatedLights);
                notifyDataChanged();
                Log.d("DataFetcher", "Data change notification sent to listeners.");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DataFetcher", "DatabaseError: " + databaseError.getMessage());
            }
        });
    }

    private void notifyDataChanged() {
        Log.d("DataFetcher", "Notifying " + listeners.size() + " listeners about data change.");
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged(streetlights);
            Log.d("DataFetcher", "Notified listener: " + listener.toString());
        }
    }

    public Map<String, Streetlight> getStreetlights() {
        return streetlights;
    }

    public static class Streetlight {
        public boolean isFaulty;
        public boolean isReport;
        public double latitude;
        public double longitude;
        public String operation_id;

        // Getter and Setter Methods
        public boolean getIsFaulty() { return isFaulty; }
        public void setIsFaulty(boolean isFaulty) { this.isFaulty = isFaulty; }
        public boolean getIsReport() { return isReport; }
        public void setIsReport(boolean isReport) { this.isReport =
                isReport = isReport; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public String getOperation_id() { return operation_id; }
        public void setOperation_id(String operation_id) { this.operation_id = operation_id; }
    }
}