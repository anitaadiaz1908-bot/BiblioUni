package com.bibliouni.servicio;

import com.bibliouni.modelo.Usuario;
import com.bibliouni.repositorio.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    // texto + @ + dominio con al menos un punto (ej. laura@uniempresarial.edu.co)
    private static final Pattern FORMATO_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    public Usuario registrar(Usuario usuario) {
        validarCorreo(usuario.getCorreo());
        return usuarioRepository.save(usuario);
    }

    /** Revisa que el correo tenga buen formato y que no lo tenga otro usuario. */
    private void validarCorreo(String correo) {
        if (!esCorreoValido(correo)) {
            throw new ReglaNegocioException("El correo no es válido");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese correo");
        }
    }

    private boolean esCorreoValido(String correo) {
        return correo != null && FORMATO_CORREO.matcher(correo).matches();
    }
}
