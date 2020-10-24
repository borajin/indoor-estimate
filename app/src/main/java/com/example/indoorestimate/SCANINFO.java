package com.example.indoorestimate;

public class SCANINFO {
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
