package com.bibliouni.servicio;

/** Se lanza cuando no se cumple una regla de la biblioteca (HTTP 400). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
