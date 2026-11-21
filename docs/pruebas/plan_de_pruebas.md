# Plan de pruebas – BiblioUni (basado en IEEE 829)

## 1. Identificación del plan

| Campo | Valor |
|-------|-------|
| **Identificador** | PP-BIBLIOUNI-001 |
| **Nombre** | Plan de pruebas del sistema de préstamos BiblioUni |
| **Versión** | 1.1 |
| **Fecha** | noviembre de 2026 |
| **Responsable** | Ana María Díaz Parra |
| **Asignatura** | Pruebas de software – Unidad 2 |
| **Documentos relacionados** | `estrategia.md`, `escenarios.md` y `calidad_iso25010.md` (Unidad 1), `casos_de_prueba.md` (Unidad 2) |

---

## 2. Introducción

Este plan describe cómo se van a probar los servicios de BiblioUni: qué se prueba, qué no, con qué herramientas, cuándo se pueden empezar las pruebas y cuándo se consideran terminadas. Sigue la estructura de la norma IEEE 829 y toma como base la estrategia de pruebas de la Unidad 1.

El foco de esta unidad está en la **capa de servicios** (`MaterialService`, `PrestamoService` y `UsuarioService`), que es donde están las reglas de la biblioteca. En la Unidad 3 se va a ampliar a pruebas de integración y de API.

---

## 3. Ítems a probar

| Ítem | Clase / archivo | Versión |
|------|-----------------|---------|
| Servicio de materiales | `servicio/MaterialService.java` | rama `feature/plan-tests` |
| Servicio de préstamos | `servicio/PrestamoService.java` | rama `feature/plan-tests` |
| Servicio de usuarios | `servicio/UsuarioService.java` | rama `feature/plan-tests` |
| Entidad préstamo (campo multa) | `modelo/Prestamo.java` | rama `feature/plan-tests` |
| Excepciones de negocio | `RecursoNoEncontradoException`, `ReglaNegocioException` | rama `feature/plan-tests` |

---

## 4. Alcance

### 4.1 Características que SÍ se prueban

| ID | Funcionalidad | HU | Nivel |
|----|---------------|----|-------|
| F-01 | Registrar material (queda DISPONIBLE) | HU-01 | Unitaria |
| F-02 | Buscar material por id y por título | HU-02 | Unitaria |
| F-03 | Eliminar material (solo si está disponible) | HU-01 | Unitaria |
| F-04 | Registrar préstamo (material disponible, máximo 3 préstamos activos) | HU-04 | Unitaria |
| F-05 | Devolver préstamo | HU-05 | Unitaria |
| F-06 | **Calcular multa por devolución tardía** ($1.000 por día de retraso) – *nueva, hecha con TDD* | HU-05 | Unitaria |
| F-07 | **Validar el correo al registrar un usuario** (formato y que no esté repetido) – *nueva, hecha con TDD* | HU-03 | Unitaria |
| F-08 | Préstamo de un material APARTADO (gancho innovador) | HU-07 | Diseñado en casos; se automatiza en U3 |

### 4.2 Características que NO se prueban en esta unidad

| Qué no se prueba | Por qué |
|------------------|---------|
| Controladores REST y códigos HTTP | Se dejan para la Unidad 3 (pruebas de API con MockMvc y Postman). Solo se mantiene la prueba básica `MaterialControllerTest`. |
| `ReservaService` completo (lista de espera) | Ya tiene escenarios diseñados en la U1; su automatización es la siguiente prioridad (U3). |
| Repositorios de Spring Data | Son código de Spring; en estas pruebas se simulan con Mockito. |
| Rendimiento, carga y seguridad avanzada | Fuera del alcance del proyecto académico (ver estrategia U1, sección 5). |
| Envío real de correos | La aplicación no envía correos todavía. |

---

## 5. Enfoque de pruebas

- **Pruebas unitarias (principal en esta unidad):** JUnit 5 + Mockito. Se usa `@ExtendWith(MockitoExtension.class)`, `@Mock` para los repositorios y servicios de los que se depende, e `@InjectMocks` para el servicio que se prueba. Así se prueba solo la lógica, sin base de datos.
- **Patrón AAA:** cada prueba está dividida en *Arrange* (preparar datos y simulaciones), *Act* (llamar al método) y *Assert* (verificar el resultado).
- **Nombres en español** con la forma `metodoCuandoCondicionEntoncesResultado`.
- **Técnicas de diseño (caja negra):**
  - *Partición de equivalencia*: se divide cada dato de entrada en clases válidas e inválidas (ej. correo válido / sin @ / vacío).
  - *Valores límite*: se prueban los extremos (ej. 2, 3 y 4 préstamos activos; devolver 1 día antes, el mismo día y 1 día después de la fecha límite).
- **TDD (Red – Green – Refactor):** las funcionalidades nuevas F-06 y F-07 se desarrollan escribiendo primero la prueba que falla.
- **Pruebas de integración y API:** se planean para la Unidad 3 con `@SpringBootTest`, MockMvc y Postman sobre H2.
- **Regresión:** antes de cada commit se ejecuta `mvn test` completo.

---

## 6. Criterios de entrada (para empezar a probar)

| # | Criterio | Cómo se verifica |
|---|----------|------------------|
| CE-1 | El proyecto compila sin errores. | `mvn compile` termina en BUILD SUCCESS. |
| CE-2 | Las pruebas que ya existían pasan. | `mvn test` en verde antes de empezar. |
| CE-3 | La aplicación arranca y los endpoints responden. | `mvn spring-boot:run` y prueba manual de `GET /api/materiales`. |
| CE-4 | Las reglas de negocio están definidas. | Historias de usuario HU-01 a HU-07 y regla de multa documentadas. |
| CE-5 | Los casos de prueba están diseñados. | `casos_de_prueba.md` con mínimo 15 casos. |

---

## 7. Criterios de salida (para dar las pruebas por terminadas)

| # | Criterio | Meta |
|---|----------|------|
| CS-1 | Casos de prueba ejecutados | 100 % de los casos automatizables de esta unidad |
| CS-2 | Pruebas que pasan en `mvn test` | 100 % (0 fallos, 0 errores) |
| CS-3 | Cobertura de líneas de todo el proyecto (JaCoCo) | > 60 % |
| CS-4 | Cobertura de líneas del paquete `servicio` | ≥ 80 % en `MaterialService`, `PrestamoService` y `UsuarioService` |
| CS-5 | Defectos críticos o altos abiertos | 0 |
| CS-6 | Densidad de defectos aceptable | Máximo 1 defecto medio abierto por cada servicio probado |
| CS-7 | Funcionalidades con TDD | 2 (con commits *red*, *green* y *refactor*) |

### 7.1 Criterios de aprobación / fallo de cada caso

- **Aprueba:** el resultado obtenido es igual al resultado esperado del caso (valor devuelto, excepción y mensaje, y llamadas al repositorio).
- **Falla:** cualquier diferencia, una excepción no esperada o que se guarde algo que no se debía guardar.

### 7.2 Criterios de suspensión y reanudación

- Se **suspenden** las pruebas si el proyecto no compila o si falla más del 30 % de las pruebas por un problema del entorno (por ejemplo, dependencias de Maven que no descargan).
- Se **reanudan** cuando se corrige el problema y `mvn test` vuelve a ejecutar todas las pruebas.

### 7.3 Clasificación de defectos

| Severidad | Ejemplo en BiblioUni |
|-----------|----------------------|
| Crítico | Se presta un material que ya está prestado o se pasa el límite de 3 préstamos. |
| Alto | La multa se calcula mal o se guarda un usuario con correo inválido. |
| Medio | Mensaje de error incorrecto o poco claro. |
| Bajo | Detalles de nombres o textos. |

---

## 8. Entregables de las pruebas

- `docs/pruebas/plan_de_pruebas.md` (este documento).
- `docs/pruebas/casos_de_prueba.md` (22 casos de prueba formales).
- `src/test/java/com/bibliouni/servicio/MaterialServiceTest.java`
- `src/test/java/com/bibliouni/servicio/PrestamoServiceTest.java`
- `src/test/java/com/bibliouni/servicio/UsuarioServiceTest.java`
- Historial de commits de la rama `feature/plan-tests` con las fases *red*, *green* y *refactor* de TDD.
- Workflow de integración continua `.github/workflows/ci.yml` que ejecuta `mvn test` en cada push y Pull Request.
- Reporte de resultados de Surefire (`target/surefire-reports/`) y de cobertura JaCoCo (`target/site/jacoco/index.html`).
- Captura de `mvn test` en verde y Pull Request hacia `develop`.

---

## 9. Recursos

### 9.1 Recursos técnicos

| Recurso | Uso |
|---------|-----|
| Java 17 y Maven 3.8+ | Compilar y ejecutar las pruebas (`mvn test`). |
| Spring Boot 3.3.5 | Framework de la aplicación. |
| JUnit 5 | Escribir y ejecutar las pruebas unitarias. |
| Mockito | Simular repositorios y servicios (`@Mock`, `@InjectMocks`). |
| JaCoCo 0.8.12 | Medir la cobertura de código. |
| Postman | Pruebas manuales de la API (U3). |
| H2 (en memoria) | Base de datos del entorno de pruebas. |
| IntelliJ IDEA | Editor y ejecución de pruebas. |
| GitHub y GitHub Actions | Repositorio, ramas, Pull Request e integración continua (`mvn test` automático). |

### 9.2 Recursos humanos

| Persona | Rol | Dedicación estimada |
|---------|-----|---------------------|
| Ana María Díaz Parra | Analista de pruebas, desarrolladora y responsable del plan | 10 horas (ver cronograma) |

---

## 10. Responsabilidades

| Tarea | Responsable |
|-------|-------------|
| Elaborar y actualizar el plan de pruebas | Ana María Díaz Parra |
| Diseñar los casos de prueba | Ana María Díaz Parra |
| Implementar pruebas unitarias y ciclos TDD | Ana María Díaz Parra |
| Ejecutar `mvn test` y revisar cobertura | Ana María Díaz Parra |
| Revisar y aprobar el Pull Request | Docente de la asignatura |

---

## 11. Cronograma y estimación de esfuerzo

| Fase | Actividad | Esfuerzo estimado |
|------|-----------|:-----------------:|
| 1 | Plan de pruebas (este documento) | 2 h |
| 2 | Diseño de casos de prueba (partición y valores límite) | 2,5 h |
| 3 | Pruebas unitarias de `MaterialService` y `PrestamoService` | 2 h |
| 4 | TDD: multa por devolución tardía y validación de correo | 2,5 h |
| 5 | Ejecución final, captura, Pull Request | 1 h |
| | **Total** | **10 h** |

---

## 12. Riesgos y contingencias

Probabilidad (P) e impacto (I) de 1 (bajo) a 3 (alto). Prioridad = P × I. Están ordenados de mayor a menor prioridad.

| # | Riesgo | P | I | P×I | Mitigación (antes) | Contingencia (si pasa) | Cómo se monitorea |
|---|--------|:-:|:-:|:---:|--------------------|------------------------|-------------------|
| R1 | Errores en la lógica nueva (multa y correo) que no se detecten | 2 | 3 | **6** | Desarrollarla con TDD y probar valores límite. | Abrir el defecto, escribir una prueba que lo reproduzca y corregir antes del merge. | Resultado de `mvn test` en cada commit. |
| R2 | Las pruebas pasan pero no cubren las ramas importantes (`if`) | 2 | 3 | **6** | Diseñar casos para cada camino feliz y triste. | Revisar el reporte de JaCoCo y agregar casos para las líneas en rojo. | Cobertura en `target/site/jacoco`. |
| R3 | Mocks mal configurados que hacen pasar pruebas que deberían fallar | 2 | 2 | **4** | Usar `verify()` y comprobar que cada prueba falle al menos una vez (fase *red*). | Revisar la prueba y cambiar la simulación. | Revisión del código de prueba antes del commit. |
| R4 | Maven no descarga dependencias (internet o versión de Java) | 1 | 3 | **3** | Usar Java 17+ y dejar las dependencias en el `pom.xml`. | Ejecutar desde IntelliJ con el Maven integrado o usar otra red. | Mensajes de `mvn test`. |
| R5 | Falta de tiempo por otras materias | 2 | 2 | **4** | Seguir el cronograma por fases. | Priorizar los casos de prioridad Alta y dejar los demás para U3. | Avance de la lista de tareas de este plan. |
| R6 | Cambios en las reglas de negocio (ej. valor de la multa) | 1 | 2 | **2** | La multa está en una constante (`MULTA_POR_DIA`). | Actualizar la constante, los casos y este plan (nueva versión). | Control de cambios (sección 14). |

---

## 13. Aprobaciones

| Rol | Nombre | Fecha |
|-----|--------|-------|
| Elaboró | Ana María Díaz Parra | noviembre de 2026 |
| Revisa | Docente de Pruebas de software | — |

---

## 14. Control de cambios del plan

| Versión | Fecha | Cambio | Autor |
|---------|-------|--------|-------|
| 1.0 | noviembre de 2026 | Versión inicial del plan para la Unidad 2. | Ana María Díaz Parra |
| 1.1 | noviembre de 2026 | Se agrega a los entregables el historial TDD y el workflow de CI (GitHub Actions); se agrega GitHub Actions a los recursos. La regla de correo repetido (CP-021) entra al alcance de F-07. | Ana María Díaz Parra |

Cualquier cambio en el alcance, los criterios o los riesgos se registra en esta tabla con una nueva versión y se sube a la rama de pruebas.
