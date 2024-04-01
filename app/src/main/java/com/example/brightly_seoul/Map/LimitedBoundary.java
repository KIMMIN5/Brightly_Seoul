package com.example.brightly_seoul.Map;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

public class LimitedBoundary {
    private GoogleMap mMap; // Google Maps 객체
    private LatLngBounds bounds; // 제한할 지역의 경계
    private Context context; // 컨텍스트

    // 생성자: GoogleMap 객체, 제한할 경계, 컨텍스트 설정
    public LimitedBoundary(GoogleMap googleMap, LatLngBounds bounds, Context context) {
        this.mMap = googleMap;
        this.bounds = bounds;
        this.context = context;

        // 지도의 카메라 이동 리스너 설정
        setCameraMoveListener();
    }

    // 지도 카메라 이동 리스너 설정 메서드
    private void setCameraMoveListener() {
        mMap.setOnCameraMoveListener(() -> {
            // 카메라의 현재 위치가 설정된 경계를 벗어나는 경우 확인
            if (!bounds.contains(mMap.getCameraPosition().target)) {
                // 사용자에게 알림 표시
                Toast.makeText(context, "해당 구를 벗어났습니다.", Toast.LENGTH_SHORT).show();

                // 카메라를 경계의 중앙으로 재설정하는 코드를 비활성화
                // mMap.moveCamera(CameraUpdateFactory.newLatLng(bounds.getCenter()));
            }
        });
    }
}