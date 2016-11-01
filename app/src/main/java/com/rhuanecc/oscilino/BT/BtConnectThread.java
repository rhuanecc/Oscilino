package com.rhuanecc.oscilino.BT;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Conecta com um dispositivo bluetooth passando o socket correspondente para a thread que
 * recebe os dados
 */
public class BtConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mmAdapter;
    private final Handler mmHandler;

    private static UUID sockUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BtConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        BluetoothSocket tmpSock = null;
        mmDevice = device;
        mmAdapter = adapter;
        mmHandler = handler;

        //Tenta conectar usando UUID
        try {
            tmpSock = mmDevice.createRfcommSocketToServiceRecord(sockUUID);
        } catch (IOException e) { }
        mmSocket = tmpSock;
    }

    public void run() {
        mmAdapter.cancelDiscovery();

        try {
            mmSocket.connect();
            Log.e("BT", "BT Connected");
            BtSocket.setSocket(mmSocket);   //set socket on static attribute
            Message m = new Message();
            m.arg1 = 1;
            mmHandler.sendMessage(m);
            //conectado
        } catch (IOException connectException) {
            Message m = new Message();
            m.arg1 = 0;
            mmHandler.sendMessage(m);
            //erro ao conectar
            try {
                mmSocket.close();
            } catch (IOException closeException) {/*erro ao fechar socket*/ }
            return;
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
