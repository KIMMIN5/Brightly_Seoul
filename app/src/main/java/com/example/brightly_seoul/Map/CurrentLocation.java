package com.example.brightly_seoul.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class CurrentLocation {
    private static final String TAG = "CurrentLocation";
    private Context context; // 애플리케이션 컨텍스트
    private GoogleMap googleMap; // Google Maps 객체
    private LocationManager locationManager; // 위치 정보를 관리하는 LocationManager 객체
    private LatLng currentLatLng; // 현재 위치의 위도와 경도
    private static final long MIN_TIME = 400; // 위치 업데이트 간 최소 시간 간격 (밀리초)
    private static final float MIN_DISTANCE = 30; // 위치 업데이트 간 최소 거리 변화 (미터)
    private boolean isFirstLocationUpdate = true; // 최초 위치 업데이트 여부를 확인하는 플래그

    public CurrentLocation(Context context, GoogleMap googleMap) {
        this.context = context;
        this.googleMap = googleMap;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // 위치 업데이트 리스너 인터페이스 정의
    public interface LocationUpdateListener {
        void onLocationUpdated(LatLng newLocation);
    }

    private LocationUpdateListener locationUpdateListener;

    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.locationUpdateListener = listener;
    }

    // 위치 리스너 초기화 및 위치 업데이트 시작
    public void initializeLocationListener() {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 위치 변경 시 실행되는 메서드
                if (googleMap != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    // 현재 위치의 위도와 경도를 로그로 출력
                    Log.d(TAG, "Current location updated: Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude());

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                    if (isFirstLocationUpdate) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
                        isFirstLocationUpdate = false;
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        // 위치 권한 확인 및 위치 업데이트 요청
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
        }
    }

    // 현재 위치의 위도와 경도 반환
    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }
}