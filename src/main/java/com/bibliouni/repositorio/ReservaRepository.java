package com.bibliouni.repositorio;

import com.bibliouni.modelo.EstadoReserva;
import com.bibliouni.modelo.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // La fila de espera de un material, en orden de llegada
    List<Reserva> findByMaterialIdAndEstadoOrderByFechaReservaAsc(Long materialId, EstadoReserva estado);

    boolean existsByMaterialIdAndUsuarioIdAndEstado(Long materialId, Long usuarioId, EstadoReserva estado);

    Optional<Reserva> findFirstByMaterialIdAndEstado(Long materialId, EstadoReserva estado);
}
