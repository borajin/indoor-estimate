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
import java.util.Stack;

class WifiReceiver extends BroadcastReceiver {
    private WifiManager wifiManager;
    private TableLayout map;

    boolean first_start = true;

    private Estimate test;

    class position {
        int x;
        int y;

        position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private Stack<position> pastPositions;

    public WifiReceiver(Context context, WifiManager wifiManager, TableLayout map) {
        this.wifiManager = wifiManager;
        this.map = map;
        pastPositions = new Stack<>();
        test = new Estimate(context);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            scan(context);
        } else {
            Toast.makeText(context, "측정 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void scan(Context context) {
        test.getScanList().clear();

        List<ScanResult> wifiList = wifiManager.getScanResults();
        for (ScanResult scanResult : wifiList) {
            test.addScanItem(scanResult.BSSID.replace(":", ""), scanResult.level);
        }

        String estimateResult = test.startEstimate();   // x, y 셀값

        if(estimateResult.equals("NORESULT")) {
            Toast.makeText(context, "결과 없음", Toast.LENGTH_SHORT).show();
        } else {
            int past_x, past_y, current_x, current_y;

            current_x = Integer.parseInt(estimateResult.split(",")[0]);
            current_y = Integer.parseInt(estimateResult.split(",")[1]);

            if(first_start) {
                //처음 시작이면 과거 위치 = 현재위치가 같음
                past_x = current_x;
                past_y = current_y;
            } else {
                //처음 시작 아니면 과거 위치는 이전의 현재 위치값
                past_x = pastPositions.peek().x;
                past_y = pastPositions.peek().y;
                first_start = false;
            }

            //과거 위치 표시
            ImageView past_cell = map.findViewById(past_y * 100 + past_x);
            past_cell.setImageResource(R.drawable.cell_past_location);

            //현재 위치 표시
            ImageView current_cell;
            if(Math.abs(current_x - past_x) <= 2 || Math.abs(current_y - past_y) <= 2) {
                //셀 반경 2 이내면 현재 위치 표시, 위치 포함시키기
                pastPositions.push(new position(current_x, current_y));
                current_cell = map.findViewById(current_y*100+current_x);
            } else {
                //셀 반경 2를 벗어났다면 과거 위치를 현재 위치로 표시.
                current_cell = map.findViewById(past_x*100+past_y);
            }
            current_cell.setImageResource(R.drawable.cell_location);

            Toast.makeText(context, "측정 완료!", Toast.LENGTH_SHORT).show();
        }

        wifiManager.startScan();
    }
}

