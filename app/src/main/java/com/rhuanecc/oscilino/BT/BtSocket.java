package com.rhuanecc.oscilino.BT;

import android.bluetooth.BluetoothSocket;

/**
 * Created by rhuan on 2016-09-07.
 */
public class BtSocket {
    private static BluetoothSocket socket;

    public static BluetoothSocket getSocket() {
        return socket;
    }

    public static void setSocket(BluetoothSocket socket) {
        BtSocket.socket = socket;
    }
}
