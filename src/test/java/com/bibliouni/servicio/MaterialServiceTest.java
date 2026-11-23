package com.bibliouni.servicio;

import com.bibliouni.modelo.EstadoMaterial;
import com.bibliouni.modelo.Material;
import com.bibliouni.repositorio.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de MaterialService.
 * El repositorio se simula con Mockito, así no se usa la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private MaterialService materialService;

    private Material libro;

    @BeforeEach
    void setUp() {
        libro = new Material("Cien años de soledad", "Gabriel García Márquez", "LIBRO");
    }

    // CP-001
    @Test
    void registrarCuandoDatosValidosEntoncesQuedaDisponibleYSeGuarda() {
        // Arrange
        libro.setEstado(EstadoMaterial.PRESTADO); // aunque venga con otro estado
        when(materialRepository.save(any(Material.class))).thenAnswer(inv -> {
            Material m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        // Act
        Material guardado = materialService.registrar(libro);

        // Assert
        assertNotNull(guardado.getId());
        assertEquals(EstadoMaterial.DISPONIBLE, guardado.getEstado());
        assertEquals("Cien años de soledad", guardado.getTitulo());
        verify(materialRepository).save(libro);
    }

    // CP-002
    @Test
    void buscarPorIdCuandoExisteEntoncesRetornaElMaterial() {
        // Arrange
        libro.setId(5L);
        when(materialRepository.findById(5L)).thenReturn(Optional.of(libro));

        // Act
        Material encontrado = materialService.buscarPorId(5L);

        // Assert
        assertNotNull(encontrado);
        assertEquals(5L, encontrado.getId());
        assertEquals("Gabriel García Márquez", encontrado.getAutor());
        verify(materialRepository, times(1)).findById(5L);
    }

    // CP-003 (camino triste)
    @Test
    void buscarPorIdCuandoNoExisteEntoncesLanzaRecursoNoEncontrado() {
        // Arrange
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> materialService.buscarPorId(999L));

        // Assert
        assertEquals("No existe el material con id 999", error.getMessage());
    }

    // CP-004
    @Test
    void buscarPorTituloCuandoSeEscribeEnMinusculaEntoncesEncuentraElMaterial() {
        // Arrange
        Material calculo = new Material("Cálculo Diferencial", "James Stewart", "LIBRO");
        when(materialRepository.findByTituloContainingIgnoreCase("cálculo")).thenReturn(List.of(calculo));

        // Act
        List<Material> resultado = materialService.buscarPorTitulo("cálculo");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Cálculo Diferencial", resultado.get(0).getTitulo());
    }

    // CP-005 (camino triste)
    @Test
    void eliminarCuandoElMaterialEstaPrestadoEntoncesLanzaReglaNegocio() {
        // Arrange
        libro.setId(5L);
        libro.setEstado(EstadoMaterial.PRESTADO);
        when(materialRepository.findById(5L)).thenReturn(Optional.of(libro));

        // Act + Assert
        assertThrows(ReglaNegocioException.class, () -> materialService.eliminar(5L));
        verify(materialRepository, never()).delete(any(Material.class));
    }
}
