package com.example.privacyhook; // ⚠️请务必确认包名

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    private Switch swId, swContact, swLocation;
    private EditText etLat, etLng;
    private TextView tvLog;
    private ScrollView logScroll;
    private Handler logHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swId = findViewById(R.id.switchId);
        swContact = findViewById(R.id.switchContact);
        swLocation = findViewById(R.id.switchLocation);
        etLat = findViewById(R.id.etLat);
        etLng = findViewById(R.id.etLng);
        tvLog = findViewById(R.id.tvLog);
        logScroll = findViewById(R.id.logScroll);
        Button btnSync = findViewById(R.id.btnSync);

        prefs = getSharedPreferences("ModularConfig", MODE_PRIVATE);
        swId.setChecked(prefs.getBoolean("id_on", true));
        swContact.setChecked(prefs.getBoolean("contact_on", true));
        swLocation.setChecked(prefs.getBoolean("location_on", true));
        etLat.setText(prefs.getString("lat", "22.3193"));
        etLng.setText(prefs.getString("lng", "114.1694"));

        btnSync.setOnClickListener(v -> {
            prefs.edit().putBoolean("id_on", swId.isChecked())
                    .putBoolean("contact_on", swContact.isChecked())
                    .putBoolean("location_on", swLocation.isChecked())
                    .putString("lat", etLat.getText().toString())
                    .putString("lng", etLng.getText().toString()).commit();

            // 每次同步时重置日志文件
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c", "rm /data/local/tmp/intercept.log"});
            } catch (Exception e) {}

            syncToUnderground();
            tvLog.setText("[SYSTEM] 配置已同步，日志监控重启中...\n");
            Toast.makeText(this, "配置已全量同步", Toast.LENGTH_SHORT).show();
        });

        startLogMonitor();
    }

    private void startLogMonitor() {
        logHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLogDisplay();
                logHandler.postDelayed(this, 1500);
            }
        }, 1000);
    }

    private void refreshLogDisplay() {
        try {
            File logFile = new File("/data/local/tmp/intercept.log");
            if (logFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                tvLog.setText(sb.toString());
                logScroll.post(() -> logScroll.fullScroll(android.view.View.FOCUS_DOWN));
            }
        } catch (Exception e) {}
    }

    private void syncToUnderground() {
        try {
            File transferFile = new File(getFilesDir(), "transfer.txt");
            FileWriter fw = new FileWriter(transferFile);
            fw.write("ID_ON:" + swId.isChecked() + "\n");
            fw.write("CONTACT_ON:" + swContact.isChecked() + "\n");
            fw.write("LOCATION_ON:" + swLocation.isChecked() + "\n");
            fw.write("LAT:" + etLat.getText().toString() + "\n");
            fw.write("LNG:" + etLng.getText().toString() + "\n");
            fw.close();

            // 【核心修复】：主程序 Root 权限提前开路，赋予日志文件 777 权限
            String cmd = "setenforce 0; " +
                    "cp " + transferFile.getAbsolutePath() + " /data/local/tmp/privacy.txt; " +
                    "chmod 777 /data/local/tmp/privacy.txt; " +
                    "touch /data/local/tmp/intercept.log; " +
                    "chmod 777 /data/local/tmp/intercept.log";

            Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
        } catch (Exception e) {}
    }
}