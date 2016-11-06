package com.rhuanecc.oscilino;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rhuan on 2016-09-05.
 */
public class ParserThread extends Thread {
    LinkedBlockingQueue<Byte> fila;
    Buffer buffer;
    ArrayList<Integer> graphBuffer;
    DataPoint[] graphData;
    Handler uiHandler;
    byte temp;
    boolean parse = true;

    public ParserThread(LinkedBlockingQueue<Byte> fila, Handler uiHandler) {
        this.setName("ParserThread");
        this.fila = fila;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        buffer = new Buffer();
        graphBuffer = new ArrayList<>(); //thread safe list
        int tempInt;
        long tempLong;

        while(fila.peek() == null); //aguarda chegar algo na fila
        try {
            Thread.sleep(10);      //espera chegar mais dados na fila
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(fila.peek() != null && fila.peek() != (byte)';') {  //consome fila até o inicio da proxima serie completa
           fila.remove();
        }

        //deve ocorrer a cada SCREEN_REFRESH_INTERVAL ms
        //converte para DataPoint[] calculando escala de tensão e tempo
        Timer t = new Timer("TimerThread");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(!GraphActivity.paused) {
                    graphBuffer = GraphBuffer.getInstance();
                    if (graphBuffer.size() == GraphActivity.graphPointsNumber *GraphActivity.takeSampleEvery) {  //só atualiza gui quando tiver tela completa

                        graphData = new DataPoint[GraphActivity.graphPointsNumber];

                        int j=0;
                        int size = GraphActivity.graphPointsNumber * GraphActivity.takeSampleEvery;    //tamanho do buffer de acordo com escala de tempo
                        for(int i=0; i<size; i+=GraphActivity.takeSampleEvery) {
                            graphData[j] = new DataPoint(i * GraphActivity.TIME_SCALE, (float)graphBuffer.get(i) * GraphActivity.voltageScale);
                            j++;
                        }

                        //envia ponto para grafico (atualiza GUI)
                        Message m = new Message();
                        m.arg1 = GraphActivity.SET_CH0_DATA;     //substitui dados do canal 0
                        m.obj = graphData;                       //pontos
                        uiHandler.sendMessage(m);
                    }
                }
            }
        };
        t.scheduleAtFixedRate(task, GraphActivity.SCREEN_REFRESH_INTERVAL, GraphActivity.SCREEN_REFRESH_INTERVAL);


        //Lê fila separando cada conjunto de pontos
        while(parse){
            try {
                temp = fila.take(); //bloqueante até que haja algo na fila

                if(temp == (byte)';'){      //fim do buffer
                    processaBuffer();     //checa integridade, envia para gui
                    buffer = new Buffer();      //inicia novo buffer (novo conjunto de pontos chegará)

                    //inicio do proximo conjunto
                    buffer.setCh(fila.take());  //identifica canal sendo recebido

                }else if(temp == (byte)','){    //se virgula, recebe proximos 4 bytes como cks, converte para long
                    tempLong = ((fila.take()&0xFF) << 24);     //recebe byte mais significativos
                    tempLong += ((fila.take()&0xFF) << 16);    //2nd byte
                    tempLong += ((fila.take()&0xFF) << 8);     //3rd byte
                    tempLong += (fila.take()&0xFF);            //4th byte
                    buffer.setCks(tempLong);            //cks enviado ao buffer

                }else{                        //une 2 bytes em um int
                    tempInt = ((temp&0xFF) << 8);      //recebe 8 bits mais significativos
                    tempInt += (fila.take()&0xFF);     //soma aos 8 bits menos significativos
                    buffer.add(tempInt);     //converte para escala do ADC e add no buffer
                }
            } catch (InterruptedException e) {}
        }
    }

    //Verifica checksum, se ok envia pontos para gui
    private void processaBuffer(){
        if(buffer.isValid()){
            addPontosGraphBuffer();
            //TimerTask irá enviar graphBuffer para GUI a cada SCREEN_REFRESH_INTERVAL
        }
        else{
            //Log.e("CHECKSUM","Buffer descartado");
        }
    }

    /**Adiciona pontos do buffer de recebimento ao buffer do grafico limitando em graphPointsNumber mais recentes*/
    private void addPontosGraphBuffer(){
        for(Integer p : buffer.getPontos()) {
            GraphBuffer.add(p);
        }
    }

    /** Envia buffer completo recebido. Deve receber protocolo com graphPointsNumber pontos do Arduino.*/
    private void enviaBufferCompleto(){
        int i = 0;
        int tempo = 0;
        graphData = new DataPoint[GraphActivity.graphPointsNumber];

        if(buffer.getPontos().size() > 0) {
            for (Integer p : buffer.getPontos()) {
                if (p != null) {
                    graphData[i] = new DataPoint(tempo * GraphActivity.TIME_SCALE, (float)p * GraphActivity.voltageScale);
                    i++;
                }
                tempo++;    //se valor null compensa ponto não recebido "esticando" grafico
            }

            //envia ponto para grafico (atualiza GUI)
            Message m = new Message();
            m.arg1 = GraphActivity.SET_CH0_DATA;     //substitui dados do canal 0
            m.obj = graphData;                       //pontos
            uiHandler.sendMessage(m);
        }
    }

    public void close(){
        parse = false;

    }
}
