package com.rhuanecc.oscilino.BT;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.rhuanecc.oscilino.ParserThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Recebe os dados via bluetooth e passa para o parser
 */
public class BtReceiverThread extends Thread {
    private final BluetoothSocket socket;
    private final InputStream in;
    private final OutputStream out;
    private Handler uiHandler;
    private boolean receive = true;

    public LinkedBlockingQueue<Byte> fila;

    public BtReceiverThread(Handler handler) {
        this.setName("ReceiverThread");
        this.uiHandler = handler;
        socket = BtSocket.getSocket();
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("BT", "Erro in/out stream");
            e.printStackTrace();
        }

        in = tmpIn;
        out = tmpOut;

        fila = new LinkedBlockingQueue(); //fila de comunicação entre receiver e parser

        new ParserThread(fila, uiHandler).start();   //inicia thread parser
    }

    //Thread para recepção dos dados
    @Override
    public void run() {
        byte a;

        while(receive){
            try{
                a = (byte) in.read();

                fila.offer(a);   //adiciona caracter na fila, caso cheia descarta
                //Log.e("RECEIVER", Thread.currentThread().getName() +": "+ a);

            } catch (IOException e) {
                Log.e("BT", "Erro in.read");
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void write(byte[] bytes){
        try{
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            receive = false;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
