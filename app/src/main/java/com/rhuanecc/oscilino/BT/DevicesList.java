package com.rhuanecc.oscilino.BT;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by rhuan on 2016-08-23.
 */
public class DevicesList extends ListActivity {
    private BluetoothAdapter btAdapter;

    private static String MAC_ADDR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        this.setListAdapter(btArrayAdapter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> dispositivosPareados = btAdapter.getBondedDevices();   //lista dispositivos pareados

        if(dispositivosPareados.size()>0){
            for(BluetoothDevice device : dispositivosPareados){ //add cada dispositivo na listView
                String nomeBt = device.getName();
                String macBt = device.getAddress();
                btArrayAdapter.add(nomeBt+"\n"+macBt);
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String info = ((TextView) v).getText().toString();

        MAC_ADDR = info.substring(info.length()-17);

        //retorna endereço mac selecionado para activity anterior
        Intent i = new Intent();
        i.putExtra("MAC",MAC_ADDR);
        setResult(RESULT_OK, i);
        this.finish();
    }
}
