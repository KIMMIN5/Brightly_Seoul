package com.example.brightly_seoul.User;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import androidx.core.content.ContextCompat;

public class ButtonOfReport {
    private Context context; // 애플리케이션 컨텍스트
    private Button reportButton; // 보고하기 버튼

    // 생성자: Context와 버튼 객체 초기화, 버튼 설정
    public ButtonOfReport(Context context, Button reportButton) {
        this.context = context;
        this.reportButton = reportButton;
        setupButton(); // 버튼 설정 메서드 호출
    }

    // 보고하기 버튼 설정 메서드
    private void setupButton() {
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전화 권한 확인 및 요청
                Permissions.checkCallPhonePermission((Activity) context);

                // 권한이 이미 부여되었다면, 비상 전화 기능 수행
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    callEmergencyNumber(); // 비상 전화 기능 호출
                }
            }
        });
    }

    // 비상 전화 기능을 수행하는 메서드
    private void callEmergencyNumber() {
        Intent callIntent = new Intent(Intent.ACTION_CALL); // 전화 걸기 인텐트 생성
        callIntent.setData(Uri.parse("tel:112")); // 전화번호 설정
        context.startActivity(callIntent); // 인텐트를 사용하여 전화 기능 시작
    }
}