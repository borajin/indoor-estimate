package com.example.indoorestimate;


import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Estimate {

    private static final String APTBL = "APLIST";
    private static final String FPTBL = "TR_FPDB_MAP";
    private static final String CHECK_AP = "SELECT 1 FROM %s WHERE INFRAID='%s'";
    private static final String SEARCH_FP = "SELECT IDX, TM_X, TM_Y, RADIOMAP, CELL_X, CELL_Y FROM %s WHERE RADIOMAP LIKE '%%%s%%'";

    private Context context;
    private DBAdapter db;
    private Cursor cursor;

    private ArrayList<SCANINFO> scanList;

    private class FP_POS {
        /*
    private int IDX;                //격자id
    private double TM_X;            //평면좌표 x
    private double TM_Y;            //평면좌표 y
    private int NUM_LINK_HEADINGs;  //수집자 이동각도 수
    private String LINK_HEADING;    //수집자 이동각도
    private String FPFLOOR;         //층정보
    private int NUM_Aps;            //radiomap의 ap 수
    private String RADIOMAP;        //wifi 측위정보
    private String GID;             //역사id
    private double LONGITUDE;       //경도
    private double LATITUDE;        //위도
   */

        int idx; //격자id
        public double x; //평면좌표 x
        public double y; //평면좌표 y
        public int refCount; //todo : 일치하는 ap 수인 듯? 제대로 알아보기
        public double score; //todo: score가 정확히 뭔지 알아보기
        public int cell_x;
        public int cell_y;

        public FP_POS(int idx, double x, double y, int cell_x, int cell_y) {
            this.idx = idx;
            this.x = x;
            this.y = y;
            this.cell_x = cell_x;
            this.cell_y = cell_y;
        }
    }

    private class SCANINFO {
        public String infraID;  //mac
        public double wf;       //rssi
        public double wfRatio;  //rssi ratio

        SCANINFO(String infraID, double rssi) {
            this.infraID = infraID;
            double exp = -(-10. - rssi);
            this.wf = Math.pow(10.0, exp / 20.0);
            this.wfRatio = 0.0;
        }
    }

    public Estimate(Context mContext) {
        this.context = mContext;
        scanList = new ArrayList<>();
        db = new DBAdapter(context);
    }

    public void addScanItem(String infraID, double rssi) {
        scanList.add(new SCANINFO(infraID, rssi));
    }

    public ArrayList<SCANINFO> getScanList() {
        return scanList;
    }

    public String startEstimate() {
        return estimate(db, scanList);
    }

    //DB에 AP정보가 존재하는지 체크
    private boolean checkAP(DBAdapter db, String infraID) {
        boolean result = false;

        String sql = String.format(CHECK_AP, APTBL, infraID);
        cursor = db.search(sql);

        if (cursor != null) {
            result = true;
        }
        cursor.close();

        return result;
    }

    //측정 함수
    private String estimate(DBAdapter db, List<SCANINFO> scanList) {

        //scan 한 결과들 중 db에 없는 ap 는 제거하기
        double totalWF = 0.0; //rssi 값 총합
        int i = scanList.size() - 1;
        while (i >= 0) {
            SCANINFO scan = scanList.get(i);
            if (checkAP(db, scan.infraID)) totalWF += scan.wf;
            else scanList.remove(i);
            --i;
        }
        // rssi 값 총합이 0이면..
        if (totalWF == 0) {
            scanList.clear();
            return "NORESULT";
        }

        // search FP   <====    main algorithm !!
        List<FP_POS> fpList = new ArrayList<>();    //scan한 ap 정보와 유사한 position 리스트
        int posIdxByScore = 0;      // final position by score..
        double maxScore = 0.0;

        for (SCANINFO scan : scanList) {
            // calc wfRatio..
            scan.wfRatio = scan.wf / totalWF;
            // calc candidate pos using search result..
            String sql = String.format(SEARCH_FP, FPTBL, scan.infraID);

            cursor = db.search(sql);

            if(cursor != null) {
                while (cursor.moveToNext()) {
                    int idx = cursor.getInt(cursor.getColumnIndex("IDX"));
                    double x = cursor.getDouble(cursor.getColumnIndex("TM_X"));
                    double y = cursor.getDouble(cursor.getColumnIndex("TM_Y"));
                    int cell_x = cursor.getInt(cursor.getColumnIndex("CELL_X"));
                    int cell_y = cursor.getInt(cursor.getColumnIndex("CELL_Y"));

                    FP_POS pos = new FP_POS(idx, x, y, cell_x, cell_y);

                    int posIdx = fpList.indexOf(pos);
                    if (posIdx != -1) pos = fpList.get(posIdx);
                    else
                        posIdx = fpList.size();

                    String radiomap = cursor.getString(cursor.getColumnIndex("RADIOMAP")).trim();
                    StringTokenizer st = new StringTokenizer(radiomap, "/");
                    double posTotalWF = 0;
                    double posWF = 0;

                    while (st.hasMoreTokens()) {
                        String token = st.nextToken().toLowerCase();
                        String atom[] = token.split(";");
                        double rssi = Double.parseDouble(atom[1]);
                        double exp = -(-10. - rssi);
                        double wf = Math.pow(10.0, exp / 20.0);
                        posTotalWF += wf;

                        if (!atom[0].trim().equalsIgnoreCase(scan.infraID)) {
                            continue;
                        }

                        // infraID mac addres가 한번만 나온다는 전제
                        posWF = wf;
                    }

                    // assert : posTotalWF is not zero
                    if (posTotalWF == 0) continue;

                    double posWFRatio = posWF / posTotalWF;
                    // calc weight
                    double wfRatio = Math.min(scan.wf, posWF) / Math.max(scan.wf, posWF);
                    double wfScanRatio = Math.min(scan.wfRatio, posWFRatio);
                    double wfWeight = wfRatio * wfScanRatio;
                    // set candidate pos..
                    if (pos.refCount == 0) fpList.add(pos);
                    pos.refCount++;
                    pos.score += wfWeight;
                    // update final pos..
                    if (pos.score > maxScore) {
                        maxScore = pos.score;
                        posIdxByScore = posIdx;
                    }
                }
                cursor.close();
            }
        }

        //최종 position reuslt
        if (fpList.size() > 0) {
            String result;
            result = fpList.get(posIdxByScore).cell_x + "," + fpList.get(posIdxByScore).cell_y;
            return result;
        }

        return "NORESULT";
    }

    public void endEstimate() {
        try {
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}