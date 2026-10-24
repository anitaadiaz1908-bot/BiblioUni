package com.bibliouni.controlador;

import com.bibliouni.modelo.Prestamo;
import com.bibliouni.servicio.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    // Ejemplo: POST /api/prestamos?usuarioId=1&materialId=2
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo prestar(@RequestParam Long usuarioId, @RequestParam Long materialId) {
        return prestamoService.prestar(usuarioId, materialId);
    }

    @PutMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable Long id) {
        return prestamoService.devolver(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Prestamo> activosDeUsuario(@PathVariable Long usuarioId) {
        return prestamoService.prestamosActivosDe(usuarioId);
    }
}
