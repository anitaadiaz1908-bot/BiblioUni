# Casos de prueba formales – BiblioUni

**Asignatura:** Pruebas de software – Unidad 2
**Autora:** Ana María Díaz Parra
**Fecha:** noviembre de 2026

En este documento los escenarios narrativos de la Unidad 1 (`escenarios.md`) se convierten en casos de prueba formales, con datos concretos. Para diseñarlos se usaron dos técnicas de caja negra:

- **Partición de equivalencia (PE):** dividir los datos de entrada en clases válidas e inválidas y tomar un valor representativo de cada clase.
- **Valores límite (VL):** probar justo en los bordes de una regla, porque ahí es donde más se equivoca uno al programar (por ejemplo `>` en vez de `>=`).

Para el préstamo de un material apartado también se usó una pequeña **tabla de decisión (TD)**.

---

## 1. Reglas de negocio que se usaron

| Regla | Valor |
|-------|-------|
| Máximo de préstamos activos por usuario | 3 (con 3 ya no puede pedir otro) |
| Días de préstamo | 8 (fecha límite = fecha de préstamo + 8 días) |
| Multa por devolución tardía *(nueva)* | $1.000 por cada día de retraso después de la fecha límite. Si se devuelve el día límite o antes, no hay multa. |
| Correo del usuario *(nueva)* | Debe tener texto antes de la @, una @ y un dominio con punto (ej. `laura@uniempresarial.edu.co`). No puede estar repetido. |

---

## 2. Matriz de clases de equivalencia

| Dato de entrada | Clases válidas | Clases inválidas | Valores límite |
|-----------------|----------------|------------------|----------------|
| Préstamos activos del usuario | V1: 0 a 2 | I1: 3 o más | 0 (mín), 2 (máx permitido), 3 (máx + 1), 4 |
| Estado del material al prestar | V2: DISPONIBLE | I2: PRESTADO · I3: APARTADO para otro usuario | — |
| Id de usuario / material | V3: existe | I4: no existe (999) | — |
| Días de retraso al devolver (fecha devolución − fecha límite) | V4: ≤ 0 (sin multa) · V5: > 0 (con multa) | — | −1, 0, 1 |
| Correo del usuario | V6: `texto@dominio.ext` | I5: sin @ · I6: sin texto antes de @ · I7: sin dominio · I8: vacío · I9: nulo · I10: correo ya registrado | — |
| Estado del préstamo al devolver | V7: activo | I11: ya devuelto | — |
| Estado del material al eliminar | V8: DISPONIBLE | I12: PRESTADO o APARTADO | — |

---

## 3. Resumen de los casos

| ID | Funcionalidad | Técnica | Camino | Prioridad | HU | Escenario U1 | Prueba automatizada |
|----|---------------|---------|--------|-----------|----|--------------|---------------------|
| CP-001 | Registrar material | PE (V8) | Feliz | Alta | HU-01 | EP-001 | `MaterialServiceTest` |
| CP-002 | Buscar material por id | PE (V3) | Feliz | Media | HU-02 | EP-002 | `MaterialServiceTest` |
| CP-003 | Buscar material por id | PE (I4) | **Triste** | Alta | HU-02 | EP-006 | `MaterialServiceTest` |
| CP-004 | Buscar material por título | PE | Feliz | Media | HU-02 | EP-002 | `MaterialServiceTest` |
| CP-005 | Eliminar material | PE (I12) | **Triste** | Media | HU-01 | — | `MaterialServiceTest` |
| CP-006 | Registrar préstamo | PE (V1, V2, V3) | Feliz | Alta | HU-04 | EP-003 | `PrestamoServiceTest` |
| CP-007 | Registrar préstamo | PE (I2) | **Triste** | Alta | HU-04 | EP-005 | `PrestamoServiceTest` |
| CP-008 | Límite de préstamos | VL (0 = mín) | Feliz | Media | HU-04 | EP-004 | `PrestamoServiceTest` (parametrizada) |
| CP-009 | Límite de préstamos | VL (2 = máx permitido) | Feliz | Alta | HU-04 | EP-004 | `PrestamoServiceTest` (parametrizada) |
| CP-010 | Límite de préstamos | VL (3 = máx + 1) | **Triste** | Alta | HU-04 | EP-004 | `PrestamoServiceTest` (parametrizada) |
| CP-011 | Límite de préstamos | VL (4) | **Triste** | Baja | HU-04 | EP-004 | `PrestamoServiceTest` (parametrizada) |
| CP-012 | Registrar préstamo | PE (I4) | **Triste** | Alta | HU-04 | EP-006 | `PrestamoServiceTest` |
| CP-013 | Calcular multa | VL (−1 día) | Feliz | Alta | HU-05 | — | `PrestamoServiceTest` (parametrizada) |
| CP-014 | Calcular multa | VL (0 días) | Feliz | Alta | HU-05 | — | `PrestamoServiceTest` (parametrizada) |
| CP-015 | Calcular multa | VL (+1 día) | Feliz | Alta | HU-05 | — | `PrestamoServiceTest` (parametrizada) |
| CP-016 | Calcular multa | PE (V5) | Feliz | Media | HU-05 | — | `PrestamoServiceTest` (parametrizada) |
| CP-017 | Devolver con retraso | PE (V5, V7) | Feliz | Alta | HU-05 | EP-007 | `PrestamoServiceTest` |
| CP-018 | Devolver préstamo | PE (I11) | **Triste** | Media | HU-05 | EP-012 | `PrestamoServiceTest` |
| CP-019 | Registrar usuario | PE (V6) | Feliz | Alta | HU-03 | EP-010 | `UsuarioServiceTest` |
| CP-020 | Registrar usuario | PE (I5 a I9) | **Triste** | Alta | HU-03 | EP-010 | `UsuarioServiceTest` (parametrizada) |
| CP-021 | Registrar usuario | PE (I10) | **Triste** | Media | HU-03 | — | `UsuarioServiceTest` |
| CP-022 | Prestar material apartado | TD / PE (I3) | **Triste** | Alta | HU-07 | EP-014 | Pendiente (U3, integración) |

**Totales:** 22 casos · 15 con partición de equivalencia · 7 con valores límite · 10 caminos tristes · 21 automatizados en esta unidad.

---

## 4. Detalle de los casos

### CP-001 – Registrar material válido
- **Funcionalidad:** Registrar material (`MaterialService.registrar`)
- **Técnica:** Partición de equivalencia – clase válida (título y autor con texto).
- **Precondiciones:** Ninguna.
- **Datos de entrada:** título = "Cien años de soledad", autor = "Gabriel García Márquez", tipo = "LIBRO", estado inicial = PRESTADO (para ver que el servicio lo cambia).
- **Pasos:** 1) Crear el material con los datos. 2) Llamar a `registrar(material)`.
- **Resultado esperado:** Se guarda con un id y su estado es DISPONIBLE. Se llama una vez a `materialRepository.save`.
- **Postcondición:** El material existe y se puede prestar.

### CP-002 – Buscar material por id existente
- **Funcionalidad:** Buscar material (`MaterialService.buscarPorId`)
- **Técnica:** Partición de equivalencia – clase válida (id que existe).
- **Precondiciones:** Existe el material con id 5.
- **Datos de entrada:** id = 5
- **Pasos:** 1) Llamar a `buscarPorId(5)`.
- **Resultado esperado:** Devuelve el material con id 5 y autor "Gabriel García Márquez".
- **Postcondición:** No cambia nada en la base de datos.

### CP-003 – Buscar material por id inexistente *(camino triste)*
- **Funcionalidad:** Buscar material
- **Técnica:** Partición de equivalencia – clase inválida (id que no existe).
- **Precondiciones:** No existe ningún material con id 999.
- **Datos de entrada:** id = 999
- **Pasos:** 1) Llamar a `buscarPorId(999)`.
- **Resultado esperado:** Se lanza `RecursoNoEncontradoException` con el mensaje "No existe el material con id 999" (en la API sería un 404).
- **Postcondición:** No cambia nada.

### CP-004 – Buscar material por título sin importar mayúsculas
- **Funcionalidad:** Buscar material por título
- **Técnica:** Partición de equivalencia – título escrito en minúscula (misma clase que en mayúscula).
- **Precondiciones:** Existe el material "Cálculo Diferencial".
- **Datos de entrada:** texto = "cálculo"
- **Pasos:** 1) Llamar a `buscarPorTitulo("cálculo")`.
- **Resultado esperado:** La lista tiene 1 elemento con título "Cálculo Diferencial".
- **Postcondición:** No cambia nada.

### CP-005 – Eliminar un material prestado *(camino triste)*
- **Funcionalidad:** Eliminar material
- **Técnica:** Partición de equivalencia – clase inválida (estado distinto de DISPONIBLE).
- **Precondiciones:** El material 5 está PRESTADO.
- **Datos de entrada:** id = 5
- **Pasos:** 1) Llamar a `eliminar(5)`.
- **Resultado esperado:** Se lanza `ReglaNegocioException` y **no** se llama a `delete`.
- **Postcondición:** El material sigue existiendo.

### CP-006 – Registrar préstamo exitoso
- **Funcionalidad:** Registrar préstamo (`PrestamoService.prestar`)
- **Técnica:** Partición de equivalencia – todas las entradas en clases válidas.
- **Precondiciones:** Usuaria Laura (id 1) existe y tiene 0 préstamos activos. Material 5 está DISPONIBLE.
- **Datos de entrada:** usuarioId = 1, materialId = 5
- **Pasos:** 1) Llamar a `prestar(1, 5)`.
- **Resultado esperado:** Préstamo activo de Laura con el material 5, fecha de préstamo = hoy, fecha límite = hoy + 8 días. El material queda PRESTADO y se guarda.
- **Postcondición:** Laura tiene 1 préstamo activo; el material no se le puede prestar a nadie más.

### CP-007 – Registrar préstamo con material no disponible *(camino triste)*
- **Funcionalidad:** Registrar préstamo
- **Técnica:** Partición de equivalencia – clase inválida (material PRESTADO).
- **Precondiciones:** Laura (id 1) existe con 0 préstamos. El material 5 está PRESTADO a otra persona.
- **Datos de entrada:** usuarioId = 1, materialId = 5
- **Pasos:** 1) Llamar a `prestar(1, 5)`.
- **Resultado esperado:** `ReglaNegocioException` con el mensaje "El material no está disponible". No se guarda ningún préstamo.
- **Postcondición:** El préstamo anterior del material no cambia.

### CP-008 – Préstamo con 0 préstamos activos (valor mínimo)
- **Funcionalidad:** Límite de préstamos activos
- **Técnica:** Valores límite – mínimo del rango válido.
- **Precondiciones:** Laura existe y tiene **0** préstamos activos. Material 5 DISPONIBLE.
- **Datos de entrada:** usuarioId = 1, materialId = 5, préstamos activos = 0
- **Pasos:** 1) Llamar a `prestar(1, 5)`.
- **Resultado esperado:** El préstamo se crea.
- **Postcondición:** Laura queda con 1 préstamo activo.

### CP-009 – Préstamo con 2 préstamos activos (máximo permitido)
- **Funcionalidad:** Límite de préstamos activos
- **Técnica:** Valores límite – último valor válido antes del límite.
- **Precondiciones:** Laura tiene **2** préstamos activos. Material 5 DISPONIBLE.
- **Datos de entrada:** usuarioId = 1, materialId = 5, préstamos activos = 2
- **Pasos:** 1) Llamar a `prestar(1, 5)`.
- **Resultado esperado:** El préstamo se crea (sería el tercero).
- **Postcondición:** Laura queda con 3 préstamos activos.

### CP-010 – Préstamo con 3 préstamos activos *(camino triste)*
- **Funcionalidad:** Límite de préstamos activos
- **Técnica:** Valores límite – primer valor inválido.
- **Precondiciones:** Laura tiene **3** préstamos activos. Material 5 DISPONIBLE.
- **Datos de entrada:** usuarioId = 1, materialId = 5, préstamos activos = 3
- **Pasos:** 1) Llamar a `prestar(1, 5)`.
- **Resultado esperado:** `ReglaNegocioException` con "El usuario ya tiene 3 préstamos activos". No se guarda el préstamo y el material no cambia de estado.
- **Postcondición:** Laura sigue con 3 préstamos.

### CP-011 – Préstamo con 4 préstamos activos *(camino triste)*
- **Funcionalidad:** Límite de préstamos activos
- **Técnica:** Valores límite – valor por encima del límite (dato que no debería existir, por ejemplo si se cargaron datos a mano).
- **Precondiciones:** Laura tiene **4** préstamos activos.
- **Datos de entrada:** usuarioId = 1, materialId = 5, préstamos activos = 4
- **Pasos:** 1) Llamar a `prestar(1, 5)`.
- **Resultado esperado:** Igual que CP-010: se rechaza.
- **Postcondición:** No se crea préstamo.

### CP-012 – Préstamo con usuario inexistente *(camino triste)*
- **Funcionalidad:** Registrar préstamo
- **Técnica:** Partición de equivalencia – clase inválida (usuario no existe).
- **Precondiciones:** No existe el usuario 999.
- **Datos de entrada:** usuarioId = 999, materialId = 5
- **Pasos:** 1) Llamar a `prestar(999, 5)`.
- **Resultado esperado:** `RecursoNoEncontradoException` ("No existe el usuario con id 999"). No se busca el material ni se guarda nada.
- **Postcondición:** No cambia nada.

### CP-013 – Multa al devolver 1 día antes de la fecha límite
- **Funcionalidad:** Calcular multa (`PrestamoService.calcularMulta`)
- **Técnica:** Valores límite – justo antes del límite.
- **Precondiciones:** Préstamo con fecha límite 10/11/2026.
- **Datos de entrada:** fecha de devolución = 09/11/2026 (−1 día)
- **Pasos:** 1) Llamar a `calcularMulta(prestamo, 09/11/2026)`.
- **Resultado esperado:** $0
- **Postcondición:** No cambia nada (solo es un cálculo).

### CP-014 – Multa al devolver el mismo día límite
- **Funcionalidad:** Calcular multa
- **Técnica:** Valores límite – en el límite exacto.
- **Precondiciones:** Préstamo con fecha límite 10/11/2026.
- **Datos de entrada:** fecha de devolución = 10/11/2026 (0 días)
- **Pasos:** 1) Llamar a `calcularMulta(prestamo, 10/11/2026)`.
- **Resultado esperado:** $0 (todavía está a tiempo).
- **Postcondición:** No cambia nada.

### CP-015 – Multa al devolver 1 día después de la fecha límite
- **Funcionalidad:** Calcular multa
- **Técnica:** Valores límite – justo después del límite.
- **Precondiciones:** Préstamo con fecha límite 10/11/2026.
- **Datos de entrada:** fecha de devolución = 11/11/2026 (+1 día)
- **Pasos:** 1) Llamar a `calcularMulta(prestamo, 11/11/2026)`.
- **Resultado esperado:** $1.000
- **Postcondición:** No cambia nada.

### CP-016 – Multa con varios días de retraso
- **Funcionalidad:** Calcular multa
- **Técnica:** Partición de equivalencia – valor representativo de la clase "con retraso".
- **Precondiciones:** Préstamo con fecha límite 10/11/2026.
- **Datos de entrada:** fecha de devolución = 20/11/2026 (10 días)
- **Pasos:** 1) Llamar a `calcularMulta(prestamo, 20/11/2026)`.
- **Resultado esperado:** $10.000
- **Postcondición:** No cambia nada.

### CP-017 – Devolver con retraso guarda la multa
- **Funcionalidad:** Devolver préstamo (`PrestamoService.devolver`)
- **Técnica:** Partición de equivalencia – préstamo activo (V7) devuelto con retraso (V5).
- **Precondiciones:** Préstamo 10 activo, con fecha límite hace 3 días.
- **Datos de entrada:** prestamoId = 10
- **Pasos:** 1) Llamar a `devolver(10)`.
- **Resultado esperado:** Préstamo inactivo, fecha de devolución = hoy, multa = $3.000. Se llama a `reservaService.asignarAlSiguiente` con el material.
- **Postcondición:** El material queda disponible o apartado para el siguiente de la lista de espera.

### CP-018 – Devolver un préstamo que ya fue devuelto *(camino triste)*
- **Funcionalidad:** Devolver préstamo
- **Técnica:** Partición de equivalencia – clase inválida (préstamo inactivo).
- **Precondiciones:** El préstamo 10 ya fue devuelto (activo = false).
- **Datos de entrada:** prestamoId = 10
- **Pasos:** 1) Llamar a `devolver(10)`.
- **Resultado esperado:** `ReglaNegocioException` con "Este préstamo ya fue devuelto". No se guarda nada ni se mueve la lista de espera.
- **Postcondición:** La fecha de devolución original no cambia.

### CP-019 – Registrar usuario con correo válido
- **Funcionalidad:** Registrar usuario (`UsuarioService.registrar`)
- **Técnica:** Partición de equivalencia – clase válida.
- **Precondiciones:** No existe otro usuario con ese correo.
- **Datos de entrada:** nombre = "Laura Gómez", correo = "laura@uniempresarial.edu.co", rol = "ESTUDIANTE"
- **Pasos:** 1) Llamar a `registrar(usuario)`.
- **Resultado esperado:** El usuario se guarda (se llama a `usuarioRepository.save`).
- **Postcondición:** Laura puede pedir préstamos.

### CP-020 – Registrar usuario con correo inválido *(camino triste)*
- **Funcionalidad:** Registrar usuario
- **Técnica:** Partición de equivalencia – un valor por cada clase inválida.
- **Precondiciones:** Ninguna.
- **Datos de entrada:** `laura.gmail.com` (I5, sin @), `@gmail.com` (I6, sin texto antes), `laura@` (I7, sin dominio), `""` (I8, vacío), `null` (I9).
- **Pasos:** 1) Para cada correo, llamar a `registrar(usuario)`.
- **Resultado esperado:** En todos los casos `ReglaNegocioException` con "El correo no es válido" y **no** se guarda el usuario.
- **Postcondición:** No se crea ningún usuario.

### CP-021 – Registrar usuario con correo repetido *(camino triste)*
- **Funcionalidad:** Registrar usuario
- **Técnica:** Partición de equivalencia – clase inválida (correo ya registrado).
- **Precondiciones:** Ya existe un usuario con el correo `laura@uniempresarial.edu.co`.
- **Datos de entrada:** correo = "Laura@Uniempresarial.edu.co" (con mayúsculas, debe detectarse igual)
- **Pasos:** 1) Llamar a `registrar(usuario)`.
- **Resultado esperado:** `ReglaNegocioException` con "Ya existe un usuario con ese correo". No se guarda.
- **Postcondición:** Sigue habiendo un solo usuario con ese correo.

### CP-022 – Prestar un material apartado para otro usuario *(camino triste, gancho innovador)*
- **Funcionalidad:** Registrar préstamo de material APARTADO
- **Técnica:** Tabla de decisión (ver sección 5) + partición de equivalencia (I3).
- **Precondiciones:** El material 5 está APARTADO para Carlos (id 2) por la lista de espera. Laura (id 1) tiene 0 préstamos.
- **Datos de entrada:** usuarioId = 1, materialId = 5
- **Pasos:** 1) `POST /api/prestamos?usuarioId=1&materialId=5`. 2) Luego `POST /api/prestamos?usuarioId=2&materialId=5`.
- **Resultado esperado:** Para Laura: 400 con "El material está apartado para otro usuario de la lista de espera". Para Carlos: 201 y su reserva queda CUMPLIDA.
- **Postcondición:** El material queda PRESTADO a Carlos.
- **Nota:** se automatiza en la Unidad 3 como prueba de integración.

---

## 5. Tabla de decisión: ¿se puede prestar el material?

| Condición / Regla | R1 | R2 | R3 | R4 | R5 | R6 |
|-------------------|:--:|:--:|:--:|:--:|:--:|:--:|
| ¿El usuario existe? | No | Sí | Sí | Sí | Sí | Sí |
| ¿Tiene menos de 3 préstamos activos? | – | No | Sí | Sí | Sí | Sí |
| Estado del material | – | – | DISPONIBLE | PRESTADO | APARTADO | APARTADO |
| ¿El apartado es para este usuario? | – | – | – | – | No | Sí |
| **Acción** | Error 404 | Error límite | **Presta** | Error no disponible | Error apartado | **Presta** y reserva CUMPLIDA |
| **Caso** | CP-012 | CP-010, CP-011 | CP-006, CP-008, CP-009 | CP-007 | CP-022 | CP-022 |

---

## 6. Trazabilidad

### 6.1 Historia de usuario → casos

| HU | Casos de prueba |
|----|-----------------|
| HU-01 Registrar materiales | CP-001, CP-005 |
| HU-02 Buscar materiales | CP-002, CP-003, CP-004 |
| HU-03 Registrar usuarios | CP-019, CP-020, CP-021 |
| HU-04 Pedir prestado (máx. 3) | CP-006 a CP-012 |
| HU-05 Devolver material | CP-013 a CP-018 |
| HU-06 Lista de espera | Escenarios EP-008 y EP-013 (se automatizan en U3) |
| HU-07 Apartado 48 horas | CP-022 |

### 6.2 Escenario de la Unidad 1 → casos

| Escenario (U1) | Caso(s) U2 |
|----------------|------------|
| EP-001 | CP-001 |
| EP-002 | CP-002, CP-004 |
| EP-003 | CP-006 |
| EP-004 | CP-008, CP-009, CP-010, CP-011 |
| EP-005 | CP-007 |
| EP-006 | CP-003, CP-012 |
| EP-007 | CP-017 |
| EP-010 | CP-019, CP-020 |
| EP-012 | CP-018 |
| EP-014 | CP-022 |

Los escenarios EP-008, EP-009, EP-011, EP-013, EP-015, EP-016 y EP-017 son de la lista de espera, de la API o no funcionales, así que se convierten en casos en la Unidad 3.
