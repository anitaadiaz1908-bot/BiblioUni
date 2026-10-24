package com.bibliouni.servicio;

import com.bibliouni.modelo.EstadoMaterial;
import com.bibliouni.modelo.Material;
import com.bibliouni.repositorio.MaterialRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> listarTodos() {
        return materialRepository.findAll();
    }

    public List<Material> listarDisponibles() {
        return materialRepository.findByEstado(EstadoMaterial.DISPONIBLE);
    }

    public List<Material> buscarPorTitulo(String titulo) {
        return materialRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public Material buscarPorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el material con id " + id));
    }

    public Material registrar(Material material) {
        material.setEstado(EstadoMaterial.DISPONIBLE);
        return materialRepository.save(material);
    }

    public void eliminar(Long id) {
        Material material = buscarPorId(id);
        if (material.getEstado() != EstadoMaterial.DISPONIBLE) {
            throw new ReglaNegocioException("No se puede eliminar un material prestado o apartado");
        }
        materialRepository.delete(material);
    }
}
