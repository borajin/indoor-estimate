package com.example.indoorestimate;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION"};

    //GPS
    LocationManager locationManager;

    private WifiManager wifiManager;
    private WifiReceiver receiverWifi;

    private Button startBtn;
    private TextView scanTime;
    private TextView requestTime;

    private TextView result;

    List<SCANINFO> scanList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI();

        //todo : 비동기로 처리해야 함... 퍼미션 체크(처음만) > db 체크(처음만) > wifi 체크(계속) > gps 체크(계속)
        checkPermission();
        DBCheck();
        GPSCheck();
    }

    private void setUI() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        startBtn = findViewById(R.id.startBtn);
        scanTime = findViewById(R.id.scanTime);
        requestTime = findViewById(R.id.requestTime);
        result = findViewById(R.id.result);

        startBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss");
                String formatDate = sdfNow.format(date);

                requestTime.setText("요청 시간 " + formatDate);
                wifiManager.startScan();

                Toast.makeText(getApplicationContext(), "측정중...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //마지막 초기화 작업?? onresume은 activity가 전면에 나타날 때, oncreate 호출 이후에도 호출됨.
    @Override
    public void onResume() {
        super.onResume();
        receiverWifi = new WifiReceiver(wifiManager, scanTime, scanList, result);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
    }

    //onStop, onDestroy 호출되기 이전에 호출됨. onresume 쌍으로 보고 거기서 했던 작업을 여기서 정리, 멈춤.
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }

    private void checkPermission() {
        if (!hasPermissions(PERMISSIONS)) {
            requestNecessaryPermissions(PERMISSIONS);
        } else {
            //이미 사용자에게 퍼미션 허가를 받았음

        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions,
                                           int[] grantResults) {
        switch (permsRequestCode) {

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean readAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!readAccepted || !writeAccepted) {
                            return;
                        }
                    }
                }
                break;
        }
    }

    private boolean hasPermissions(String[] permissions) {
        int res = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {
            res = this.checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                //퍼미션 허가 안된 경우
                return false;
            }
        }
        //퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkNetwork() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE) {//쓰리지나 LTE로 연결된것(모바일을 뜻한다.)
                return true;
            } else if (type == ConnectivityManager.TYPE_WIFI) {//와이파이 연결된것
                return true;
            }
        }

        return false;
    }

    private void DBCheck() {
        File target = new File("/data/data/com.example.indoorestimate/databases/test.db");

        if (!target.exists()) {
            DBdownload();
        } else {
            Toast.makeText(this, "DB파일 존재", Toast.LENGTH_SHORT).show();
        }
    }

    private void DBdownload() {
        String DB_DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=1uvJgvziZe1lR_3Nvpy-iqNpMqZckrtJx";

        if (checkNetwork()) {
            new DBdownload(this).execute(DB_DOWNLOAD_URL, "1", "1");
        } else {
            Toast.makeText(this, "다운로드를 위해 와이파이를 킵니다.", Toast.LENGTH_SHORT).show();
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
            wifi.setWifiEnabled(true);

            DBdownload();
        }
    }

    private void GPSCheck() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }
    }
}