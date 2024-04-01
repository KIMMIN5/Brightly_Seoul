package com.example.brightly_seoul.User;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.brightly_seoul.Admin.DataFetcher;
import com.example.brightly_seoul.Admin.LampManager;
import com.example.brightly_seoul.MainActivity;
import com.example.brightly_seoul.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EventOfLamp implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMapClickListener {
    private static final float MIN_ZOOM_LEVEL_FOR_MARKERS = 17.0f; // 마커 표시에 필요한 최소 줌 레벨
    private GoogleMap mMap; // Google Maps 객체
    private Context context; // 애플리케이션 컨텍스트
    private LampManager lampManager; // 가로등 관리자 객체
    private Marker lastSelectedMarker = null; // 마지막으로 선택된 마커
    private MainActivity mainActivity; // MainActivity 객체

    // 생성자: Context, LampManager 객체 초기화, 이벤트 리스너 설정
    public EventOfLamp(Context context, LampManager lampManager) {
        this.context = context;
        this.lampManager = lampManager;
        if (context instanceof MainActivity) {
            this.mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        // 이벤트 리스너 설정
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMapClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();

        // 가로등 마커일 경우
        if (tag instanceof DataFetcher.Streetlight) {
            DataFetcher.Streetlight selectedLight = (DataFetcher.Streetlight) tag;
            updateMarkerColor(marker, selectedLight); // 마커 색상 업데이트
            String statusText = selectedLight.getIsFaulty() ? "고장" : "정상";
            int statusColor = selectedLight.getIsFaulty() ? Color.RED : Color.GREEN;
            String reportText = selectedLight.getIsReport() ? "접수" : "미신고";
            int reportColor = selectedLight.getIsReport() ? Color.RED : Color.GREEN;

            // 가로등 상태 및 신고 여부 표시
            TextView statusTextView = mainActivity.findViewById(R.id.status_text_view);
            statusTextView.setText("상태: " + statusText);
            statusTextView.setTextColor(statusColor);
            statusTextView.setVisibility(View.VISIBLE);

            TextView reportTextView = mainActivity.findViewById(R.id.report_text_view);
            reportTextView.setText("신고 여부: " + reportText);
            reportTextView.setTextColor(reportColor);
            reportTextView.setVisibility(View.VISIBLE);

            // 신고 버튼 클릭 이벤트 설정
            Button reportButton = mainActivity.findViewById(R.id.report_button);
            reportButton.setOnClickListener(v -> reportStreetlight(selectedLight, marker));

            selectMarker(marker); // 마커 강조 및 레이아웃 표시
            showLayout();

        } else {
            hideLayout();
        }

        return true; // 이벤트 처리 완료
    }

    @Override
    public void onCameraIdle() {
        float zoomLevel = mMap.getCameraPosition().zoom;
        boolean shouldMarkersBeVisible = zoomLevel >= MIN_ZOOM_LEVEL_FOR_MARKERS;
        for (Marker marker : lampManager.getExistingMarkers().values()) {
            marker.setVisible(shouldMarkersBeVisible);
        }
    }

    // 마커 아이콘을 초기 상태로 복원하는 메소드
    private void resetMarkerIcon(Marker marker) {
        if (marker.getTag() instanceof DataFetcher.Streetlight) {
            DataFetcher.Streetlight light = (DataFetcher.Streetlight) marker.getTag();
            if (light.getIsFaulty()) {
                // 고장난 가로등 색상 설정
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else if(light.getIsReport()){
                // 고장신고접수된 가로등 색상 설정
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            } else {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
        }
    }

    // 마커 선택 시 아이콘 변경
    private void selectMarker(Marker marker) {
        if (lastSelectedMarker != null && !lastSelectedMarker.equals(marker)) {
            resetMarkerIcon(lastSelectedMarker);
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        lastSelectedMarker = marker;
    }


    @Override
    public void onMapClick(LatLng latLng) {
        // 마커 선택 해제
        if (lastSelectedMarker != null) {
            resetMarkerIcon(lastSelectedMarker);
            lastSelectedMarker = null;
        }
        // UI 요소 숨기기
        hideLayout();
    }

    // 레이아웃을 표시하는 메소드
    private void showLayout() {
        if (mainActivity != null) {
            mainActivity.findViewById(R.id.status_text_view).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.report_text_view).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.report_button).setVisibility(View.VISIBLE);
        }
    }

    // 레이아웃을 숨기는 메소드
    private void hideLayout() {
        if (mainActivity != null) {
            mainActivity.findViewById(R.id.status_text_view).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.report_text_view).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.report_button).setVisibility(View.GONE);
        }
    }

    private void reportStreetlight(DataFetcher.Streetlight streetlight, Marker marker) {
        streetlight.setIsReport(true); // 가로등 신고 상태를 true로 설정
        updateStreetlightInDatabase(streetlight); // 데이터베이스 업데이트 로직
        updateMarkerColor(marker, streetlight); // 마커 색상 재설정
    }

    private void updateMarkerColor(Marker marker, DataFetcher.Streetlight streetlight) {
        if (streetlight.getIsFaulty()) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else if (streetlight.getIsReport()) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        } else {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }
    }

    // 데이터베이스 업데이트를 위한 메서드
    private void updateStreetlightInDatabase(DataFetcher.Streetlight streetlight) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // 'streetlights' 노드 내에서 해당 가로등의 operation_id를 기반으로 경로를 생성합니다.
        // 예를 들어, operation_id가 "가락지하차도-01"이면, 경로는 "streetlights/가락지하차도-01"이 됩니다.
        String streetlightPath = "streetlights/" + streetlight.getOperation_id();

        // 'isReport' 필드를 true로 설정하여 업데이트합니다.
        Map<String, Object> updates = new HashMap<>();
        updates.put("isReport", true);

        // 데이터베이스에 변경 사항을 적용합니다.
        databaseReference.child(streetlightPath).updateChildren(updates)
                .addOnSuccessListener(aVoid -> Log.d("EventOfLamp", "Data updated successfully for " + streetlight.getOperation_id()))
                .addOnFailureListener(e -> Log.e("EventOfLamp", "Error updating data for " + streetlight.getOperation_id(), e));
    }
}