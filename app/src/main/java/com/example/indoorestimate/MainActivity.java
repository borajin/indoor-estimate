package com.example.indoorestimate;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
    private TextView result;
    private TableLayout map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        GPSCheck();
        checkNetwork();
        DBCheck();

        setUI();
    }

    private void setUI() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        startBtn = findViewById(R.id.startBtn);
        result = findViewById(R.id.result);
        map = findViewById(R.id.map);

        startBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManager.startScan();
                Toast.makeText(getApplicationContext(), "측정중...", Toast.LENGTH_SHORT).show();
            }
        });

        drawMap();
    }

    private void drawMap() {
        int[][] mapXY = new int[76][8];

        DBAdapter db = new DBAdapter(this);
        String sql = "select CELL_X, CELL_Y from TR_FPDB_MAP;";
        Cursor cursor = db.search(sql);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int cell_x = cursor.getInt(cursor.getColumnIndex("CELL_X"));
                int cell_y = cursor.getInt(cursor.getColumnIndex("CELL_Y"));

                mapXY[cell_y][cell_x] = 1;
            }
        }
        cursor.close();

        //db 닫기
        try {
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i <= 75; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j <= 7; j++) {
                ImageView cell = new ImageView(this);
                cell.setId((i*100) + j);

                if(mapXY[i][j] == 1) {
                    cell.setImageResource(R.drawable.cell_fill);
                } else {
                    cell.setImageResource(R.drawable.cell_blank);
                }

                cell.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                row.addView(cell);
            }

            map.addView(row);
        }
    }

    //마지막 초기화 작업?? onresume은 activity가 전면에 나타날 때, oncreate 호출 이후에도 호출됨.
    @Override
    public void onResume() {
        super.onResume();
        receiverWifi = new WifiReceiver(wifiManager, result, map);
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

    private void checkNetwork() {
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE) {//쓰리지나 LTE로 연결된것(모바일을 뜻한다.)
                turnOnWifi();
            } else if (type == ConnectivityManager.TYPE_WIFI) {//와이파이 연결된것

            }
        }

        turnOnWifi();
    }

    private void turnOnWifi() {
        message("wifi 킴");
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(this.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
    }

    private void DBCheck() {
        File target = new File("/data/data/com.example.indoorestimate/databases/test.db");

        if (!target.exists() || target.length() <= 0) {
            message("db파일 없음");
            DBdownload();
        } else {
            message("db파일 존재");
        }
    }

    private void DBdownload() {
        String DB_DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=1SmLb-kHpQQnZSP45bDJFDI2GjFM11EA8";
        new DBdownload(this).execute(DB_DOWNLOAD_URL, "1", "1");
    }

    private void GPSCheck() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        } else {

        }
    }

    private void message(final String msg) {
        final String message = msg;
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}