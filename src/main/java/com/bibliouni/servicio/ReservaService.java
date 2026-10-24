package com.bibliouni.servicio;

import com.bibliouni.modelo.*;
import com.bibliouni.repositorio.MaterialRepository;
import com.bibliouni.repositorio.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * GANCHO INNOVADOR: Lista de espera inteligente.
 */
@Service
public class ReservaService {

    public static final int HORAS_APARTADO = 48;

    private final ReservaRepository reservaRepository;
    private final MaterialRepository materialRepository;
    private final MaterialService materialService;
    private final UsuarioService usuarioService;

    public ReservaService(ReservaRepository reservaRepository, MaterialRepository materialRepository,
                          MaterialService materialService, UsuarioService usuarioService) {
        this.reservaRepository = reservaRepository;
        this.materialRepository = materialRepository;
        this.materialService = materialService;
        this.usuarioService = usuarioService;
    }

    /** El estudiante se anota en la fila de un material que no está disponible. */
    public Reserva anotarseEnLista(Long usuarioId, Long materialId) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        Material material = materialService.buscarPorId(materialId);

        if (material.getEstado() == EstadoMaterial.DISPONIBLE) {
            throw new ReglaNegocioException("El material está disponible, puede pedirlo prestado directamente");
        }
        if (reservaRepository.existsByMaterialIdAndUsuarioIdAndEstado(materialId, usuarioId, EstadoReserva.EN_ESPERA)) {
            throw new ReglaNegocioException("El usuario ya está en la lista de espera de este material");
        }
        return reservaRepository.save(new Reserva(usuario, material, LocalDateTime.now()));
    }

    /** Posición del usuario en la fila (1 = primero). Devuelve 0 si no está. */
    public int consultarPosicion(Long usuarioId, Long materialId) {
        List<Reserva> fila = reservaRepository
                .findByMaterialIdAndEstadoOrderByFechaReservaAsc(materialId, EstadoReserva.EN_ESPERA);
        for (int i = 0; i < fila.size(); i++) {
            if (fila.get(i).getUsuario().getId().equals(usuarioId)) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * Se llama cuando devuelven el material: se aparta para el primero de la fila.
     * Si no hay nadie esperando, el material queda DISPONIBLE.
     */
    @Transactional
    public Optional<Reserva> asignarAlSiguiente(Material material) {
        List<Reserva> fila = reservaRepository
                .findByMaterialIdAndEstadoOrderByFechaReservaAsc(material.getId(), EstadoReserva.EN_ESPERA);

        if (fila.isEmpty()) {
            material.setEstado(EstadoMaterial.DISPONIBLE);
            materialRepository.save(material);
            return Optional.empty();
        }

        Reserva siguiente = fila.get(0);
        siguiente.setEstado(EstadoReserva.NOTIFICADA);
        siguiente.setVenceApartado(LocalDateTime.now().plusHours(HORAS_APARTADO));
        material.setEstado(EstadoMaterial.APARTADO);
        materialRepository.save(material);
        // Aquí se enviaría la notificación (correo) al estudiante
        return Optional.of(reservaRepository.save(siguiente));
    }

    /** Si el estudiante no recogió el material en 48 horas, se pasa al siguiente. */
    @Transactional
    public void vencerApartado(Long materialId, LocalDateTime ahora) {
        Optional<Reserva> notificada = reservaRepository
                .findFirstByMaterialIdAndEstado(materialId, EstadoReserva.NOTIFICADA);

        if (notificada.isPresent() && notificada.get().getVenceApartado().isBefore(ahora)) {
            Reserva reserva = notificada.get();
            reserva.setEstado(EstadoReserva.VENCIDA);
            reservaRepository.save(reserva);
            asignarAlSiguiente(reserva.getMaterial());
        }
    }

    public Optional<Reserva> buscarApartadoDe(Long materialId) {
        return reservaRepository.findFirstByMaterialIdAndEstado(materialId, EstadoReserva.NOTIFICADA);
    }

    public Reserva marcarCumplida(Reserva reserva) {
        reserva.setEstado(EstadoReserva.CUMPLIDA);
        return reservaRepository.save(reserva);
    }
}
