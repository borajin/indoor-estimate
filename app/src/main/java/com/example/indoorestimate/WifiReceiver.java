package com.example.indoorestimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class WifiReceiver extends BroadcastReceiver {
    private WifiManager wifiManager;
    private TableLayout map;

    int past_x;
    int past_y;

    boolean first_start = true;

    public WifiReceiver(Context context, WifiManager wifiManager, TableLayout map) {
        this.wifiManager = wifiManager;
        this.map = map;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            //real_scan(context);
            test_scan(context);
        } else {
            //첫 스캔이면 아무 것도 반환 안 하고 n번째 스캔이면 results 에 이전 결과가 출력됨.
            Toast.makeText(context, "측정 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void real_scan(Context context) {
        List<SCANINFO> scanList = new ArrayList<>();
        List<ScanResult> wifiList = wifiManager.getScanResults();

        for (ScanResult scanResult : wifiList) {
            scanList.add(new SCANINFO(scanResult.BSSID.replace(":", ""), scanResult.level));
        }

        Estimate test = new Estimate(context, scanList);
        String scanResult = test.test_main();

        if(scanResult.equals("NORESULT")) {
            Toast.makeText(context, "결과 없음", Toast.LENGTH_SHORT).show();
        } else {
            if(!first_start) {
                ImageView past_cell = map.findViewById(past_y * 100 + past_x);
                past_cell.setImageResource(R.drawable.cell_fill);
            } else {
                first_start = false;
            }

            int x = Integer.parseInt(scanResult.split(",")[0]);
            int y = Integer.parseInt(scanResult.split(",")[1]);
            ImageView current_cell = map.findViewById(y*100+x);
            current_cell.setImageResource(R.drawable.cell_location);

            past_x = x;
            past_y = y;

            Toast.makeText(context, "측정 완료!", Toast.LENGTH_SHORT).show();
        }

        wifiManager.startScan();
    }

    private void test_scan(Context context) {
        HashMap<String, String> testList = new HashMap<String, String>();
        testList.put("12번출구", "06300d8acf43;-48/00300d8acf40;-50/06300d8acf42;-50/0e09b4760884;-54/0009b4760884;-54/0609b4760884;-54/0a09b4760884;-54/1209b4760884;-54/0a085299f792;-55/0a085299f793;-55/0609b4760883;-55/0a300d8acf52;-57/0a09b4760883;-57/06085299f793;-58/0009b4760883;-58/06085299f792;-60/0a300d8acf53;-61/8a3c1ce8c243;-61/88366cd8f02e;-61/00300d8acf50;-62/06085299f7b3;-70/1209b47608a4;-70/10e3c70983a4;-70/06300d8ada02;-70/00300d8ada00;-71/06300d8ada03;-71/0a300d8ada12;-71/02e3c70983a3;-72/88366c578e06;-72/0aaed6fe0329;-72/02e3c70983a4;-72/00300d8ada10;-72/0a300d8ada13;-72/10e3c70983a3;-73/00078984f1c9;-73/12e3c70983a3;-74/085ddd255f7d;-74/0009b47608a3;-75/06085299f873;-76/0e09b47608ab;-76/0009b47608a4;-76/06085299f7a3;-77/0609b47608a4;-77/0a09b47608a4;-77/0e09b47608a4;-77/705dccad533c;-78/02e3c70b2dae;-78/929f33d06af8;-78/02e3c70b2daf;-78/06085299f7a2;-79/144d6795fcf4;-79/10e3c70b2dae;-79/12e3c70b2dae;-79/0a0b6b2fe9c4;-79/00078984f1ca;-79/883c1ce8c242;-80/060b6b2fe997;-80/0a0b6b2fe997;-80/9e7bef5aff94;-80/085ddd255f7c;-81/909f33b30dde;-81/085ddd5c0386;-82/060b6b2fe9c4;-83/ba3c1ce8c242;-83/d89d67931b50;-83/085dddc8fd0a;-85/88366c578e04;-85/d8fee342e83b;-85/6029d504081a;-86/");
        testList.put("12번출구개찰구-노포", "0009b4760883;-43/0609b4760883;-43/0a09b4760883;-43/06300d8acf42;-44/0e09b4760884;-44/0009b4760884;-44/0609b4760884;-44/0a09b4760884;-44/1209b4760884;-44/00300d8acf40;-46/06300d8acf43;-46/0a085299f792;-47/0a085299f793;-47/06085299f792;-52/0a300d8acf52;-53/06085299f793;-53/00300d8acf50;-53/0a300d8acf53;-53/00300d8ada00;-66/06300d8ada02;-66/06300d8ada03;-66/00300d8ada10;-68/0a300d8ada12;-69/0a300d8ada13;-69/10e3c70983a3;-73/02e3c70b2dae;-75/24d13f147e38;-75/12e3c70b2dae;-75/1209b47608a4;-76/0009b47608a4;-76/0609b47608a4;-76/0a09b47608a4;-76/0e09b47608a4;-76/06085299f7b3;-76/10e3c70b2dae;-76/12e3c70b2da5;-77/06085299f872;-78/0a085299f7b2;-78/0a085299f7b3;-78/00078984f1c9;-79/0a0b6b2fe9c4;-80/12e3c70b2daf;-80/10e3c70b2daf;-80/060b6b2fe9c4;-80/060b6b2fe997;-81/0009b47608ab;-81/0a0b6b2fe997;-81/12078984f1ca;-82/24d13f147e39;-83/02e3c70983a3;-84/00300d8ad8b0;-84/0a300d8ad8b2;-84/0a300d8ad8b3;-84/02e3c70b2da4;-85/12e3c70b2da4;-85/ba3c1ce8c242;-86/10e3c70b2da4;-86/b8b7f116166c;-86/883c1ce8c242;-87/d89d67931b50;-87/");
        testList.put("12번출구개찰구-연산다대포", "06300d8acf42;-49/0a085299f792;-52/0a085299f793;-52/06300d8acf43;-52/0009b4760883;-52/0a09b4760883;-52/06085299f793;-53/0609b4760883;-53/06085299f792;-54/0e09b4760884;-58/0009b4760884;-58/0609b4760884;-59/0a09b4760884;-59/1209b4760884;-59/0a300d8acf52;-60/00300d8ada00;-65/06300d8ada02;-65/06300d8ada03;-65/00300d8acf50;-66/0a300d8acf53;-66/0009b47608ab;-66/0a300d8ada12;-67/0a300d8ada13;-67/00300d8ada10;-68/06085299f872;-69/1209b47608a4;-70/0009b47608a4;-70/0609b47608a4;-70/0a09b47608a4;-70/0e09b47608a4;-70/0a085299f7a2;-71/0a085299f7a3;-71/0009b476087b;-77/929f33d06af8;-77/02e3c70b2dae;-78/10e3c70b2dae;-78/12e3c70b2dae;-79/0609b476087b;-81/06085299f873;-82/b8b7f11621da;-82/12e3c70b2da5;-83/0a0b6b2fe9c4;-84/b8b7f116166c;-84/060b6b2fe9c4;-84/0e09b47608ab;-84/88366c578e06;-84/24d13f147e39;-85/4cb1cdaf30dc;-85/10e3c70b2da5;-85/883c1ce8c242;-86/02e3c70983a3;-86/10e3c70983a3;-86/0a085299f883;-86/4cb1cd2f30dc;-86/12e3c70983a3;-86/02e3c70b2da5;-86/4cb1cd6f30dc;-87/4cb1cdaf30d8;-87/0a300d8ad8b3;-89/0a085299f882;-89/");
        testList.put("화장실입구", "0a09b4760883;-56/00300d8ada00;-56/06300d8ada02;-56/06300d8ada03;-56/06085299f793;-58/0e09b4760884;-59/0009b4760884;-59/0609b4760884;-59/0a09b4760884;-59/1209b4760884;-59/06085299f792;-60/06300d8acf42;-60/0a085299f7a2;-60/0a085299f7a3;-60/00300d8acf40;-60/06300d8acf43;-61/0009b4760883;-61/0609b4760883;-61/0a300d8ada13;-61/00300d8ada10;-62/0a300d8ada12;-62/06085299f7a3;-68/0009b47608a4;-69/0a09b47608a4;-69/0e09b47608a4;-69/06085299f7a2;-69/1209b47608a4;-70/0609b47608a4;-70/0a300d8acf52;-71/00300d8acf50;-71/0a300d8acf53;-71/8a3c1ce8c243;-75/24d13f147e38;-76/10e3c70983a4;-77/4cb1cdaf30d8;-78/0217b20b9e03;-78/4cb1cd6f30d8;-78/10e3c70b2daf;-78/02e3c70b2daf;-79/0a085299f7b2;-81/0a085299f7b3;-81/0009b4760823;-81/0a09b476087b;-81/06300d8ad8a2;-81/06300d8ad8a3;-81/12e3c70983a4;-81/929f33d06af8;-82/0217b20b4c96;-82/085ddd255f7d;-83/0217b20b5954;-83/0217b20b5950;-83/883c1ce8c243;-84/0a085299f872;-86/10e3c70b2da4;-86/909f33d76af8;-86/02e3c70983a3;-87/24d13f147e39;-87/2217b20b9e05;-87/2217b20b5a36;-88/00300d8ad8b0;-90/2217b20b5a34;-90/");
        testList.put("여자화장실", "0a085299f792;-61/0a085299f793;-61/00300d8acf40;-62/0e09b4760884;-63/0009b4760884;-63/0609b4760884;-63/0a09b4760884;-63/1209b4760884;-63/06085299f792;-64/06300d8acf43;-64/06300d8acf42;-65/06085299f793;-65/0009b4760883;-65/0a09b4760883;-65/0609b4760883;-66/00300d8ada10;-66/0a300d8ada12;-66/0a300d8ada13;-66/00300d8ada00;-67/06300d8ada02;-67/06300d8ada03;-67/0a300d8acf52;-71/0a300d8acf53;-71/00300d8acf50;-72/1209b47608a4;-72/0009b47608a4;-72/0609b47608a4;-72/0a09b47608a4;-72/0e09b47608a4;-72/0a09b47608a3;-72/06085299f7a3;-73/06085299f7a2;-74/0217b20b4b7e;-76/2217b20b4b7e;-79/0217b20b4b7c;-80/12e3c70b2daf;-80/4cb1cd2f30d8;-81/2217b20b4b7c;-81/0a085299f7b2;-83/0a085299f7b3;-83/24d13f147e38;-83/06300d8ad8a3;-83/02e3c70b2daf;-83/0217b20b9dc7;-83/883c1ce8c243;-85/24d13f148118;-85/d861625087fd;-85/0217b20b5a5a;-86/0a085299f872;-87/4cb1cd6f30d8;-87/06085299f873;-87/b4a94f78a5db;-87/0217b20b9dd7;-87/12e3c70983a4;-88/0217b20b4c94;-89/2217b20b4b7c;-89/");
        testList.put("34번출구개찰구-연산다대포", "0a300d8ada12;-40/00300d8ada10;-40/0a300d8ada13;-40/06300d8ada02;-42/00300d8ada00;-42/06300d8ada03;-42/06085299f7a2;-43/06085299f7a3;-43/0a085299f7a2;-46/0a085299f7a3;-46/0e09b47608a4;-46/0009b47608a4;-46/0609b47608a4;-46/0a09b47608a4;-46/1209b47608a4;-46/0609b47608a3;-49/0a09b47608a3;-49/0009b47608a3;-49/10e3c70b2da5;-61/02e3c70b2da5;-62/24d13f148117;-67/4cb1cd6f30d8;-68/4cb1cd2f30d8;-69/4cb1cdaf30d8;-69/b4a94f486632;-70/4cb1cd2f30dc;-73/0009b4760883;-73/12e3c70b2da5;-73/24d13f148118;-74/0e09b4760884;-74/1209b4760884;-74/4cb1cd6f30dc;-74/4cb1cdaf30dc;-74/0609b4760823;-74/0609b4760884;-75/0a09b4760884;-75/0a085299f792;-75/0a085299f793;-75/0a0b6b2fea3c;-76/0009b4760884;-76/00300d8acf40;-77/06085299f793;-77/00271c3bef34;-77/24d13f148649;-78/060b6b2fea3c;-78/10e3c70b2daf;-80/02e3c70b2da4;-81/00300d8acf50;-81/0a300d8acf52;-81/0a300d8acf53;-81/060b6b2fea42;-81/0a0b6b2fea42;-81/909f33a34c10;-82/b4a94f184826;-82/b6a94f184826;-82/10e3c70b2dae;-83/0009b47608ab;-83/24d13f147e39;-84/10e3c70b2da4;-85/12e3c70b2da4;-86/0a085299f7c3;-86/d86162508817;-88/0a085299f7c2;-90/");
        testList.put("34번출구-노포", "06300d8ada02;-37/00300d8ada00;-37/06300d8ada03;-37/0a300d8ada13;-40/0a300d8ada12;-41/00300d8ada10;-41/06085299f7a2;-44/06085299f7a3;-44/0609b47608a3;-45/0a09b47608a3;-45/0009b47608a3;-46/0a085299f7a2;-47/0a085299f7a3;-47/0e09b47608a4;-51/0009b47608a4;-51/0609b47608a4;-51/0a09b47608a4;-51/1209b47608a4;-51/4cb1cdaf30d8;-61/00300d8acf50;-66/0a300d8acf52;-66/06300d8acf43;-66/0a300d8acf53;-67/4cb1cd2f30d8;-69/10e3c70b2da5;-69/085ddd68e845;-70/06300d8acf42;-70/0a085299f792;-71/4cb1cd2f30dc;-71/4cb1cd6f30dc;-71/4cb1cdaf30dc;-71/00300d8acf40;-72/0a085299f793;-72/0a085299f882;-72/0a085299f883;-72/02e3c70b2da5;-73/0009b4760884;-73/0609b4760884;-73/0a09b4760884;-73/0e09b4760884;-73/1209b4760884;-73/d86162508817;-74/24d13f148118;-75/085ddd68e846;-77/b4a94f184827;-78/1ca532ebc6b6;-79/0a0b6b2fea3c;-80/1ca532ebc6b8;-80/10e3c70b2dae;-81/8a3c1ce8c243;-81/02e3c70b2dae;-81/12e3c70b2dae;-81/02e3c70b2da4;-82/060b6b2fea42;-82/0a0b6b2fea42;-82/b4a94f486631;-82/12e3c70b2da4;-83/060b6b2fea3c;-83/10e3c70b2da4;-83/0a085299f872;-83/6029d50b3b21;-83/00300d8ad8a0;-83/06300d8ad8a2;-83/0a085299f873;-84/060b6b2fe9c4;-85/0a09b47608ab;-86/085ddd131faf;-88/");
        testList.put("34번출구", "06300d8ada02;-53/00300d8ada00;-53/06300d8ada03;-53/06085299f7a2;-54/06085299f7a3;-54/0609b47608a3;-58/0a09b47608a3;-58/0009b47608a3;-58/0609b47608a4;-58/0e09b47608a4;-59/0009b47608a4;-59/0a09b47608a4;-59/1209b47608a4;-59/0a300d8ada12;-60/00300d8ada10;-60/0a300d8ada13;-60/0a085299f7a2;-61/0a085299f7a3;-61/b4a94f486632;-64/6029d50b3b22;-65/4cb1cd6f30d8;-66/4cb1cdaf30d8;-67/0a0b6b2fea3c;-70/4cb1cd2f30dc;-70/4cb1cd6f30dc;-70/4cb1cdaf30dc;-70/6029d50b3b21;-70/00271c3bef34;-70/060b6b2fea3c;-71/d86162508817;-71/b4a94f486631;-73/d861625087fd;-73/1ca532ebc6b7;-74/10e3c70b2da5;-75/18c501a11ae6;-75/24d13f148118;-76/e009bf346da4;-76/1223aa8392e6;-76/705dcc67ac2a;-77/02e3c70b2daf;-77/1ca532ebc6b9;-78/1223aa6255a9;-78/0a09b4760884;-79/06085299f793;-79/000789c35cfa;-79/64e599c99b34;-79/10e3c70983a4;-79/0009b4760884;-80/0609b4760884;-80/0e09b4760884;-80/1209b4760884;-80/88366c7b2f02;-80/0a085299f793;-81/18c501a11ae7;-81/0a300d8acf52;-82/0a300d8acf53;-82/10e3c70b2dae;-82/0a085299f792;-82/060b6b2fea42;-82/0a0b6b2fea42;-82/02e3c70b2dae;-82/12e3c70b2dae;-82/0609b4760823;-82/b4a94f184826;-82/b6a94f184826;-82/00300d8acf50;-84/085dddc8fd0a;-85/883c1c1678f7;-87/10e3c70983a3;-89/");
        testList.put("다익스트라", "909f33fba1ae;-80;0.0/909f33aba2f0;-79;0.0/909f332494d8;-91;0.0/000b819e85a1;-84;0.0/12e3c7052d16;-81;0.0/10e3c7052d16;-80;0.0/06300d8ada02;-77;0.0/06300d8ad8a2;-79;0.0/06300d8acf42;-47;0.0/06300d8a8662;-82;0.0/06300d8a5682;-87;0.0/0609b4760883;-56;0.0/0609b476087b;-84;0.0/00405abfe12b;-78;0.0/00405abfe113;-77;0.0/00405abfe112;-80;0.0/00405abf9503;-51;0.0/00405abf9502;-49;0.0/00405abf9501;-50;0.0/00405abf8d2b;-83;0.0/00405abf8d2a;-81;0.0/00405abf8d29;-81;0.0/00405abf7d53;-80;0.0/00405abf7d51;-79;0.0/00300d8ada00;-78;0.0/00300d8ad8a0;-81;0.0/00300d8acf40;-47;0.0/00300d8a8660;-87;0.0/0014bfa29146;-85;0.0/0009b47608ab;-88;0.0/0009b4760883;-57;0.0/00089ffcd7d4;-84;0.0/00089fba5720;-80;0.0/00089f8e10b9;-80;0.0/00300d20e456;-90;0.0/0609b47608ab;-88;0.0/00405abfe129;-78;0.0/00405abfe12a;-77;0.0/00405abf7d52;-80;0.0/00300d8a5680;-87;0.0/0609b47608a3;-82;0.0/0009b476087b;-82;0.0/06300d8ad982;-92;0.0/909f33ad36c6;-89;0.0/32cda736ee32;-80;0.0/0008520b0972;-87;0.0/");


        String[] testAps = testList.get("34번출구").split("/");

        List<SCANINFO> scanList = new ArrayList<>();

        for (int i=0; i<testAps.length-1; i++) {
            String bssid;
            double rssi;

            bssid = testAps[i].split(";")[0];
            rssi = Double.parseDouble(testAps[i].split(";")[1]);

            scanList.add(new SCANINFO(bssid, rssi));
        }

        Estimate test = new Estimate(context, scanList);
        String scanResult = test.test_main();

        if(scanResult.equals("NORESULT")) {
            Toast.makeText(context, "결과 없음", Toast.LENGTH_SHORT).show();
        } else {
            if(!first_start) {
                ImageView past_cell = map.findViewById(past_y * 100 + past_x);
                past_cell.setImageResource(R.drawable.cell_fill);
            } else {
                first_start = false;
            }

            int x = Integer.parseInt(scanResult.split(",")[0]);
            int y = Integer.parseInt(scanResult.split(",")[1]);
            ImageView current_cell = map.findViewById(y*100+x);
            current_cell.setImageResource(R.drawable.cell_location);

            past_x = x;
            past_y = y;

            Toast.makeText(context, "측정 완료!", Toast.LENGTH_SHORT).show();
        }
    }
}

