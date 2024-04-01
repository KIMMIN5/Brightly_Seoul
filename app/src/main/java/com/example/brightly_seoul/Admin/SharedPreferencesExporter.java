package com.example.brightly_seoul.Admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SharedPreferencesExporter {

    // SharedPreferences 내용을 외부 파일로 내보내는 메서드
    public static void exportSharedPreferences(Context context, String sharedPreferencesName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        File exportDir = context.getExternalFilesDir(null);
        if (exportDir == null) {
            Log.e("SharedPreferencesExport", "External storage not available");
            // 사용자에게 스토리지 문제 알림
            Toast.makeText(context, "외부 스토리지 사용 불가능", Toast.LENGTH_LONG).show();
            return;
        }

        File file = new File(exportDir, sharedPreferencesName + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                writer.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
            }
            writer.flush();
            // 내보내기 성공 알림
            Toast.makeText(context, sharedPreferencesName + " 내보내기 완료", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("SharedPreferencesExport", "Error writing SharedPreferences to file: " + file.getAbsolutePath(), e);
            // 에러 알림
            Toast.makeText(context, "파일 내보내기 실패", Toast.LENGTH_LONG).show();
        }
    }
}