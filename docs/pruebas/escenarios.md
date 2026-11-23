# Escenarios de prueba narrativos – BiblioUni

Estos escenarios describen en lenguaje natural lo que se va a probar. En la Unidad 2 se convertirán en casos de prueba formales y en la Unidad 3 en pruebas automatizadas.

Cada escenario está escrito con la estructura **Dado – Cuando – Entonces** (precondición, acción y resultado esperado).

---

## 1. Resumen de escenarios

| ID | Componente bajo prueba | Descripción | Tipo de prueba | Prioridad | Camino | HU |
|----|------------------------|-------------|----------------|-----------|--------|----|
| EP-001 | `MaterialService` | Verificar que se pueda registrar un material con título y autor, y que quede DISPONIBLE. | Unitaria | Alta | Feliz | HU-01 |
| EP-002 | `MaterialService` | Verificar que la búsqueda por título encuentre el material sin importar mayúsculas o minúsculas. | Unitaria | Media | Feliz | HU-02 |
| EP-003 | `PrestamoService` | Verificar que un estudiante pueda pedir prestado un material disponible y que el material quede PRESTADO. | Unitaria | Alta | Feliz | HU-04 |
| EP-004 | `PrestamoService` | Verificar que un estudiante no pueda tener más de 3 préstamos activos. | Unitaria | Alta | Triste | HU-04 |
| EP-005 | `PrestamoService` | Verificar que no se pueda prestar un material que ya está prestado. | Unitaria | Alta | Triste | HU-04 |
| EP-006 | `PrestamoController` | Verificar que al pedir un préstamo con un usuario que no existe la API responda 404. | API | Alta | Triste | HU-04 |
| EP-007 | `PrestamoService` + BD | Verificar que al devolver un material sin nadie en la lista de espera, quede DISPONIBLE. | Integración | Alta | Feliz | HU-05 |
| EP-008 | `ReservaService` | Verificar que un estudiante se pueda anotar en la lista de espera de un material prestado y vea su posición. | Unitaria | Alta | Feliz | HU-06 |
| EP-009 | `PrestamoService` + `ReservaService` + BD | Verificar que al devolver un material con fila de espera, quede APARTADO para el primero de la fila por 48 h. | Integración | Alta | Feliz | HU-07 |
| EP-010 | `UsuarioController` | Verificar que no se pueda registrar un usuario con un correo inválido. | API | Media | Triste | HU-03 |
| EP-011 | `MaterialController` | Verificar que no se pueda registrar un material sin título. | API | Media | Triste | HU-01 |
| EP-012 | `PrestamoService` | Verificar que no se pueda devolver dos veces el mismo préstamo. | Unitaria | Media | Triste | HU-05 |
| EP-013 | `ReservaService` | Verificar que un estudiante no se pueda anotar en la lista si el material está disponible o si ya está en la fila. | Unitaria | Media | Triste | HU-06 |
| EP-014 | `PrestamoService` + `ReservaService` | Verificar que un material APARTADO solo lo pueda llevar la persona a quien le tocó el turno. | Integración | Alta | Triste | HU-07 |
| EP-015 | `ReservaService` | Verificar que si pasan más de 48 h sin recoger el material, el apartado pase al siguiente de la fila. | Unitaria | Alta | Alterno | HU-07 |
| EP-016 | `MaterialController` | Verificar que la consulta de materiales responda rápido aunque haya muchos registros. | No funcional (rendimiento) | Baja | — | HU-02 |
| EP-017 | `ManejadorErrores` | Verificar que todos los errores devuelvan un mensaje claro en español y no un error 500. | No funcional (usabilidad) | Media | — | Todas |

**Escenarios del gancho innovador (lista de espera):** EP-008, EP-009, EP-013, EP-014 y EP-015.
**Escenarios de camino triste:** EP-004, EP-005, EP-006, EP-010, EP-011, EP-012, EP-013 y EP-014.
**Escenarios no funcionales:** EP-016 y EP-017.

---

## 2. Detalle de cada escenario

### EP-001 – Registrar material válido
- **Dado** que el bibliotecario tiene los datos de un libro nuevo
- **Cuando** registra el material con título "Cien años de soledad", autor "Gabriel García Márquez" y tipo "LIBRO"
- **Entonces** el material se guarda con un id y su estado es DISPONIBLE.
- **Criterio de aceptación:** el id no es nulo y `estado = DISPONIBLE`.

### EP-002 – Buscar material por título
- **Dado** que existe el material "Cálculo Diferencial"
- **Cuando** el estudiante busca "cálculo" (todo en minúscula)
- **Entonces** el material aparece en los resultados.
- **Criterio de aceptación:** la lista tiene 1 elemento con ese título.

### EP-003 – Préstamo exitoso
- **Dado** que la estudiante Laura (id 1) tiene 0 préstamos activos y el libro con id 5 está DISPONIBLE
- **Cuando** pide prestado el libro 5
- **Entonces** se crea el préstamo y el libro pasa a PRESTADO.
- **Criterio de aceptación:** préstamo activo, `fechaLimite = fechaPrestamo + 8 días`, material en PRESTADO, respuesta HTTP 201.

### EP-004 – Límite de 3 préstamos activos
- **Dado** que la estudiante Laura ya tiene 3 préstamos activos
- **Cuando** intenta pedir un cuarto material disponible
- **Entonces** el sistema lo rechaza con el mensaje "El usuario ya tiene 3 préstamos activos".
- **Datos de prueba (valores límite):** con 2 préstamos → sí se permite; con 3 → se rechaza; con 4 (no debería pasar) → se rechaza.
- **Criterio de aceptación:** se lanza `ReglaNegocioException`, la API responde 400 y no se crea ningún préstamo nuevo.

### EP-005 – Material no disponible
- **Dado** que el libro con id 5 está PRESTADO a otro estudiante
- **Cuando** Laura intenta pedirlo prestado
- **Entonces** el sistema responde "El material no está disponible".
- **Criterio de aceptación:** respuesta 400 y el préstamo anterior no cambia.

### EP-006 – Usuario inexistente
- **Dado** que no existe ningún usuario con id 999
- **Cuando** se hace `POST /api/prestamos?usuarioId=999&materialId=5`
- **Entonces** la API responde 404 con el mensaje "No existe el usuario con id 999".
- **Criterio de aceptación:** código 404 y JSON con el campo `error`.

### EP-007 – Devolución sin lista de espera
- **Dado** que Laura tiene prestado el libro 5 y nadie está en su lista de espera
- **Cuando** devuelve el libro (`PUT /api/prestamos/{id}/devolucion`)
- **Entonces** el préstamo queda inactivo con fecha de devolución y el libro vuelve a DISPONIBLE.
- **Criterio de aceptación:** `activo = false`, `fechaDevolucion = hoy`, material en DISPONIBLE (consultado en la base de datos).

### EP-008 – Anotarse en la lista de espera *(gancho innovador)*
- **Dado** que el libro 5 está PRESTADO y Carlos y Sofía ya están en la fila
- **Cuando** Laura se anota en la lista de espera
- **Entonces** su reserva queda EN_ESPERA y su posición es 3.
- **Criterio de aceptación:** `GET /api/reservas/posicion?usuarioId=1&materialId=5` devuelve `{"posicion": 3}`.

### EP-009 – Apartado automático al devolver *(gancho innovador)*
- **Dado** que el libro 5 está prestado y Carlos es el primero de la fila
- **Cuando** se devuelve el libro
- **Entonces** el libro queda APARTADO, la reserva de Carlos pasa a NOTIFICADA y vence en 48 horas.
- **Criterio de aceptación:** material en APARTADO, reserva NOTIFICADA, `venceApartado` entre 47 h 59 min y 48 h desde la devolución. Las reservas de los demás siguen EN_ESPERA.

### EP-010 – Usuario con correo inválido
- **Dado** que se quiere registrar a un usuario nuevo
- **Cuando** se envía el correo "laura.gmail.com" (sin @)
- **Entonces** la API responde 400 con el mensaje "El correo no es válido".
- **Datos de prueba (clases de equivalencia):** válido: `laura@uniempresarial.edu.co`; inválidos: `laura.gmail.com`, `@gmail.com`, vacío.
- **Criterio de aceptación:** código 400 y el usuario no se guarda.

### EP-011 – Material sin título
- **Dado** que se quiere registrar un material
- **Cuando** se envía con título vacío `""`
- **Entonces** la API responde 400 con "El título es obligatorio".
- **Criterio de aceptación:** código 400 y el número de materiales en la BD no cambia.

### EP-012 – Devolver dos veces
- **Dado** que el préstamo 10 ya fue devuelto
- **Cuando** se intenta devolver otra vez
- **Entonces** el sistema responde "Este préstamo ya fue devuelto".
- **Criterio de aceptación:** respuesta 400 y la fecha de devolución original no cambia.

### EP-013 – Reserva no permitida *(gancho innovador – camino triste)*
- **Caso A – Dado** que el libro 7 está DISPONIBLE, **cuando** Laura intenta anotarse en la fila, **entonces** el sistema responde "El material está disponible, puede pedirlo prestado directamente".
- **Caso B – Dado** que Laura ya está en la fila del libro 5, **cuando** intenta anotarse otra vez, **entonces** responde "El usuario ya está en la lista de espera de este material".
- **Criterio de aceptación:** respuesta 400 en ambos casos y no se crean reservas duplicadas.

### EP-014 – Material apartado para otra persona *(gancho innovador – camino triste)*
- **Dado** que el libro 5 está APARTADO para Carlos
- **Cuando** Laura intenta pedirlo prestado
- **Entonces** el sistema responde "El material está apartado para otro usuario de la lista de espera".
- **Y cuando** Carlos lo pide, el préstamo se crea y su reserva pasa a CUMPLIDA.
- **Criterio de aceptación:** 400 para Laura, 201 para Carlos.

### EP-015 – Vencimiento del apartado *(gancho innovador – camino alterno)*
- **Dado** que el libro 5 está apartado para Carlos y Sofía es la segunda de la fila
- **Cuando** pasan más de 48 horas sin que Carlos lo recoja
- **Entonces** la reserva de Carlos queda VENCIDA y el libro queda apartado para Sofía.
- **Datos de prueba (valores límite):** a las 47 h 59 min → sigue siendo de Carlos; a las 48 h 01 min → pasa a Sofía.
- **Criterio de aceptación:** reserva de Carlos VENCIDA, reserva de Sofía NOTIFICADA.

### EP-016 – Tiempo de respuesta de la consulta *(no funcional)*
- **Dado** que hay 500 materiales registrados
- **Cuando** se consulta `GET /api/materiales`
- **Entonces** la respuesta llega rápido.
- **Criterio de aceptación:** tiempo de respuesta menor a 1 segundo en el computador de pruebas.

### EP-017 – Mensajes de error claros *(no funcional)*
- **Dado** cualquier error esperado (usuario inexistente, datos inválidos, regla de negocio)
- **Cuando** la API responde
- **Entonces** el mensaje viene en español, en un JSON con el campo `error` y el código correcto (400 o 404).
- **Criterio de aceptación:** ninguno de los escenarios EP-004 a EP-014 devuelve un error 500.

---

## 3. Clases de equivalencia y valores límite usados

| Dato | Clase válida | Clases inválidas | Valores límite |
|------|-------------|------------------|----------------|
| Préstamos activos del usuario | 0 a 2 (puede pedir otro) | 3 o más | 2, 3, 4 |
| Correo del usuario | Tiene texto, @ y dominio | Sin @, sin texto antes de @, vacío | — |
| Título del material | Texto no vacío | Vacío o solo espacios | 0 y 1 carácter |
| Tiempo del apartado | Menos de 48 h | Más de 48 h | 47 h 59 min, 48 h 01 min |
| Id de usuario/material | Existe en la BD | No existe (ej. 999) | — |

---

## 4. Matriz de priorización por riesgo e impacto

|  | **Impacto bajo** | **Impacto medio** | **Impacto alto** |
|---|---|---|---|
| **Probabilidad alta** | — | EP-017 | EP-008, EP-009, EP-014, EP-015 |
| **Probabilidad media** | — | EP-010, EP-011, EP-012, EP-013 | EP-003, EP-004, EP-005, EP-006 |
| **Probabilidad baja** | EP-002, EP-016 | — | EP-001, EP-007 |

Los escenarios de la esquina superior derecha (lista de espera) se van a probar primero, porque es lógica nueva y si falla un estudiante podría quedarse sin el material que le tocaba.
