package com.fesb.carduino;

import android.graphics.Color;
import android.os.Bundle;

import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.graph1);
        float[] list = {};
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            list = extras.getFloatArray("extra");
            createGraph(list);
        } else {
            list[0] = 0.0f;
            createGraph(list);
        }
    }

    private void createGraph(float[]  list) {

        GraphView graphView = new LineGraphView(this, "GraphViewDemo");

        GraphView.GraphViewData[] data = new GraphView.GraphViewData[list.length];
        float a;

        for (int i = 0; i < list.length; i++) {
            a = list[i];
            data[i] = new GraphView.GraphViewData(i, a);

        }
        /*GraphView.GraphViewData[] data = new GraphView.GraphViewData[list.size()];

        for (int i = 0; i < list.size(); i++) {
            data[i] = new GraphView.GraphViewData(i, list.get(i));
        }*/

            GraphViewSeries listG = new GraphViewSeries("Distance",
                    new GraphViewSeries.GraphViewSeriesStyle(Color.BLUE, 5), data);
            graphView.addSeries(listG);

            graphView.setShowLegend(true);
            graphView.getGraphViewStyle().setLegendWidth(200);

            LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout);
            layout.addView(graphView);
        }
}
