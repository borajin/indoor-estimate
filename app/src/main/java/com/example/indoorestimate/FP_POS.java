package com.example.indoorestimate;

public class FP_POS {
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
