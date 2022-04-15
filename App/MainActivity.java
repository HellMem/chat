package com.project.chatapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLContext;


public class MainActivity extends AppCompatActivity {

    private Button btnSend, btnBroadcast, btnConectar, btnDesconectar;
    private EditText etMensaje;
    private TextView txtIds;

    private SignalRService mService;
    private boolean mBound = false;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBroadcast = findViewById(R.id.btnBroadcast);
        btnSend = findViewById(R.id.btnSend);
        btnConectar = findViewById(R.id.btnConectar);
        btnDesconectar = findViewById(R.id.btnDesconectar);

        etMensaje = findViewById(R.id.etMensaje);
        txtIds = findViewById(R.id.txtIds);

        btnBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message();
            }
        });
        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conectar();
            }
        });
        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desconectar();
            }
        });
    }

    private void conectar() {
        if (!mBound) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SignalRService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            mService.startSignalR(new ConexionId() {
                @Override
                public void obtenerIdConexion(String IdConexion) {
                    String acumulado = txtIds.getText().toString();
                    txtIds.setText(acumulado + "\n" + IdConexion);
                }
            });
        }
    }

    private void desconectar() {
        mService.desconectar();
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*
        if (networkChangeReceiver == null)
            networkChangeReceiver = new NetworkChangeReceiver();

        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
        */
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*
        if (networkChangeReceiver != null) unregisterReceiver(networkChangeReceiver);
        */
    }

    private class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                if (isNetworkAvailable(context)) {
                    if (!mBound) {
                        Intent intentService = new Intent();
                        intentService.setClass(getApplicationContext(), SignalRService.class);
                        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
                    }
                } else {
                    unbindService(mConnection);
                    mBound = false;
                }
            }
        }


    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    private void message() {
        if (!isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = etMensaje.getText().toString();

        if (mBound && !message.equals("")) {
            mService.message(message);
        }
    }


    private void broadcast() {
        if (!isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBound) {
            mService.broadCast();
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;

            Toast.makeText(getApplicationContext(), "Hola mundo", Toast.LENGTH_SHORT).show();

            if (isNetworkAvailable(getApplicationContext())) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SignalRService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            }
        }
    };



    /*
    private WebSocketClient mWebSocketClient;

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("wss://ws.compusoluciones.com/debug/gcoronad/chat/api/chat?username=memo");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        output(message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
                //output(s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
                //output(e.getMessage());
            }
        };

        mWebSocketClient.connect();
    }

    private void output(final String txt) {
        Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
    }

    */
}
