# BiblioUni

Sistema de préstamos para la biblioteca de la universidad, hecho con **Spring Boot**. Funciona por medio de una API REST.

## Funcionalidades

- Registrar y buscar materiales (libros, revistas, tesis).
- Registrar usuarios (estudiantes y docentes).
- Prestar y devolver materiales (máximo 3 préstamos activos por usuario, 8 días de préstamo).
- **Gancho innovador – Lista de espera inteligente:** si un material está prestado, el estudiante se anota en una fila. Cuando lo devuelven, queda apartado 48 horas para el primero de la fila. Si no lo recoge, pasa al siguiente.

## Requisitos

- Java 17 o superior
- Maven 3.8 o superior

## Cómo ejecutar

```bash
# Ejecutar la aplicación (queda en http://localhost:8080)
mvn spring-boot:run

# Ejecutar las pruebas
mvn test
```

Después de `mvn test`:
- Los resultados de las pruebas quedan en `target/surefire-reports/`.
- El reporte de cobertura (JaCoCo) queda en `target/site/jacoco/index.html`.

## Endpoints principales

| Método | URL | Qué hace |
|--------|-----|----------|
| GET | `/api/materiales` | Lista los materiales (se puede filtrar con `?titulo=`) |
| GET | `/api/materiales/disponibles` | Lista solo los disponibles |
| POST | `/api/materiales` | Registra un material |
| POST | `/api/usuarios` | Registra un usuario |
| POST | `/api/prestamos?usuarioId=1&materialId=2` | Presta un material |
| PUT | `/api/prestamos/{id}/devolucion` | Devuelve un material |
| POST | `/api/reservas?usuarioId=1&materialId=2` | Se anota en la lista de espera |
| GET | `/api/reservas/posicion?usuarioId=1&materialId=2` | Consulta la posición en la fila |

## Organización del proyecto

```
src/
├── main/java/com/bibliouni/
│   ├── modelo/        → entidades (Material, Usuario, Prestamo, Reserva)
│   ├── repositorio/   → acceso a la base de datos
│   ├── servicio/      → reglas de la biblioteca
│   └── controlador/   → API REST
└── test/java/com/bibliouni/
    ├── servicio/      → pruebas unitarias de los servicios
    └── controlador/   → pruebas de integración y de API
docs/pruebas/
├── estrategia.md          → estrategia de pruebas
├── calidad_iso25010.md    → evaluación de calidad ISO/IEC 25010
├── escenarios.md          → escenarios de prueba narrativos
└── mapa_conceptual.png    → mapa conceptual de pruebas de software
```

### Estrategia de organización del repositorio de pruebas

- Las pruebas siguen la convención de Maven: todo va en `src/test/java`.
- La carpeta de pruebas tiene **la misma estructura de paquetes** que el código principal, así cada clase tiene su prueba en el mismo lugar (ej.: `servicio/MaterialService` → `servicio/MaterialServiceTest`).
- Las clases de prueba terminan en `Test` para que Maven (Surefire) las encuentre solas.
- La documentación de pruebas va en `docs/pruebas/`.
- Ramas: el trabajo de pruebas se hace en ramas `feature/...` y se une a `develop` por medio de Pull Request, solo si `mvn test` pasa.
