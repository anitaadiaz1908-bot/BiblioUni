package com.bibliouni.controlador;

import com.bibliouni.modelo.Material;
import com.bibliouni.servicio.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public List<Material> listar(@RequestParam(required = false) String titulo) {
        if (titulo != null) {
            return materialService.buscarPorTitulo(titulo);
        }
        return materialService.listarTodos();
    }

    @GetMapping("/disponibles")
    public List<Material> disponibles() {
        return materialService.listarDisponibles();
    }

    @GetMapping("/{id}")
    public Material buscar(@PathVariable Long id) {
        return materialService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Material crear(@Valid @RequestBody Material material) {
        return materialService.registrar(material);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        materialService.eliminar(id);
    }
}
