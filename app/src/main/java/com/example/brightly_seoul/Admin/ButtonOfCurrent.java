package com.example.brightly_seoul.Admin;

import com.example.brightly_seoul.Map.CurrentLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ButtonOfCurrent {
    private CurrentLocation currentLocation; // 사용자의 현재 위치를 관리하는 객체
    private GoogleMap mMap; // Google Maps 객체
    private SaveMarker saveMarker; // 마커 위치를 저장하는 객체

    // 생성자: 현재 위치, GoogleMap 객체, 마커 저장 객체를 초기화
    public ButtonOfCurrent(CurrentLocation currentLocation, GoogleMap googleMap, SaveMarker saveMarker) {
        this.currentLocation = currentLocation;
        this.mMap = googleMap;
        this.saveMarker = saveMarker;
    }

    // 현재 위치에 마커를 추가하는 메서드
    public void addMarkerAtCurrentLocation() {
        if (mMap != null && currentLocation != null) {
            LatLng currentLatLng = currentLocation.getCurrentLatLng();
            if (currentLatLng != null) {
                // 현재 위치에 새 마커 생성 및 타이틀 설정
                Marker marker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("현재 위치"));
                marker.setTag("currentLocation"); // 현재 위치 마커 구별을 위한 태그 설정

                // 마커 위치를 SaveMarker 객체를 통해 저장
                saveMarker.saveMarkerPosition(currentLatLng);

                // 지도 카메라를 현재 위치로 이동 및 줌 조절
                float currentZoom = mMap.getCameraPosition().zoom;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, currentZoom));
            }
        }
    }
}