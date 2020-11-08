package com.example.indoorestimate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import java.util.List;

class WifiReceiver extends BroadcastReceiver {
    private WifiManager wifiManager;
    private TableLayout map;
    private Estimate test;

    private static int past_ref_x = 3, past_ref_y = 1, past_score_x = 3, past_score_y = 1;

    public WifiReceiver(Context context, WifiManager wifiManager, TableLayout map) {
        this.wifiManager = wifiManager;
        this.map = map;
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

        if (estimateResult.equals("NORESULT")) {
            Toast.makeText(context, "결과 없음", Toast.LENGTH_SHORT).show();
        } else {
            String ref = estimateResult.split("/")[0];
            String score = estimateResult.split("/")[1];
            int ref_x, ref_y;
            int score_x, score_y;

            int REF_ERROR_RADIUS = 1;
            int SCORE_ERROR_RADIUS = 1;
            boolean find_ref = false;
            boolean find_score = false;

            ref_x = Integer.parseInt(ref.split(",")[0]);
            ref_y = Integer.parseInt(ref.split(",")[1]);

            score_x = Integer.parseInt(score.split(",")[0]);
            score_y = Integer.parseInt(score.split(",")[1]);

            //이전 셀 색 지우기
            ImageView past_ref_cell = map.findViewById(past_ref_y * 100 + past_ref_x);
            past_ref_cell.setImageResource(R.drawable.cell_fill);

            ImageView past_score_cell = map.findViewById(past_score_y * 100 + past_score_x);
            past_score_cell.setImageResource(R.drawable.cell_fill);

            //현재 셀 색 채우기
            while (find_ref == false || find_score == false) {
                if(ref_x == score_x && ref_y == score_y) {
                    ImageView ref_score_cell = map.findViewById(ref_y * 100 + ref_x);
                    ref_score_cell.setImageResource(R.drawable.cell_ref_score_location);

                    past_ref_x = ref_x;
                    past_ref_y = ref_y;

                    find_ref = true;
                    find_score = true;
                } else {
                    if (!find_ref) {
                        if (Math.abs(ref_x - past_ref_x) + Math.abs(ref_y - past_ref_y) <= REF_ERROR_RADIUS) {
                            ImageView ref_cell = map.findViewById(ref_y * 100 + ref_x);
                            ref_cell.setImageResource(R.drawable.cell_ref_location);

                            past_ref_x = ref_x;
                            past_ref_y = ref_y;

                            find_ref = true;
                        } else {
                            REF_ERROR_RADIUS++;
                        }
                    }

                    if (!find_score) {
                        if (Math.abs(score_x - past_score_x) + Math.abs(score_y - past_score_y) <= SCORE_ERROR_RADIUS) {
                            ImageView score_cell = map.findViewById(score_y * 100 + score_x);
                            score_cell.setImageResource(R.drawable.cell_score_location);

                            past_score_x = score_x;
                            past_score_y = score_y;

                            find_score = true;
                        } else {
                            SCORE_ERROR_RADIUS++;
                        }
                    }
                }
            }

            Toast.makeText(context, "빨강 : ref " + REF_ERROR_RADIUS + ", 파랑 : score" + SCORE_ERROR_RADIUS, Toast.LENGTH_LONG).show();
        }

        //wifiManager.startScan();
    }
}

