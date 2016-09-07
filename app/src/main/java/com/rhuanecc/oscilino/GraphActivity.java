package com.rhuanecc.oscilino;

import android.bluetooth.BluetoothSocket;
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

public class GraphActivity extends AppCompatActivity {
    GraphView graph;
    LineGraphSeries<DataPoint> ch1;
    LineGraphSeries<DataPoint> ch2;
    BtReceiverThread receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        graph = (GraphView) findViewById(R.id.graph);
        graph.setTitle("Voltage x Time");
        //graph.setBackgroundColor(Color.DKGRAY);
        graph.setHorizontalScrollBarEnabled(true);
        graph.setKeepScreenOn(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(200);

        //Dados para grafico
        ch1 = new LineGraphSeries<>();
        ch2 = new LineGraphSeries<>();
        //ch1.setColor(Color.YELLOW);
        graph.addSeries(ch1);    //Add dados no grafico
        graph.addSeries(ch2);    //Add dados no grafico


        //Mostra info do ponto clicado
        ch1.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(GraphActivity.this, dataPoint.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //Inicia nova thread para receber os dados
        receiver = new BtReceiverThread(uiHandler);
        receiver.start();
    }


    Handler uiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            Ponto p = (Ponto)msg.obj;
            //Log.e("uiHandler", "Recebeu msg: "+p);
            if(p.canal == 1){
                ch1.appendData(new DataPoint(p.tempo, p.valor), true, 200);
            } else if(p.canal == 2){
                ch2.appendData(new DataPoint(p.tempo, p.valor), true, 200);
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
