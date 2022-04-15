package com.project.chatapp;

/**
 * Created by gcoronad on 26/07/2018.
 */

public interface RealTimeListener {
    void estaEscribiendo(String idEmpleado, String nombreCompleto);
    void nuevoComentario();
    void finEscritura(String idEmpleado);

    void conexionExitosa();
    void conexionLenta();
    void conexionCaida();
}
