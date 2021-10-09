package com.example.polynomial;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class Graph extends AppCompatActivity {

    private LineGraphSeries<DataPoint> ptsSeries;
    private double x, y;
    private ArrayList<tableRow> function;
    private TextView fx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        function = (ArrayList<tableRow>) getIntent().getSerializableExtra("function"); // get the selected function from the previous activity

        x = -10.0; //start of graph

        GraphView graph = findViewById(R.id.graph1); // integrate graph view
        ptsSeries = new LineGraphSeries<>();

        fx=findViewById(R.id.fx); // integrate the f(x) title
        fx.setText(getIntent().getStringExtra("title"));
        double minY=calculateFx(-10),maxY=calculateFx(10);
        for (int i = 0; i < 200; i++, x = x + 0.1) { // function domain: -10<x<10
            y = calculateFx(x); // y=f(x)
            if(y<minY) minY=y;
            if(y>maxY) maxY=y;

            ptsSeries.appendData(new DataPoint(x, y), false, 500);
        }
        graph.addSeries(ptsSeries);

        graph.getViewport().setMinX(-10);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setMinY(minY-5);
        graph.getViewport().setMaxY(maxY+5);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
    }

    private double calculateFx(double x) {
        double y = 0;
        for (tableRow term : function)
            y += term.getCoefficient() * Math.pow(x, term.getPower());
        return y;
    }
}