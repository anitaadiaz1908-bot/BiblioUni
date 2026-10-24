package com.bibliouni.controlador;

import com.bibliouni.modelo.Reserva;
import com.bibliouni.servicio.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Endpoints del gancho innovador: lista de espera inteligente. */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    // Ejemplo: POST /api/reservas?usuarioId=1&materialId=2
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva anotarse(@RequestParam Long usuarioId, @RequestParam Long materialId) {
        return reservaService.anotarseEnLista(usuarioId, materialId);
    }

    @GetMapping("/posicion")
    public Map<String, Integer> posicion(@RequestParam Long usuarioId, @RequestParam Long materialId) {
        return Map.of("posicion", reservaService.consultarPosicion(usuarioId, materialId));
    }
}
