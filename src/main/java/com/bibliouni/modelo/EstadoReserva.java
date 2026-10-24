package com.bibliouni.modelo;

public enum EstadoReserva {
    EN_ESPERA,   // está en la fila
    NOTIFICADA,  // ya le tocó el turno, tiene 48 horas para recogerlo
    CUMPLIDA,    // recogió el material
    VENCIDA,     // no lo recogió a tiempo
    CANCELADA
}
