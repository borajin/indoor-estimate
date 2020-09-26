package com.example.indoorestimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class WifiReceiver extends BroadcastReceiver {
    private WifiManager wifiManager;
    private TextView scanTime;
    private List<SCANINFO> scanList;
    private TextView result;

    public WifiReceiver(WifiManager wifiManager, TextView scanTime, List<SCANINFO> scanList, TextView result) {
        this.wifiManager = wifiManager;
        this.scanTime = scanTime;
        this.scanList = scanList;
        this.result = result;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //broadcast 로 단말기의 상태변화나 다른 단말기가 송신하는 메세지를 receive 할 수 있고 그에 따른 처리도 가능함.
        //단말기 배터리가 부족하다거나 뭐 그런...

        //인텐트 쪽에서 scan 한 상태가 되면 scan 결과 처리하는 braodcast (개발자가 custom status 도 처리 할 수 있음)
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            List<ScanResult> wifiList = wifiManager.getScanResults();

            for (ScanResult scanResult : wifiList) {
                //scanresult feild 참고 - https://developer.android.com/reference/android/net/wifi/ScanResult?hl=ko
                //ssid, bssid(mac address), level(rssi), timestamp(언제 scan했는지), frequency 등..

                scanList.add(new SCANINFO(scanResult.BSSID.replace(":", ""), scanResult.level));
            }

            Estimate test = new Estimate(context, scanList);
            result.setText(test.test_main());

            //test_scan(context);

            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm:ss");
            String formatDate = sdfNow.format(date);
            scanTime.setText("측정 시간 " + formatDate);

            Toast.makeText(context, "측정완료!", Toast.LENGTH_SHORT).show();
        } else {
            //참고 :: fail 시 첫 스캔이면 아무 것도 반환 안 하고 n번째 스캔이면 results 에 이전 결과가 출력됨.
            System.out.println("fail");
        }
    }

    private void test_scan(Context context) {
        String testScanResult = "06300d8acf43;-52/06085299f792;-53/0a09b4760884;-53/0009b4760884;-53/0609b4760884;-53/0e09b4760884;-53/1209b4760884;-53/0609b4760883;-55/06300d8acf42;-56/00300d8acf50;-56/0a300d8acf52;-56/0a300d8acf53;-56/00300d8acf40;-57/0009b4760883;-57/0a09b4760883;-58/0a085299f792;-59/0a085299f793;-59/06085299f793;-59/24d13f149233;-59/88366cd8f02e;-68/12e3c70983a3;-70/10e3c70983a4;-70/8a3c1ce8c243;-71/10e3c70983a3;-71/02e3c70983a3;-71/00078984f1c9;-71/02e3c70b2dae;-72/12e3c70b2dae;-72/02e3c70b2daf;-72/10e3c70b2dae;-74/060b6b2fe9c4;-74/10e3c70b2daf;-74/00300d8ada00;-75/0a0b6b2fe9c4;-76/06300d8ada02;-76/00078984f1ca;-77/12e3c70b2da5;-77/88366cdf3058;-77/0023aa6255a9;-77/883c1ce8c242;-78/060b6b2fe997;-78/0a300d8ada12;-78/0a300d8ada13;-78/0a0b6b2fe997;-79;00300d8ada10;-79/d89d67961b50;-80/02e3c70b2da4;-81/6629d504081a;-82/40270b034e88;-82/6029d504081a;-83/10e3c70b2da4;-83/0a09b47608ab;-83/5820b15c64fa;-83/64eeb7bc64eb;-85/2217b20b97ab;-86/06085299f872;-86/2217b20b4d22;-86/2217b20b97a9;-87/2217b20b5918;-87/2217b20b4d20;-87/2217b20b9677;-89/144d6795fcd4;-89/0a300d8ad8b2;-81";
        String[] testAps = testScanResult.split("/");

        for (int i=0; i<testAps.length-1; i++) {
            String bssid;
            double rssi;

            bssid = testAps[i].split(";")[0];
            rssi = Double.parseDouble(testAps[i].split(";")[1]);

            scanList.add(new SCANINFO(bssid, rssi));
        }

        Estimate test = new Estimate(context, scanList);
        result.setText(test.test_main());
    }
}

