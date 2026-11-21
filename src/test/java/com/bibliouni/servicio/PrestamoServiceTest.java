package com.bibliouni.servicio;

import com.bibliouni.modelo.EstadoMaterial;
import com.bibliouni.modelo.Material;
import com.bibliouni.modelo.Prestamo;
import com.bibliouni.modelo.Usuario;
import com.bibliouni.repositorio.MaterialRepository;
import com.bibliouni.repositorio.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de PrestamoService.
 * Se simulan los repositorios y los otros servicios para probar solo las reglas del préstamo.
 */
@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MaterialService materialService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private PrestamoService prestamoService;

    private Usuario laura;
    private Material libro;

    @BeforeEach
    void setUp() {
        laura = new Usuario("Laura Gómez", "laura@uniempresarial.edu.co", "ESTUDIANTE");
        laura.setId(1L);
        libro = new Material("Cien años de soledad", "Gabriel García Márquez", "LIBRO");
        libro.setId(5L);
        libro.setEstado(EstadoMaterial.DISPONIBLE);
    }

    // CP-006
    @Test
    void prestarCuandoMaterialDisponibleEntoncesCreaPrestamoActivo() {
        // Arrange
        when(usuarioService.buscarPorId(1L)).thenReturn(laura);
        when(materialService.buscarPorId(5L)).thenReturn(libro);
        when(prestamoRepository.countByUsuarioIdAndActivoTrue(1L)).thenReturn(0L);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Prestamo prestamo = prestamoService.prestar(1L, 5L);

        // Assert
        assertNotNull(prestamo);
        assertTrue(prestamo.isActivo());
        assertEquals(laura, prestamo.getUsuario());
        assertEquals(LocalDate.now(), prestamo.getFechaPrestamo());
        assertEquals(LocalDate.now().plusDays(8), prestamo.getFechaLimite());

        ArgumentCaptor<Material> materialGuardado = ArgumentCaptor.forClass(Material.class);
        verify(materialRepository).save(materialGuardado.capture());
        assertEquals(EstadoMaterial.PRESTADO, materialGuardado.getValue().getEstado());
    }

    // CP-007 (camino triste)
    @Test
    void prestarCuandoMaterialYaEstaPrestadoEntoncesLanzaReglaNegocio() {
        // Arrange
        libro.setEstado(EstadoMaterial.PRESTADO);
        when(usuarioService.buscarPorId(1L)).thenReturn(laura);
        when(materialService.buscarPorId(5L)).thenReturn(libro);
        when(prestamoRepository.countByUsuarioIdAndActivoTrue(1L)).thenReturn(0L);

        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> prestamoService.prestar(1L, 5L));

        // Assert
        assertEquals("El material no está disponible", error.getMessage());
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    // CP-008 y CP-009 (valores límite: 0 = mínimo, 2 = máximo permitido)
    @ParameterizedTest(name = "con {0} préstamos activos sí puede prestar")
    @ValueSource(longs = {0, 1, 2})
    void prestarCuandoTieneMenosDeTresPrestamosEntoncesPermitePrestar(long prestamosActivos) {
        // Arrange
        when(usuarioService.buscarPorId(1L)).thenReturn(laura);
        when(materialService.buscarPorId(5L)).thenReturn(libro);
        when(prestamoRepository.countByUsuarioIdAndActivoTrue(1L)).thenReturn(prestamosActivos);
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Prestamo prestamo = prestamoService.prestar(1L, 5L);

        // Assert
        assertNotNull(prestamo);
        assertEquals(EstadoMaterial.PRESTADO, libro.getEstado());
    }

    // CP-010 y CP-011 (valores límite: 3 = máximo + 1, y 4)
    @ParameterizedTest(name = "con {0} préstamos activos se rechaza")
    @ValueSource(longs = {3, 4})
    void prestarCuandoTieneTresOMasPrestamosEntoncesLanzaReglaNegocio(long prestamosActivos) {
        // Arrange
        when(usuarioService.buscarPorId(1L)).thenReturn(laura);
        when(materialService.buscarPorId(5L)).thenReturn(libro);
        when(prestamoRepository.countByUsuarioIdAndActivoTrue(1L)).thenReturn(prestamosActivos);

        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> prestamoService.prestar(1L, 5L));

        // Assert
        assertEquals("El usuario ya tiene 3 préstamos activos", error.getMessage());
        assertEquals(EstadoMaterial.DISPONIBLE, libro.getEstado());
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    // CP-012 (camino triste)
    @Test
    void prestarCuandoUsuarioNoExisteEntoncesLanzaRecursoNoEncontrado() {
        // Arrange
        when(usuarioService.buscarPorId(999L))
                .thenThrow(new RecursoNoEncontradoException("No existe el usuario con id 999"));

        // Act + Assert
        assertThrows(RecursoNoEncontradoException.class, () -> prestamoService.prestar(999L, 5L));
        verify(materialService, never()).buscarPorId(anyLong());
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    // CP-018 (camino triste)
    @Test
    void devolverCuandoYaFueDevueltoEntoncesLanzaReglaNegocio() {
        // Arrange
        Prestamo prestamo = new Prestamo(laura, libro, LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));
        prestamo.setId(10L);
        prestamo.setActivo(false);
        when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));

        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> prestamoService.devolver(10L));

        // Assert
        assertEquals("Este préstamo ya fue devuelto", error.getMessage());
        verify(reservaService, never()).asignarAlSiguiente(any(Material.class));
        verify(prestamoRepository, never()).save(any(Prestamo.class));
    }

    // ---------- Multa por devolución tardía (TDD) ----------

    // CP-013, CP-014, CP-015 (valores límite) y CP-016 (partición: con retraso)
    @ParameterizedTest(name = "devuelto el {0} con límite 2026-11-10 -> multa de {1}")
    @CsvSource({
            "2026-11-09, 0",      // 1 día antes del límite
            "2026-11-10, 0",      // el mismo día límite
            "2026-11-11, 1000",   // 1 día después
            "2026-11-20, 10000"   // 10 días de retraso
    })
    void calcularMultaSegunLaFechaDeDevolucion(LocalDate fechaDevolucion, long multaEsperada) {
        // Arrange
        Prestamo prestamo = new Prestamo(laura, libro, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 10));

        // Act
        long multa = prestamoService.calcularMulta(prestamo, fechaDevolucion);

        // Assert
        assertEquals(multaEsperada, multa);
    }
}
