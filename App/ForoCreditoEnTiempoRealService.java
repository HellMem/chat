package com.project.chatapp;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Random;
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
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;
import microsoft.aspnet.signalr.client.transport.ClientTransport;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

/**
 * Created by gcoronad on 26/07/2018.
 */

public class ForoCreditoEnTiempoRealService extends Service {
    private HubConnection mHubConnection;
    private HubProxy mHubProxy;
    private Handler mHandler;
    private final String serverUrl = "https://ws.compusoluciones.com/debug/gcoronad/SolicitudCreditoAPI/v3";
    private final String SERVER_FORO_CREDITO_HUB = "ForosCreditoHub";
    private final IBinder mBinder = new LocalBinder();
    private RealTimeListener realTimeListener;

    public ForoCreditoEnTiempoRealService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        if (mHubConnection != null)
            mHubConnection.stop();
        super.onDestroy();
    }


    public class LocalBinder extends Binder {
        public void conectar(String idSolicitud, String idEmpleado, String accessToken, RealTimeListener realTimeListener) {
            ForoCreditoEnTiempoRealService.this.realTimeListener = realTimeListener;
            conectarASignalR(idSolicitud, idEmpleado, accessToken);
        }

        public void desconectar() {
            desconectarSignalR();
        }


        public void estaEscribiendoAlguien() {
            String SERVER_METHOD_ESTA_ESCRIBIENDO_ALGUIEN = "EstaEscribiendoAlguien";
            mHubProxy.invoke(SERVER_METHOD_ESTA_ESCRIBIENDO_ALGUIEN);
        }

        public void notificarNuevoComentario() {
            String SERVER_METHOD_NOTIFICAR_NUEVO_COMENTARIO = "NotificarNuevoComentario";
            mHubProxy.invoke(SERVER_METHOD_NOTIFICAR_NUEVO_COMENTARIO);
        }

        public void notificarEscritura() {
            String SERVER_METHOD_NOTIFICAR_ESCRITURA = "NotificarEscritura";
            mHubProxy.invoke(SERVER_METHOD_NOTIFICAR_ESCRITURA);
        }

        public void notificarFinEscritura() {
            String SERVER_METHOD_NOTIFICAR_FIN_ESCRITURA = "NotificarFinEscritura";
            mHubProxy.invoke(SERVER_METHOD_NOTIFICAR_FIN_ESCRITURA);
        }


    }

    private void conectarASignalR(final String idSolicitud, final String idEmpleado, final String accessToken) {
        Platform.loadPlatformComponent(new AndroidPlatformComponent());

        Credentials credentials = new Credentials() {
            @Override
            public void prepareRequest(Request request) {
                request.addHeader("IdSolicitud", idSolicitud);
                request.addHeader("IdEmpleado", idEmpleado);
                request.addHeader("Authorization", accessToken);
            }
        };

        mHubConnection = new HubConnection(serverUrl);
        mHubConnection.setCredentials(credentials);

        mHubProxy = mHubConnection.createHubProxy(SERVER_FORO_CREDITO_HUB);

        ClientTransport clientTransport = new ServerSentEventsTransport(mHubConnection.getLogger());
        SignalRFuture<Void> signalRFuture = mHubConnection.start(clientTransport);


        signalRFuture.done(new Action<Void>() {
            @Override
            public void run(Void aVoid) throws Exception {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        realTimeListener.conexionExitosa();
                    }
                });
            }
        });


        agregarMetodosCallBack(mHubConnection);
        agregarMetodosCliente(mHubProxy);
    }

    private void agregarMetodosCallBack(HubConnection mHubConnection) {
        mHubConnection.closed(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        realTimeListener.conexionCaida();
                    }
                });
            }
        });

        mHubConnection.connectionSlow(new Runnable() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        realTimeListener.conexionLenta();
                    }
                });
            }
        });

    }

    private void agregarMetodosCliente(HubProxy mHubProxy) {
        String CLIENT_NUEVO_COMENTARIO = "nuevoComentario";
        String CLIENT_ESTA_ESCRIBIENDO = "estaEscribiendo";
        String CLIENT_FIN_ESCRITURA = "finEscritura";

        mHubProxy.on(CLIENT_NUEVO_COMENTARIO, new SubscriptionHandler() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        realTimeListener.nuevoComentario();
                    }
                });
            }
        });

        mHubProxy.on(CLIENT_ESTA_ESCRIBIENDO, new SubscriptionHandler2<String, String>() {
            @Override
            public void run(final String idEmpleado, final String nombreEmpleado) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        realTimeListener.estaEscribiendo(idEmpleado, nombreEmpleado);
                    }
                });
            }
        }, String.class, String.class);


        mHubProxy.on(CLIENT_FIN_ESCRITURA, new SubscriptionHandler1<String>() {
            @Override
            public void run(final String idEmpleado) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        realTimeListener.finEscritura(idEmpleado);
                    }
                });
            }
        }, String.class);
    }


    private void desconectarSignalR() {
        mHubConnection.stop();
    }
}
