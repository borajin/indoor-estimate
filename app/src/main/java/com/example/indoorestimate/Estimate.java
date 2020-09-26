package com.example.indoorestimate;


import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Estimate {

    private static final String APTBL = "APLIST";
    private static final String FPTBL = "TR_FPDB";
    private static final String CHECK_AP = "SELECT 1 FROM %s WHERE INFRAID='%s'";
    private static final String SEARCH_FP = "SELECT IDX, GID, TM_X, TM_Y, FPFLOOR, RADIOMAP FROM %s WHERE RADIOMAP LIKE '%%%s%%'";
    //todo : SELECT IDX, GID, TM_X, TM_Y, FPFLOOR, RADIOMAP FROM %s WHERE MATCH (RADIOMAP) AGAINST ("%s") 왜 에러나는 지 알아보기.

    private Context context;
    private DBAdapter db;
    private Cursor cursor;

    private List<SCANINFO> scanList;

    public Estimate(Context mContext, List<SCANINFO> scanList) {
        this.context = mContext;
        this.scanList = scanList;
    }

    public String test_main() {
        //db 오픈
        db = new DBAdapter(context);

        //측정
        String result = estimate(db, scanList);

        //db 닫기
        try {
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "db close error";
        }

        return result;
    }

    //DB에 AP정보가 존재하는지 체크
    private boolean checkAP(DBAdapter db, String infraID) {
        boolean result = false;

        String sql = String.format(CHECK_AP, APTBL, infraID);
        cursor = db.search(sql);

        if (cursor != null) {
            result = true;
            cursor.close();
            //todo : cursor 가 null 값 가지면 close 안 해도 되는지?
        }

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
            return "no ap";
        }

        // search FP   <====    main algorithm !!
        List<FP_POS> fpList = new ArrayList<>();    //scan한 ap 정보와 유사한 position 리스트
        int posIdxByRef = 0;        // final position by ref count..
        int posIdxByScore = 0;      // final position by score..
        int maxRefCount = 0;
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
                    String gid = cursor.getString(cursor.getColumnIndex("GID")).trim();
                    double x = cursor.getDouble(cursor.getColumnIndex("TM_X"));
                    double y = cursor.getDouble(cursor.getColumnIndex("TM_Y"));
                    String floor = cursor.getString(cursor.getColumnIndex("FPFLOOR")).trim();

                    FP_POS pos = new FP_POS(idx, gid, x, y, floor);

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
                    if (pos.refCount > maxRefCount) {
                        maxRefCount = pos.refCount;
                        posIdxByRef = posIdx;
                    }
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

            result = "[by reference count] " + "\nposX: " + fpList.get(posIdxByRef).x + "\nposY: " + fpList.get(posIdxByRef).y + "\nrefCount : " + fpList.get(posIdxByRef).refCount + "\nscore : " + fpList.get(posIdxByRef).score;
            result = result + "\n\n";
            result = result + "[by score]" + "\nposX: " + fpList.get(posIdxByScore).x + "\nposY: " + fpList.get(posIdxByRef).y + "\nrefCount : " + fpList.get(posIdxByRef).refCount + "\nscore : " + fpList.get(posIdxByRef).score;

            return result;
        }

        return "no result";
    }
}