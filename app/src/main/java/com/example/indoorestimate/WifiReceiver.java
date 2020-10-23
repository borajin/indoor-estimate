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
    private List<SCANINFO> scanList;
    private TextView result;

    public WifiReceiver(WifiManager wifiManager, List<SCANINFO> scanList, TextView result) {
        this.wifiManager = wifiManager;
        this.scanList = scanList;
        this.result = result;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        //broadcast 로 단말기의 상태변화나 다른 단말기가 송신하는 메세지를 receive 할 수 있고 그에 따른 처리도 가능함.
        //단말기 배터리가 부족하다거나 뭐 그런...

        //인텐트 쪽에서 scan 한 상태가 되면 scan 결과 처리하는 braodcast (개발자가 custom status 도 처리 할 수 있음)
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            //real_scan(context);
            test_scan(context);
        } else {
            //참고 :: fail 시 첫 스캔이면 아무 것도 반환 안 하고 n번째 스캔이면 results 에 이전 결과가 출력됨.
            System.out.println("fail");
        }
    }

    private void real_scan(Context context) {
        List<ScanResult> wifiList = wifiManager.getScanResults();

        long start = System.currentTimeMillis();
        for (ScanResult scanResult : wifiList) {
            //scanresult feild 참고 - https://developer.android.com/reference/android/net/wifi/ScanResult?hl=ko
            //ssid, bssid(mac address), level(rssi), timestamp(언제 scan했는지), frequency 등..
            scanList.add(new SCANINFO(scanResult.BSSID.replace(":", ""), scanResult.level));
        }
        long end = System.currentTimeMillis();
        System.out.println("scan time : " + (end - start)/1000.0);

        start = System.currentTimeMillis();
        Estimate test = new Estimate(context, scanList);
        String scanResult = test.test_main();
        end = System.currentTimeMillis();
        System.out.println("estimate time : " + (end - start)/1000.0);

        if(scanResult.equals("NORESULT")) {
            result.setText("결과 없음");
            Toast.makeText(context, "결과 없음", Toast.LENGTH_SHORT).show();
        } else {
            result.setText(scanResult);
            Toast.makeText(context, "측정 완료!", Toast.LENGTH_SHORT).show();
        }
    }

    private void test_scan(Context context) {
        //2번출구~1번출구 사이
        String testScanResult1 = "06300d8acf43;-48/00300d8acf40;-50/06300d8acf42;-50/0e09b4760884;-54/0009b4760884;-54/0609b4760884;-54/0a09b4760884;-54/1209b4760884;-54/0a085299f792;-55/0a085299f793;-55/0609b4760883;-55/0a300d8acf52;-57/0a09b4760883;-57/06085299f793;-58/0009b4760883;-58/06085299f792;-60/0a300d8acf53;-61/8a3c1ce8c243;-61/88366cd8f02e;-61/00300d8acf50;-62/06085299f7b3;-70/1209b47608a4;-70/10e3c70983a4;-70/06300d8ada02;-70/00300d8ada00;-71/06300d8ada03;-71/0a300d8ada12;-71/02e3c70983a3;-72/88366c578e06;-72/0aaed6fe0329;-72/02e3c70983a4;-72/00300d8ada10;-72/0a300d8ada13;-72/10e3c70983a3;-73/00078984f1c9;-73/12e3c70983a3;-74/085ddd255f7d;-74/0009b47608a3;-75/06085299f873;-76/0e09b47608ab;-76/0009b47608a4;-76/06085299f7a3;-77/0609b47608a4;-77/0a09b47608a4;-77/0e09b47608a4;-77/705dccad533c;-78/02e3c70b2dae;-78/929f33d06af8;-78/02e3c70b2daf;-78/06085299f7a2;-79/144d6795fcf4;-79/10e3c70b2dae;-79/12e3c70b2dae;-79/0a0b6b2fe9c4;-79/00078984f1ca;-79/883c1ce8c242;-80/060b6b2fe997;-80/0a0b6b2fe997;-80/9e7bef5aff94;-80/085ddd255f7c;-81/909f33b30dde;-81/085ddd5c0386;-82/060b6b2fe9c4;-83/ba3c1ce8c242;-83/d89d67931b50;-83/085dddc8fd0a;-85/88366c578e04;-85/d8fee342e83b;-85/6029d504081a;-86/";

        //1,2번출구 쪽 개찰구
        String testScanResult2 = "06300d8acf42;-49/0a085299f792;-52/0a085299f793;-52/06300d8acf43;-52/0009b4760883;-52/0a09b4760883;-52/06085299f793;-53/0609b4760883;-53/06085299f792;-54/0e09b4760884;-58/0009b4760884;-58/0609b4760884;-59/0a09b4760884;-59/1209b4760884;-59/0a300d8acf52;-60/00300d8ada00;-65/06300d8ada02;-65/06300d8ada03;-65/00300d8acf50;-66/0a300d8acf53;-66/0009b47608ab;-66/0a300d8ada12;-67/0a300d8ada13;-67/00300d8ada10;-68/06085299f872;-69/1209b47608a4;-70/0009b47608a4;-70/0609b47608a4;-70/0a09b47608a4;-70/0e09b47608a4;-70/0a085299f7a2;-71/0a085299f7a3;-71/0009b476087b;-77/929f33d06af8;-77/02e3c70b2dae;-78/10e3c70b2dae;-78/12e3c70b2dae;-79/0609b476087b;-81/06085299f873;-82/b8b7f11621da;-82/12e3c70b2da5;-83/0a0b6b2fe9c4;-84/b8b7f116166c;-84/060b6b2fe9c4;-84/0e09b47608ab;-84/88366c578e06;-84/24d13f147e39;-85/4cb1cdaf30dc;-85/10e3c70b2da5;-85/883c1ce8c242;-86/02e3c70983a3;-86/10e3c70983a3;-86/0a085299f883;-86/4cb1cd2f30dc;-86/12e3c70983a3;-86/02e3c70b2da5;-86/4cb1cd6f30dc;-87/4cb1cdaf30d8;-87/0a300d8ad8b3;-89/0a085299f882;-89/";

        //우대권 발급기
        String testScanResult3 = "06300d8ada02;-50/06300d8ada03;-50/00300d8ada00;-51/06085299f7a2;-53/06085299f7a3;-53/0a300d8ada12;-54/00300d8ada10;-55/0a300d8ada13;-55/0217b20b4d0c;-58/0009b47608a4;-59/0609b47608a4;-59/0a09b47608a4;-59/0a085299f7a2;-60/0a085299f7a3;-60/0609b47608a3;-60/0a09b47608a3;-60/0009b47608a3;-60/0e09b47608a4;-60/1209b47608a4;-60/06085299f793;-67/06300d8acf42;-68/1209b4760884;-69/0609b4760884;-70/0a09b4760884;-70/0e09b4760884;-70/4cb1cdaf30d8;-71/0009b4760884;-71/4cb1cd2f30d8;-72/06085299f792;-73/00300d8acf40;-73/4cb1cd6f30d8;-73/06300d8acf43;-74/24d13f148118;-74/0a09b4760823;-76/0a300d8acf52;-78/02e3c70b2da5;-78/00300d8acf50;-79/0a300d8acf53;-79/060b6b2fea3c;-80/0a0b6b2fea3c;-80/24d13f148649;-81/705dccad533c;-82/1223aa6255a9;-82/2217b20b4bc8;-85/2217b20b4bca;-85/2217b20b58c4;-86/909f33a34c10;-86/02e3c70b2da4;-87/10e3c70b2dae;-87/24d13f147e39;-87/2217b20b58c4;-87/0217b20b4bca;-88/12e3c70b2da4;-89/";

        //3,4번출구 사이
        String testScanResult4 = "06300d8ada02;-53/00300d8ada00;-53/06300d8ada03;-53/06085299f7a2;-54/06085299f7a3;-54/0609b47608a3;-58/0a09b47608a3;-58/0009b47608a3;-58/0609b47608a4;-58/0e09b47608a4;-59/0009b47608a4;-59/0a09b47608a4;-59/1209b47608a4;-59/0a300d8ada12;-60/00300d8ada10;-60/0a300d8ada13;-60/0a085299f7a2;-61/0a085299f7a3;-61/b4a94f486632;-64/6029d50b3b22;-65/4cb1cd6f30d8;-66/4cb1cdaf30d8;-67/0a0b6b2fea3c;-70/4cb1cd2f30dc;-70/4cb1cd6f30dc;-70/4cb1cdaf30dc;-70/6029d50b3b21;-70/00271c3bef34;-70/060b6b2fea3c;-71/d86162508817;-71/b4a94f486631;-73/d861625087fd;-73/1ca532ebc6b7;-74/10e3c70b2da5;-75/18c501a11ae6;-75/24d13f148118;-76/e009bf346da4;-76/1223aa8392e6;-76/705dcc67ac2a;-77/02e3c70b2daf;-77/1ca532ebc6b9;-78/1223aa6255a9;-78/0a09b4760884;-79/06085299f793;-79/000789c35cfa;-79/64e599c99b34;-79/10e3c70983a4;-79/0009b4760884;-80/0609b4760884;-80/0e09b4760884;-80/1209b4760884;-80/88366c7b2f02;-80/0a085299f793;-81/18c501a11ae7;-81/0a300d8acf52;-82/0a300d8acf53;-82/10e3c70b2dae;-82/0a085299f792;-82/060b6b2fea42;-82/0a0b6b2fea42;-82/02e3c70b2dae;-82/12e3c70b2dae;-82/0609b4760823;-82/b4a94f184826;-82/b6a94f184826;-82/00300d8acf50;-84/085dddc8fd0a;-85/883c1c1678f7;-87/10e3c70983a3;-89/";

        //화장실
        String testScanResult5 = "0a085299f792;-61/0a085299f793;-61/00300d8acf40;-62/0e09b4760884;-63/0009b4760884;-63/0609b4760884;-63/0a09b4760884;-63/1209b4760884;-63/06085299f792;-64/06300d8acf43;-64/06300d8acf42;-65/06085299f793;-65/0009b4760883;-65/0a09b4760883;-65/0609b4760883;-66/00300d8ada10;-66/0a300d8ada12;-66/0a300d8ada13;-66/00300d8ada00;-67/06300d8ada02;-67/06300d8ada03;-67/0a300d8acf52;-71/0a300d8acf53;-71/00300d8acf50;-72/1209b47608a4;-72/0009b47608a4;-72/0609b47608a4;-72/0a09b47608a4;-72/0e09b47608a4;-72/0a09b47608a3;-72/06085299f7a3;-73/06085299f7a2;-74/0217b20b4b7e;-76/2217b20b4b7e;-79/0217b20b4b7c;-80/12e3c70b2daf;-80/4cb1cd2f30d8;-81/2217b20b4b7c;-81/0a085299f7b2;-83/0a085299f7b3;-83/24d13f147e38;-83/06300d8ad8a3;-83/02e3c70b2daf;-83/0217b20b9dc7;-83/883c1ce8c243;-85/24d13f148118;-85/d861625087fd;-85/0217b20b5a5a;-86/0a085299f872;-87/4cb1cd6f30d8;-87/06085299f873;-87/b4a94f78a5db;-87/0217b20b9dd7;-87/12e3c70983a4;-88/0217b20b4c94;-89/2217b20b4b7c;-89/";

        String[] testAps = testScanResult3.split("/");

        long start = System.currentTimeMillis();
        for (int i=0; i<testAps.length-1; i++) {
            String bssid;
            double rssi;

            bssid = testAps[i].split(";")[0];
            rssi = Double.parseDouble(testAps[i].split(";")[1]);

            scanList.add(new SCANINFO(bssid, rssi));
        }
        long end = System.currentTimeMillis();
        System.out.println("scan time : " + (end - start)/1000.0);

        start = System.currentTimeMillis();
        Estimate test = new Estimate(context, scanList);
        String scanResult = test.test_main();
        end = System.currentTimeMillis();
        System.out.println("estimate time : " + (end - start)/1000.0);

        start = System.currentTimeMillis();
        if(scanResult.equals("NORESULT")) {
            result.setText("결과 없음");
            Toast.makeText(context, "결과 없음", Toast.LENGTH_SHORT).show();
        } else {
            result.setText(scanResult);
            Toast.makeText(context, "측정 완료!", Toast.LENGTH_SHORT).show();
        }
        end = System.currentTimeMillis();
        System.out.println("ui edit time : " + (end - start)/1000.0);

    }
}

