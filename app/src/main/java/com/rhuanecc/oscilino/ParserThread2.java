package com.rhuanecc.oscilino;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rhuanecc.oscilino.Ponto;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by rhuan on 2016-09-05.
 */
public class ParserThread2 extends Thread {
    LinkedBlockingQueue<Byte> fila;
    Buffer buffer;
    Handler uiHandler;
    String pontoStr;
    byte temp;

    public ParserThread2(LinkedBlockingQueue<Byte> fila, Handler uiHandler) {
        this.setName("ParserThread");
        this.fila = fila;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        buffer = new Buffer();
        pontoStr = new String();
        int tempInt;
        long tempLong;

        while(fila.peek() != null); //aguarda chegar algo na fila
        //Thread.sleep(1);  //espera chegar mais coisa
        while(fila.peek() != null && fila.peek() != (byte)';') {  //consome fila até o inicio da proxima serie completa
            //if(fila.peek() != null)
               fila.remove();
        }

        //Lê fila separando cada conjunto de pontos
        while(true) {
            try {
                temp = fila.take(); //bloqueante até que haja algo na fila

                if(temp == (byte)';'){      //fim do buffer
                    processaBuffer(buffer);     //checa integridade, envia para gui
                    buffer = new Buffer();      //inicia novo buffer (novo conjunto de pontos chegará)

                    //inicio do proximo conjunto
                    buffer.setCh(fila.take());  //identifica canal sendo recebido

                }else if(temp == (byte)','){    //se virgula, recebe proximos 4 bytes como cks, converte para long
                    tempLong = ((fila.take()&0xFF) << 24);     //recebe byte mais significativos
                    tempLong += ((fila.take()&0xFF) << 16);    //2nd byte
                    tempLong += ((fila.take()&0xFF) << 8);     //3rd byte
                    tempLong += (fila.take()&0xFF);            //4th byte
                    buffer.setCks(tempLong);            //cks enviado ao buffer
                    Log.d("Recebeu Long",Long.toString(tempLong));

                }else{                        //une 2 bytes em um int
                    tempInt = ((temp&0xFF) << 8);      //recebe 8 bits mais significativos
                    tempInt += (fila.take()&0xFF);     //soma aos 8 bits menos significativos
                    buffer.add((float)tempInt);     //converte para escala do ADC e add no buffer
                    Log.d("Recebeu Int",Integer.toString(tempInt));
                }
            } catch (InterruptedException e) {

            }
        }
    }

    //Verifica checksum, se ok envia pontos para gui
    private void processaBuffer(Buffer b){
        Log.e("BUFFER",b.toString());
        if(b.isValid()){

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
