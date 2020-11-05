package com.example.indoorestimate;

import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initUi();
    }

    private void init() {
        //GPS 켜져있는지 검사..
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ToastMsg("gps를 켜주세요...");
            finish();
        } else {
            checkNetwork();
            checkDB();
        }
    }

    private void initUi() {
        startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EstimateActivity.class) ;
                getApplicationContext().startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
    }

    private void checkNetwork() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                //쓰리지나 LTE로 연결됐다면
                turnOnWifi();
            }
        } else {
            turnOnWifi();
        }
    }

    private void turnOnWifi() {
        ToastMsg("wifi를 킵니다.");
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
    }

    private void checkDB() {
        String DB_PATH = "/data/data/" + getPackageName() + "/databases";
        String DB_NAME = "/test.db";
        File target = new File(DB_PATH + DB_NAME);

        if (!target.exists() || target.length() <= 0) {
            ToastMsg("db파일을 다운로드 합니다.");
            String id = "1pyQpKY44IHs4JU4rkA0NdbUuOmE0cBRb";
            String DB_DOWNLOAD_URL = "https://docs.google.com/uc?id=" + id + "&export=download";
            new DBdownload(this).execute(DB_DOWNLOAD_URL, DB_PATH, DB_NAME);
        } else {
            ToastMsg("db파일 있음");
        }
    }

    private void ToastMsg(final String msg) {
        final String message = msg;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}