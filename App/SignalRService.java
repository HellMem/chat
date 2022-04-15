package com.project.chatapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import microsoft.aspnet.signalr.client.Action;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.Credentials;
import microsoft.aspnet.signalr.client.Platform;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.StateChangedCallback;
import microsoft.aspnet.signalr.client.http.Request;
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

/**
 * Created by gcoronad on 25/06/2018.
 */

public class SignalRService extends Service {
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler; // to display Toast message
    private final IBinder mBinder = new LocalBinder(); // Binder given to clients

    public SignalRService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        startSignalR(null);
        return result;
    }

    @Override
    public void onDestroy() {
        mHubConnection.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        startSignalR(null);
        return mBinder;
    }


    public class LocalBinder extends Binder {
        public SignalRService getService() {
            // Return this instance of SignalRService so clients can call public methods
            return SignalRService.this;
        }
    }


    public void broadCast() {
        String SERVER_METHOD_BROADCAST = "BroadCast";
        SignalRFuture<Void> furtue = mHubProxy.invoke(SERVER_METHOD_BROADCAST);
        furtue.done(new Action<Void>() {
            @Override
            public void run(Void aVoid) throws Exception {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });

    }

    public void message(String message) {
        String SERVER_METHOD_MESSAGE_OTHERS = "Message";
        SignalRFuture<String> future = mHubProxy.invoke(String.class, SERVER_METHOD_MESSAGE_OTHERS, mHubConnection.getConnectionId(), message);
        try {
            String respuesta = future.get(3, TimeUnit.SECONDS);
            Toast.makeText(getApplicationContext(), respuesta, Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "valió cake", Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "valió cake", Toast.LENGTH_SHORT).show();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "valió cake, en tiempo", Toast.LENGTH_SHORT).show();
        }


    }

    public void desconectar() {
        mHubConnection.stop();
    }

    private void connectionLost() {
        //stopSelf();
    }


    public void startSignalR(ConexionId conexionId) {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        Credentials credentials = new Credentials() {
            @Override
            public void prepareRequest(Request request) {
                request.addHeader("IdSolicitud", "24");
                request.addHeader("IdEmpleado", "2243");
            }
        };

        String serverUrl = "https://ws.compusoluciones.com/debug/gcoronad/Chat";
        mHubConnection = new HubConnection(serverUrl);
        mHubConnection.setCredentials(credentials);
        String SERVER_HUB_CHAT = "MyHub";
        mHubProxy = mHubConnection.createHubProxy(SERVER_HUB_CHAT);
        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);

        mHubConnection.connectionSlow(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Ta lento", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mHubConnection.reconnecting(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Ta reconectando", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        mHubConnection.connected(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        mHubConnection.closed(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT).show();
                        connectionLost();
                    }
                });
            }
        });


        try {
            signalRFuture.get();
            String connectionId = mHubConnection.getConnectionId();
            if (conexionId != null)
                conexionId.obtenerIdConexion(connectionId);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        String CLIENT_BROADCAST_METHOD = "BroadCast";

        mHubProxy.on(CLIENT_BROADCAST_METHOD, new SubscriptionHandler1<String>() {
            @Override
            public void run(final String s) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, String.class);


    }
}
