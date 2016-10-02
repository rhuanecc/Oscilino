package com.rhuanecc.oscilino;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.rhuanecc.oscilino.BT.BtReceiverThread;

import java.util.ArrayList;

public class GraphActivity extends AppCompatActivity {
    public static final int SET_CH0_DATA = 0;
    public static final int SET_CH1_DATA = 1;
    public static final float TIME_SCALE = (float) 0.120;          //120us a cada ponto -> 0.12ms
    public static final float VOLTAGE_SCALE = (float) 0.00488;     //4.88mV
    public static final int SCREEN_REFRESH_INTERVAL = 100;         //intervalo entre cada atualização da tela (ms)

    public static boolean paused = false;
    public static int graphPointsNumber = 700;          //quantidade de pontos no gráfico, reduzir para reduzir escala de tempo
    public static int takeSampleEvery = 1;              //quantidade de amostras a serem ignoradas para aumentar escala de tempo
    public static float voltageScale = VOLTAGE_SCALE;   //escala de tensão utilizada no circuito de condicionamento

    ToggleButton pauseButton;
    Spinner timeSpinner;
    Spinner voltageSpinner;

    GraphView graph;
    LineGraphSeries<DataPoint> ch0;
    LineGraphSeries<DataPoint> ch1;
    BtReceiverThread receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        //======================================== Controles ========================================
        pauseButton = (ToggleButton) findViewById(R.id.pauseButton);
        pauseButton.setOnCheckedChangeListener(pauseListener);

        timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.time_array, android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(timeSpinnerListener);
        timeSpinner.setSelection(2);        //80ms

        voltageSpinner = (Spinner) findViewById(R.id.voltageSpinner);
        ArrayAdapter<CharSequence> voltageAdapter = ArrayAdapter.createFromResource(this, R.array.voltage_array, android.R.layout.simple_spinner_dropdown_item);
        voltageSpinner.setAdapter(voltageAdapter);
        voltageSpinner.setOnItemSelectedListener(voltageSpinnerListener);
        voltageSpinner.setSelection(1);     //5v

        //========================================= Grafico =========================================
        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitle("Voltage x Time (ms)");
        graph.setKeepScreenOn(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(80);
        graph.getViewport().setScalable(true);                      //enable zoom
        graph.getGridLabelRenderer().setNumHorizontalLabels(9);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);

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
                double time = dataPoint.getX();
                double voltage = dataPoint.getY();

                Toast.makeText(GraphActivity.this, String.format("%.3f ms   -->   %.3f V", time, voltage), Toast.LENGTH_SHORT).show();
            }
        });

        //Inicia nova thread para receber os dados
        receiver = new BtReceiverThread(uiHandler);
        receiver.start();

        //graphData = new DataPoint[graphPointsNumber];
    }

    Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == SET_CH0_DATA){                          //canal 0
                ch0.resetData((DataPoint[])msg.obj);

            } else if(msg.arg1 == SET_CH1_DATA) {                   //canal 1
                ch1.resetData((DataPoint[]) msg.obj);
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

    private CompoundButton.OnCheckedChangeListener pauseListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            paused = isChecked;
        }
    };


    private AdapterView.OnItemSelectedListener timeSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selected = (String) timeSpinner.getItemAtPosition(position);
            switch (selected){
                case "20 ms":
                    takeSampleEvery = 1;
                    graph.getViewport().setMaxX(20);
                    graphPointsNumber = 175;
                    break;
                case "40 ms":
                    takeSampleEvery = 1;
                    graph.getViewport().setMaxX(40);
                    graphPointsNumber = 350;
                    break;
                case "80 ms":
                    takeSampleEvery = 1;
                    graph.getViewport().setMaxX(80);
                    graphPointsNumber = 700;
                    break;
                case "160 ms":
                    takeSampleEvery = 2;
                    graph.getViewport().setMaxX(160);
                    graphPointsNumber = 700;
                    break;
                case "320 ms":
                    takeSampleEvery = 4;
                    graph.getViewport().setMaxX(320);
                    graphPointsNumber = 700;
                    break;
                case "640 ms":
                    takeSampleEvery = 8;
                    graph.getViewport().setMaxX(640);
                    graphPointsNumber = 700;
                    break;
                case "1280 ms":
                    takeSampleEvery = 16;
                    graph.getViewport().setMaxX(1280);
                    graphPointsNumber = 700;
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    //Configurar de acordo com circuito de condicionamento do sinal (atenuação/amplificação)
    private AdapterView.OnItemSelectedListener voltageSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            float factor = 1;
            String selected = (String) voltageSpinner.getItemAtPosition(position);
            switch (selected){
                case "1 V":
                    factor = (float)(1.0/5.0);
                    break;
                case "5 V":
                    factor = 1;
                    break;
                case "20 V":
                    factor = 4;
                    break;
            }

            voltageScale = VOLTAGE_SCALE*factor;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}
