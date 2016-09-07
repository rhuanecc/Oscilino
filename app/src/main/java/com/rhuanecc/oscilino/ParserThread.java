package com.rhuanecc.oscilino;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rhuan on 2016-09-05.
 */
public class ParserThread extends Thread {
    LinkedBlockingQueue<Byte> fila;
    Handler uiHandler;
    String pontoStr;
    byte temp;

    public ParserThread(LinkedBlockingQueue<Byte> fila, Handler uiHandler) {
        this.setName("ParserThread");
        this.fila = fila;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        //Lê fila separando cada ponto
        pontoStr = new String();
        while(true) {
            try {
                temp = fila.take();

                if(temp == (byte)';'){        //se caracter ; -> delimitaçao do ponto
                    //Log.e("PARSER", Thread.currentThread().getName() +": "+ pontoStr);       //imprime ponto resultante
                    processaPonto(pontoStr);
                    pontoStr = new String();        //limpa string para proximo ponto
                }else {                        //se qualquer outro caracter
                    pontoStr = pontoStr.concat(String.valueOf((char)temp));  //concatena caracter no ponto
                }
            } catch (InterruptedException e) {

            }
        }
    }

    private void processaPonto(String ponto){
        String[] values = ponto.split(",");
        if(values.length == 3){
            int canal = Integer.parseInt(values[0]);
            int tempo = Integer.parseInt(values[1]);
            float valor = Float.parseFloat(values[2]);

            Ponto p = new Ponto(canal, tempo, valor);

            //envia ponto para grafico (atualiza GUI)
            Message m = new Message();
            m.obj = p;
            uiHandler.sendMessage(m);
        }
    }
}
