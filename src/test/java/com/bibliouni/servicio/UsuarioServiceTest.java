package com.bibliouni.servicio;

import com.bibliouni.modelo.Usuario;
import com.bibliouni.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de UsuarioService. La validación del correo se hizo con TDD.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    // CP-019
    @Test
    void registrarCuandoCorreoEsValidoEntoncesGuardaElUsuario() {
        // Arrange
        Usuario laura = new Usuario("Laura Gómez", "laura@uniempresarial.edu.co", "ESTUDIANTE");
        when(usuarioRepository.save(laura)).thenReturn(laura);

        // Act
        Usuario guardado = usuarioService.registrar(laura);

        // Assert
        assertNotNull(guardado);
        verify(usuarioRepository).save(laura);
    }

    // CP-020 (partición de equivalencia: una prueba por cada clase inválida)
    @ParameterizedTest(name = "correo inválido: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"laura.gmail.com", "@gmail.com", "laura@"})
    void registrarCuandoCorreoEsInvalidoEntoncesLanzaReglaNegocio(String correo) {
        // Arrange
        Usuario usuario = new Usuario("Laura Gómez", correo, "ESTUDIANTE");

        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> usuarioService.registrar(usuario));

        // Assert
        assertEquals("El correo no es válido", error.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // CP-021 (camino triste)
    @Test
    void registrarCuandoCorreoYaExisteEntoncesLanzaReglaNegocio() {
        // Arrange
        Usuario repetido = new Usuario("Laura G.", "Laura@Uniempresarial.edu.co", "ESTUDIANTE");
        when(usuarioRepository.existsByCorreoIgnoreCase("Laura@Uniempresarial.edu.co")).thenReturn(true);

        // Act
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> usuarioService.registrar(repetido));

        // Assert
        assertEquals("Ya existe un usuario con ese correo", error.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
