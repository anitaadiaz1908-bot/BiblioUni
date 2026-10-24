package com.bibliouni.repositorio;

import com.bibliouni.modelo.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    long countByUsuarioIdAndActivoTrue(Long usuarioId);

    List<Prestamo> findByUsuarioIdAndActivoTrue(Long usuarioId);
}
