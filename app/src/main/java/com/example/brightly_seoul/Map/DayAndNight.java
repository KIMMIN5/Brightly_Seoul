package com.example.brightly_seoul.Map;

import com.example.brightly_seoul.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import android.content.Context;
import android.content.res.Configuration;

public class DayAndNight {
    // Google Map의 스타일을 시간(주간/야간)에 따라 설정하는 메서드
    public static void setMapStyleBasedOnTime(GoogleMap googleMap, Context context) {
        // 디바이스의 현재 UI 모드(주간/야간) 확인
        int nightModeFlags = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                // 디바이스가 야간 모드일 때 적용할 지도 스타일
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.night_map_style));
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                // 디바이스가 주간 모드일 때 적용할 지도 스타일
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.day_map_style));
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                // 디바이스의 UI 모드가 설정되지 않았을 때는 기본 지도 스타일 유지
                // 추가 처리가 필요할 수 있음
                break;
        }
    }
}