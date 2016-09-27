package com.rhuanecc.oscilino;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.rhuanecc.oscilino.BT.BtReceiverThread;

import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {
    public static final int POINTS_COUNT = 1000;
    public static final int SET_CH0_DATA = 0;
    public static final int SET_CH1_DATA = 1;


    GraphView graph;
    LineGraphSeries<DataPoint> ch0;
    LineGraphSeries<DataPoint> ch1;
    BtReceiverThread receiver;

    DataPoint[] graphData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitle("Voltage x Time (ms)");
        //graph.setHorizontalScrollBarEnabled(true);
        graph.setKeepScreenOn(true);

        //Dados para grafico
        ch0 = new LineGraphSeries<>();
        ch1 = new LineGraphSeries<>();
        ch1.setColor(Color.RED);
        graph.addSeries(ch0);    //Add dados no grafico
        graph.addSeries(ch1);    //Add dados no grafico

        //Mostra info do ponto clicado
        ch0.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(GraphActivity.this, dataPoint.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //Inicia nova thread para receber os dados
        receiver = new BtReceiverThread(uiHandler);
        receiver.start();

        graphData = new DataPoint[POINTS_COUNT];

        Toast.makeText(GraphActivity.this, "Sincronizando...", Toast.LENGTH_SHORT).show();
    }

    Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == SET_CH0_DATA){                          //canal 0
                ch0.resetData((DataPoint[])msg.obj);        //recebe 1000 pontos mais recentes

            } else if(msg.arg1 == SET_CH1_DATA) {                   //canal 1
                ch1.resetData((DataPoint[]) msg.obj);        //recebe 1000 pontos mais recentes
            }
        }
    };

    //Tela cheia
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiver.close();
    }
}
