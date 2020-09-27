package com.example.indoorestimate;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.widget.SeekBar;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class MapDraw implements OnChartValueSelectedListener {

    private ScatterChart map;
    private Context context;
    float minX = 0, minY = 0;
    ArrayList<IScatterDataSet> dataSets;

    public MapDraw(Context context, ScatterChart map) {
        this.context = context;
        this.map = map;

        map.getDescription().setEnabled(false);
        map.setOnChartValueSelectedListener(this);

        map.setDrawGridBackground(false);
        map.setTouchEnabled(true);
        map.setMaxHighlightDistance(50f);

        // enable scaling and dragging
        map.setDragEnabled(false);
        map.setScaleEnabled(false);

        map.setMaxVisibleValueCount(200);
        map.setPinchZoom(false);

        Legend l = map.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        //l.setTypeface(tfLight);
        l.setXOffset(5f);

        YAxis yl = map.getAxisLeft();
        //yl.setTypeface(tfLight);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        map.getAxisRight().setEnabled(false);

        XAxis xl = map.getXAxis();
        //xl.setTypeface(tfLight);
        xl.setDrawGridLines(true);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", xIndex: " + e.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {

    }

    private void get_min() {
        ArrayList<Entry> values1 = new ArrayList<>();
        DBAdapter dbAdapter = new DBAdapter(context);

        Cursor cursor = dbAdapter.search("SELECT * FROM TR_FPDB");

        boolean first = false;

        while(cursor.moveToNext()) {
            if(first == false) {
                minX = cursor.getFloat(cursor.getColumnIndex("TM_X"));
                minY = cursor.getFloat(cursor.getColumnIndex("TM_Y"));

                first = true;
            }

            float x = cursor.getFloat(cursor.getColumnIndex("TM_X"));
            float y = cursor.getFloat(cursor.getColumnIndex("TM_Y"));

            if(minX >= x) {
                minX = x;
            }

            if(minY >= y) {
                minY = y;
            }
        }

        cursor.close();
    }

    public void setMapData() {
        ArrayList<Entry> values1 = new ArrayList<>();
        DBAdapter dbAdapter = new DBAdapter(context);

        Cursor cursor = dbAdapter.search("SELECT * FROM TR_FPDB");

        get_min();

        while(cursor.moveToNext()) {
            float x = cursor.getFloat(cursor.getColumnIndex("TM_X")) - minX;
            float y = cursor.getFloat(cursor.getColumnIndex("TM_Y")) - minY;

            values1.add(new Entry(x, y));
        }

        cursor.close();

        // create a dataset and give it a type
        ScatterDataSet set1 = new ScatterDataSet(values1, "장전역");
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        set1.setColor(Color.parseColor("#DADADA"));

        set1.setScatterShapeSize(8f);

        dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);
        //data.setValueTypeface(tfLight);

        refreshMap(data);
    }

    public void setXY(float x, float y) {
        get_min();

        x = x - minX;
        y = y - minY;

        ArrayList<Entry> location = new ArrayList<>();
        location.add(new Entry(x, y));
        ScatterDataSet locationSet = new ScatterDataSet(location, "현재 위치");
        locationSet.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        locationSet.setColor(Color.parseColor("#FF0000"));

        locationSet.setScatterShapeSize(20f);

        dataSets.add(locationSet); // add the data sets
        ScatterData data = new ScatterData(dataSets);

        refreshMap(data);
    }

    public void refreshMap(ScatterData data) {
        map.setData(data);
        map.invalidate();
    }
}
