package com.project.chatapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements RealTimeListener {

    private TextView txtEstatusConexion, txtMensajesEscritura, txtMensajesNuevos;
    private EditText etIdSolicitud, etIdEmpleado;
    private Button btnEstaEscribiendoAlguien, btnNuevoComentario, btnEscritura, btnFinEscritura, btnConectar, btnDesconectar;
    private boolean mBound = false;
    private ForoCreditoEnTiempoRealService.LocalBinder localBinder;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connection = new Connection(this);

        txtEstatusConexion = findViewById(R.id.txtEstatusConexion);
        txtMensajesEscritura = findViewById(R.id.txtMensajesEscritura);
        txtMensajesNuevos = findViewById(R.id.txtMensajesNuevos);

        etIdSolicitud = findViewById(R.id.etIdSolicitud);
        etIdEmpleado = findViewById(R.id.etIdEmpleado);

        btnEstaEscribiendoAlguien = findViewById(R.id.btnEstaEscribiendoAlguien);
        btnNuevoComentario = findViewById(R.id.btnNuevoComentario);
        btnEscritura = findViewById(R.id.btnEscritura);
        btnFinEscritura = findViewById(R.id.btnFinEscritura);

        btnConectar = findViewById(R.id.btnConectar);
        btnDesconectar = findViewById(R.id.btnDesconectar);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ForoCreditoEnTiempoRealService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStart() {
        super.onStart();

        btnEstaEscribiendoAlguien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                estaEscribiendoAlguien();
            }
        });
        btnNuevoComentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificarNuevoComentario();
            }
        });
        btnEscritura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificarEscritura();
            }
        });
        btnFinEscritura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificarFinEscritura();
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

    @Override
    protected void onStop() {
        super.onStop();

        btnEstaEscribiendoAlguien.setOnClickListener(null);
        btnNuevoComentario.setOnClickListener(null);
        btnEscritura.setOnClickListener(null);
        btnFinEscritura.setOnClickListener(null);
        btnConectar.setOnClickListener(null);
        btnDesconectar.setOnClickListener(null);

        desconectar();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        conectar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    private void estaEscribiendoAlguien() {
        localBinder.estaEscribiendoAlguien();
    }

    private void notificarNuevoComentario() {
        localBinder.notificarNuevoComentario();
    }

    private void notificarEscritura() {
        localBinder.notificarEscritura();
    }

    private void notificarFinEscritura() {
        localBinder.notificarFinEscritura();
    }

    private void conectar() {
        if (!connection.estaConectado()) {
            Toast.makeText(this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String idEmpleado = etIdEmpleado.getText().toString();
        String idSolicitud = etIdSolicitud.getText().toString();

        if (idEmpleado.trim().equals("") || idSolicitud.trim().equals(""))
            return;

        etIdEmpleado.setEnabled(false);
        etIdSolicitud.setEnabled(false);

        txtEstatusConexion.setTextColor(getColor(R.color.azul));
        txtEstatusConexion.setText("Conectando...");
        localBinder.conectar(idSolicitud, idEmpleado, "accessToken", this);
    }

    private void desconectar() {
        etIdEmpleado.setEnabled(true);
        etIdSolicitud.setEnabled(true);
        localBinder.desconectar();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            localBinder = (ForoCreditoEnTiempoRealService.LocalBinder) iBinder;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    private class UsuarioEscritor {
        public String IdEmpleado;
        public String NombreCompleto;

        public UsuarioEscritor(String IdEmpleado, String NombreCompleto) {
            this.IdEmpleado = IdEmpleado;
            this.NombreCompleto = NombreCompleto;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UsuarioEscritor) {
                UsuarioEscritor usuarioEscritor = (UsuarioEscritor) obj;

                return usuarioEscritor.IdEmpleado.equals(this.IdEmpleado);
            }
            return false;
        }
    }

    List<UsuarioEscritor> listUsuariosEscritores = new ArrayList<>();

    private void mostrarEscritores() {
        if (listUsuariosEscritores.size() <= 0) {
            txtMensajesEscritura.setText("Nadie está escribiendo");
            return;
        }

        String mensaje = "";
        for (UsuarioEscritor usuarioEscritor : listUsuariosEscritores) {
            mensaje += usuarioEscritor.NombreCompleto + " ";
        }

        if (listUsuariosEscritores.size() > 1)
            mensaje += " está escribiendo...";
        else
            mensaje += " están escribiendo...";

        txtMensajesEscritura.setText(mensaje);
    }

    @Override
    public void estaEscribiendo(String idEmpleado, String nombreCompleto) {
        UsuarioEscritor usuarioEscritor = new UsuarioEscritor(idEmpleado, nombreCompleto);
        if (!listUsuariosEscritores.contains(usuarioEscritor))
            listUsuariosEscritores.add(usuarioEscritor);

        mostrarEscritores();
    }

    @Override
    public void finEscritura(String idEmpleado) {
        listUsuariosEscritores.remove(new UsuarioEscritor(idEmpleado, ""));
        mostrarEscritores();
    }

    private int countMensajes = 0;

    @Override
    public void nuevoComentario() {
        countMensajes++;
        String comentarios = String.format("Mensajes nuevos: %d", countMensajes);
        txtMensajesNuevos.setText(comentarios);
    }


    @Override
    public void conexionExitosa() {
        txtEstatusConexion.setText("Conectado");
        txtEstatusConexion.setTextColor(getColor(R.color.verde));
    }

    @Override
    public void conexionLenta() {

    }

    @Override
    public void conexionCaida() {
        txtEstatusConexion.setText("Desconectado");
        txtEstatusConexion.setTextColor(getColor(R.color.rojo));

        //conectar();
    }
}
