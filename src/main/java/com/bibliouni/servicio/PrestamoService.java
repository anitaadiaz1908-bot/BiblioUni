package com.bibliouni.servicio;

import com.bibliouni.modelo.*;
import com.bibliouni.repositorio.MaterialRepository;
import com.bibliouni.repositorio.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    public static final int MAX_PRESTAMOS_ACTIVOS = 3;
    public static final int DIAS_PRESTAMO = 8;

    private final PrestamoRepository prestamoRepository;
    private final MaterialRepository materialRepository;
    private final MaterialService materialService;
    private final UsuarioService usuarioService;
    private final ReservaService reservaService;

    public PrestamoService(PrestamoRepository prestamoRepository, MaterialRepository materialRepository,
                           MaterialService materialService, UsuarioService usuarioService,
                           ReservaService reservaService) {
        this.prestamoRepository = prestamoRepository;
        this.materialRepository = materialRepository;
        this.materialService = materialService;
        this.usuarioService = usuarioService;
        this.reservaService = reservaService;
    }

    @Transactional
    public Prestamo prestar(Long usuarioId, Long materialId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Material material = materialService.buscarPorId(materialId);

        if (prestamoRepository.countByUsuarioIdAndActivoTrue(usuarioId) >= MAX_PRESTAMOS_ACTIVOS) {
            throw new ReglaNegocioException("El usuario ya tiene " + MAX_PRESTAMOS_ACTIVOS + " préstamos activos");
        }

        if (material.getEstado() == EstadoMaterial.PRESTADO) {
            throw new ReglaNegocioException("El material no está disponible");
        }

        // Gancho innovador: si está apartado, solo lo puede llevar a quien le tocó el turno
        if (material.getEstado() == EstadoMaterial.APARTADO) {
            Optional<Reserva> apartado = reservaService.buscarApartadoDe(materialId);
            if (apartado.isEmpty() || !apartado.get().getUsuario().getId().equals(usuarioId)) {
                throw new ReglaNegocioException("El material está apartado para otro usuario de la lista de espera");
            }
            reservaService.marcarCumplida(apartado.get());
        }

        material.setEstado(EstadoMaterial.PRESTADO);
        materialRepository.save(material);

        LocalDate hoy = LocalDate.now();
        return prestamoRepository.save(new Prestamo(usuario, material, hoy, hoy.plusDays(DIAS_PRESTAMO)));
    }

    @Transactional
    public Prestamo devolver(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el préstamo con id " + prestamoId));

        if (!prestamo.isActivo()) {
            throw new ReglaNegocioException("Este préstamo ya fue devuelto");
        }

        prestamo.setActivo(false);
        prestamo.setFechaDevolucion(LocalDate.now());

        // Gancho innovador: se aparta para el siguiente de la fila (o queda disponible)
        reservaService.asignarAlSiguiente(prestamo.getMaterial());

        return prestamoRepository.save(prestamo);
    }

    public List<Prestamo> prestamosActivosDe(Long usuarioId) {
        usuarioService.buscarPorId(usuarioId);
        return prestamoRepository.findByUsuarioIdAndActivoTrue(usuarioId);
    }
}
