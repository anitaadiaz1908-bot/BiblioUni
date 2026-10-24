package com.bibliouni.servicio;

/** Se lanza cuando no existe el material, usuario o préstamo buscado (HTTP 404). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
