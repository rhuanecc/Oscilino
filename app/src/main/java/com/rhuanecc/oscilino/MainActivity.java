package com.rhuanecc.oscilino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rhuanecc.oscilino.BT.BtConnectThread;
import com.rhuanecc.oscilino.BT.BtSocket;
import com.rhuanecc.oscilino.BT.DevicesList;

public class MainActivity extends AppCompatActivity {
    private static final int SOLICITA_BT = 1;
    private static final int SOLICICTA_CONEXAO = 2;

    Button btnBT;

    BluetoothAdapter btAdapter;
    BluetoothDevice btDevice;

    private static String MAC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBT = (Button) findViewById(R.id.btnBT);

        //botao bluetooth
        btnBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                //sem bluetooth
                if(btAdapter == null){
                    Toast.makeText(getApplicationContext(), "Dispositivo não possui bluetooth", Toast.LENGTH_SHORT).show();

                    //bt desativado -> requisita ativaçao
                } else if(!btAdapter.isEnabled()){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, SOLICITA_BT);

                    //bt ativado -> mostra pareados -> recebe mac do bt escolhido
                } else {
                    Intent abreLista = new Intent(MainActivity.this, DevicesList.class);
                    startActivityForResult(abreLista, SOLICICTA_CONEXAO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SOLICITA_BT:
                //se ativou bt -> mostra pareados -> recebe mac do bt escolhido
                if (resultCode == Activity.RESULT_OK) {   //se ativou bt
                    Intent abreLista = new Intent(MainActivity.this, DevicesList.class);
                    startActivityForResult(abreLista, SOLICICTA_CONEXAO);
                } else{
                    Toast.makeText(getApplicationContext(), "Bluetooth não foi ativado", Toast.LENGTH_SHORT).show();
                }
                break;

            //se retornou mac do dispositivo escolhido
            case SOLICICTA_CONEXAO:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getStringExtra("MAC");
                    btDevice = btAdapter.getRemoteDevice(MAC);

                    //Inicia nova thread para conectar bluetooth
                    BtConnectThread btConnect = new BtConnectThread(btDevice, btAdapter, socketHandler);
                    btConnect.start();
                } else{
                    Toast.makeText(getApplicationContext(), "Dispositivo bluetooth não escolhido", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //se conectar usando a thread BtConnect recebe socket pelo handler
    Handler socketHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 0 || BtSocket.getSocket() ==  null){
                Toast.makeText(getApplicationContext(), "Falha ao conectar bluetooth", Toast.LENGTH_LONG).show();
            }else{   //Se conectado com sucesso
                //Abre activity do grafico passando socket
                Intent i = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(i);
            }
        }
    };
}
