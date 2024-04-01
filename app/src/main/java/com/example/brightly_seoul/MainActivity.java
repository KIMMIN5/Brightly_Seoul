package com.example.brightly_seoul;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;

//import com.example.brightly.databinding.BrightlyLayoutBinding;
import com.example.brightly_seoul.Admin.ButtonOfCurrent;
import com.example.brightly_seoul.Admin.DataFetcher;
import com.example.brightly_seoul.Admin.LampManager;
import com.example.brightly_seoul.Map.CreateMap;
import com.example.brightly_seoul.Map.CurrentLocation;
import com.example.brightly_seoul.Map.DayAndNight;
import com.example.brightly_seoul.Map.LimitedBoundary;
import com.example.brightly_seoul.User.ButtonOfReport;
import com.example.brightly_seoul.User.EventOfLamp;
import com.example.brightly_seoul.User.Permissions;
import com.example.brightly_seoul.Admin.SaveMarker;
import com.example.brightly_seoul.Admin.SharedPreferencesExporter;
import com.example.brightly_seoul.databinding.BrightlyLayoutBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,
        Permissions.LocationPermissionHandler, CurrentLocation.LocationUpdateListener {

    private BrightlyLayoutBinding binding; // 레이아웃 바인딩 객체
    private GoogleMap mMap; // Google Maps 객체
    private CurrentLocation currentLocation; // 현재 위치 관리 객체
    private ButtonOfCurrent buttonOfCurrent; // 현재 위치에 마커를 추가하는 버튼 관리 객체
    private LimitedBoundary limitedBoundary; // 지도 카메라 이동 제한 관리 객체
    private TextView tailTextView; // 텍스트 뷰 객체 (예: 상태 메시지 표시)
    private SaveMarker saveMarker; // 마커 저장 관리 객체
    private Marker selectedMarker; // 현재 선택된 마커
    private Button deleteMarkerButton; // 마커 삭제 버튼
    private Button exportButton; // 데이터 내보내기 버튼
    private Button reportButton; // 긴급 신고 버튼
    private LampManager lampManager; // 가로등 관리 객체
    private EventOfLamp eventOfLamp; // 가로등 이벤트 처리 객체
    private Context context;

    // 액티비티 생성 시 호출되는 메서드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 뷰 바인딩을 사용하여 레이아웃 초기화
        binding = BrightlyLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 지도 프래그먼트 초기화 및 지도 준비
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // 지도가 준비되면 onMapReady 콜백이 호출됨
        }

        // 마커 저장을 위한 SaveMarker 인스턴스 초기화
        saveMarker = new SaveMarker(this, "currentLocation");

        // 긴급 신고 버튼 초기화 및 이벤트 리스너 설정
        reportButton = findViewById(R.id.buttonEmergencyReport);
        new ButtonOfReport(this, reportButton); // 신고 버튼 기능 연결

        // 현재 위치에 마커를 추가하는 버튼 초기화 및 이벤트 리스너 설정
        Button currentLocationButton = findViewById(R.id.buttonCurrentLocation);
        currentLocationButton.setOnClickListener(v -> buttonOfCurrent.addMarkerAtCurrentLocation());

        // 마커 삭제 버튼 초기화 및 이벤트 리스너 설정
        deleteMarkerButton = findViewById(R.id.delete_marker_button);
        deleteMarkerButton.setOnClickListener(v -> {
            if (selectedMarker != null) {
                LatLng markerPosition = selectedMarker.getPosition();
                selectedMarker.remove(); // 선택된 마커 삭제
                saveMarker.removeMarkerPosition(markerPosition); // 저장된 마커 위치 삭제
                Log.d("MainActivity", "Marker deleted: " + markerPosition);
                selectedMarker = null; // 선택된 마커 초기화
            }
        });

        // 데이터 내보내기 버튼 초기화 및 이벤트 리스너 설정
        exportButton = findViewById(R.id.export_button);
        exportButton.setOnClickListener(v -> exportSharedPreferences()); // 설정 데이터 내보내기 기능 연결

        // 위치 권한이 이미 부여되었는지 확인 및 위치 관련 기능 초기화
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initializeLocationRelatedStuff();
        }

        // 위치 권한 확인 및 요청
        Permissions.checkLocationPermission(this);

        // 전화 권한 확인 및 요청
        Permissions.checkCallPhonePermission(this);
    }

    // 위치 관련 기능 초기화 메서드
    public void initializeLocationRelatedStuff() {
        // 지도가 초기화되었고, 위치 권한이 부여된 경우에만 위치 관련 기능을 활성화
        if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // 현재 위치 관련 기능을 관리하는 객체 생성 및 초기화
            currentLocation = new CurrentLocation(this, mMap);
            currentLocation.initializeLocationListener(); // 위치 리스너 초기화
            currentLocation.setLocationUpdateListener(this); // 위치 업데이트 리스너 설정
            mMap.setMyLocationEnabled(true); // 지도에 현재 위치 표시 활성화
            mMap.getUiSettings().setMyLocationButtonEnabled(true); // 현재 위치 버튼 활성화
        }
        // 현재 위치에 마커를 추가하는 기능을 관리하는 객체 생성 및 초기화
        buttonOfCurrent = new ButtonOfCurrent(currentLocation, mMap, saveMarker);
    }

    // 권한 요청 결과 처리 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Permissions 클래스를 통해 권한 요청 결과 처리
        Permissions.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    // 지도가 준비되었을 때 호출되는 메서드
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // Google Maps 객체 초기화
        DayAndNight.setMapStyleBasedOnTime(mMap, this); // 주간/야간 모드에 따른 지도 스타일 설정

        CreateMap createMap = new CreateMap(mMap, this); // 지도 생성 및 초기화

        Log.d("MainActivity", "Map is ready"); // 로그 출력: 지도 준비 완료

        // 현재 위치 및 버튼 관련 설정
        buttonOfCurrent = new ButtonOfCurrent(currentLocation, mMap, saveMarker); // 현재 위치 마커 버튼 설정
        loadSavedMarkers(); // 저장된 마커 불러오기

        // 위치 권한이 부여된 경우 위치 관련 기능 초기화
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initializeLocationRelatedStuff();
        }

        // 가로등 및 건물 관리자 초기화
        lampManager = new LampManager(mMap, this);
        eventOfLamp = new EventOfLamp(this, lampManager);

        // 마커 클릭 및 카메라 이동 완료 리스너 설정
        mMap.setOnCameraIdleListener(eventOfLamp);
        eventOfLamp.onMapReady(mMap);

        // 마커 클릭 이벤트 처리를 위한 중앙 핸들러 설정
        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            Log.d("MainActivity", "Marker clicked with tag: " + tag);

            // 클릭된 마커에 따라 적절한 이벤트 처리
            if (tag instanceof DataFetcher.Streetlight) {
                return eventOfLamp.onMarkerClick(marker);
            } else if ("currentLocation".equals(tag)) {
                return handleCurrentLocationMarkerClick(marker);
            }

            Log.d("MainActivity", "Unknown marker clicked");
            return false;
        });
    }

    // 현재 위치 마커 클릭 시 호출되는 메서드
    private boolean handleCurrentLocationMarkerClick(Marker marker) {
        setSelectedMarker(marker); // 선택된 마커 설정
        Log.d("MainActivity", "Current location marker clicked"); // 로그 출력
        showMarkerActionButtons(); // 마커 액션 버튼 표시
        return true; // 이벤트 처리 완료
    }

    // 마커 액션 버튼을 표시하는 메서드
    private void showMarkerActionButtons() {
        deleteMarkerButton.setVisibility(View.VISIBLE); // 삭제 버튼 표시
        exportButton.setVisibility(View.VISIBLE); // 내보내기 버튼 표시
    }

    // 마커 액션 버튼을 숨기는 메서드
    private void hideMarkerActionButtons() {
        deleteMarkerButton.setVisibility(View.GONE); // 삭제 버튼 숨김
        exportButton.setVisibility(View.GONE); // 내보내기 버튼 숨김
    }

    // 위치 업데이트 시 호출되는 메서드
    @Override
    public void onLocationUpdated(LatLng newLocation) {
        tailTextView.setText("현재 위치: " + newLocation.latitude + ", " + newLocation.longitude); // 현재 위치 표시
    }

    // 저장된 마커를 로드하는 메서드
    private void loadSavedMarkers() {
        Set<LatLng> savedMarkerLatLngs = saveMarker.loadAllMarkerPositions(); // 저장된 마커 위치 로드
        if (savedMarkerLatLngs != null) {
            for (LatLng latLng : savedMarkerLatLngs) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("저장된 위치")); // 저장된 마커 추가
                Log.d("MainActivity", "Adding saved marker: " + latLng.toString()); // 로그 출력
            }
        }
    }

    // SharedPreferences 내보내기 메서드
    private void exportSharedPreferences() {
        SharedPreferencesExporter.exportSharedPreferences(this, "MarkerPref"); // SharedPreferences 내보내기
    }

    // 선택된 마커를 설정하는 메서드
    public void setSelectedMarker(Marker marker) {
        this.selectedMarker = marker; // 선택된 마커 설정
    }
}