package com.bibliouni.repositorio;

import com.bibliouni.modelo.EstadoMaterial;
import com.bibliouni.modelo.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByEstado(EstadoMaterial estado);

    List<Material> findByTituloContainingIgnoreCase(String titulo);
}
