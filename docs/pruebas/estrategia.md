# Estrategia de pruebas – BiblioUni

**Asignatura:** Pruebas de software – Unidad 1
**Autora:** Ana María Díaz Parra
**Fecha:** octubre de 2026

---

## 1. Introducción

BiblioUni es una aplicación para manejar los préstamos de la biblioteca de la universidad. Permite registrar materiales (libros, revistas, tesis) y usuarios, prestar y devolver materiales y, como gancho innovador, tiene una **lista de espera inteligente**. Todo funciona por medio de una API REST hecha en Spring Boot con una base de datos H2.

En este documento explico qué vamos a probar, con qué tipos y niveles de prueba, qué dejamos por fuera y por qué. La idea es que esta estrategia sirva de guía para diseñar los casos de prueba en la Unidad 2 y automatizarlos en la Unidad 3.

### 1.1 Objetivos de las pruebas

- Comprobar que las reglas de la biblioteca se cumplen (por ejemplo, máximo 3 préstamos activos por usuario).
- Comprobar que la API responde con los códigos y mensajes correctos, tanto cuando todo sale bien como cuando hay errores.
- Asegurar que la lista de espera asigna el material a la persona correcta y en el orden correcto.
- Encontrar defectos lo antes posible, sobre todo en los servicios, que es donde está la lógica más importante.

### 1.2 Requisitos (historias de usuario) que se tomaron como base

| ID | Historia de usuario |
|----|---------------------|
| HU-01 | Como bibliotecario quiero registrar materiales para que los estudiantes los puedan consultar y pedir. |
| HU-02 | Como estudiante quiero buscar materiales por título y ver cuáles están disponibles. |
| HU-03 | Como bibliotecario quiero registrar usuarios con nombre y correo válidos. |
| HU-04 | Como estudiante quiero pedir prestado un material disponible, con un máximo de 3 préstamos activos. |
| HU-05 | Como estudiante quiero devolver un material para cerrar mi préstamo. |
| HU-06 | Como estudiante quiero anotarme en la lista de espera de un material prestado y saber en qué posición estoy. *(gancho innovador)* |
| HU-07 | Como estudiante quiero que, cuando devuelvan el material que espero, me quede apartado 48 horas. *(gancho innovador)* |

---

## 2. ¿Qué se va a probar?

El sistema está dividido en capas. Estas son las partes que se van a probar:

| Componente | Qué hace | Clases | HU relacionadas | Responsable |
|-----------|----------|--------|-----------------|-------------|
| **Modelo** | Representa los datos y sus validaciones (campos obligatorios, correo válido). | `Material`, `Usuario`, `Prestamo`, `Reserva` | HU-01, HU-03 | Ana María Díaz |
| **Servicios** | Tienen las reglas de la biblioteca. Es la parte más importante. | `MaterialService`, `UsuarioService`, `PrestamoService`, `ReservaService` | HU-01 a HU-07 | Ana María Díaz |
| **Controladores REST** | Reciben las peticiones HTTP y devuelven las respuestas en JSON. | `MaterialController`, `UsuarioController`, `PrestamoController`, `ReservaController`, `ManejadorErrores` | HU-01 a HU-07 | Ana María Díaz |
| **Base de datos** | Guarda la información (repositorios de Spring Data con H2). | `MaterialRepository`, `PrestamoRepository`, `ReservaRepository`, `UsuarioRepository` | HU-02, HU-04, HU-06 | Ana María Díaz |

---

## 3. ¿Qué tipos de pruebas aplican y por qué?

| Tipo de prueba | ¿Aplica? | Por qué |
|----------------|----------|---------|
| **Unitarias** | Sí | Los servicios tienen reglas que se pueden revisar por separado (límite de préstamos, estados del material, orden de la fila). Se usan JUnit 5 y Mockito para "simular" los repositorios y probar solo la lógica. Son rápidas y muestran exactamente dónde está el error. |
| **De integración** | Sí | Hay que comprobar que el controlador, el servicio y la base de datos funcionan juntos. Por ejemplo, que al prestar un libro su estado realmente cambie a PRESTADO en la base de datos. Se usa `@SpringBootTest` con H2. |
| **Funcionales** | Sí | Revisan que el sistema haga lo que piden las historias de usuario, sin mirar el código por dentro (caja negra). Por ejemplo: "un estudiante con 3 préstamos no puede pedir un cuarto". |
| **De API** | Sí | Toda la aplicación se usa a través de la API REST, así que hay que revisar URLs, métodos (GET, POST, PUT, DELETE), códigos de respuesta (200, 201, 400, 404) y el JSON que se devuelve. Se usa MockMvc y Postman para pruebas manuales. |
| **No funcionales (básicas)** | Sí, en parte | Se revisan algunos aspectos de calidad: que los mensajes de error sean claros (usabilidad), que las respuestas no se demoren demasiado (rendimiento básico) y que los datos inválidos se rechacen (seguridad básica). |
| **De regresión** | Sí | Cada vez que se cambie el código se vuelven a correr todas las pruebas con `mvn test` para asegurar que no se dañó algo que ya funcionaba. |

### Técnicas que se van a usar

- **Caja negra:** clases de equivalencia y valores límite. Por ejemplo, para el límite de préstamos se prueba con 2, 3 y 4 préstamos.
- **Caja blanca:** revisar que se recorran los caminos del código (los `if` de `PrestamoService.prestar()`), apoyándonos en el reporte de cobertura de JaCoCo.
- **Basadas en la experiencia:** probar errores comunes, como ids que no existen, campos vacíos o devolver dos veces el mismo préstamo.

---

## 4. ¿Qué niveles de pruebas se van a ejecutar?

| Nivel | Qué se prueba en BiblioUni | Herramienta | Criterio de entrada | Criterio de salida |
|-------|----------------------------|-------------|---------------------|--------------------|
| **Unitarias** | Cada servicio por separado (`PrestamoService`, `ReservaService`, `MaterialService`, `UsuarioService`), con los repositorios simulados. | JUnit 5 + Mockito | El servicio compila y tiene sus reglas definidas. | 100 % de las pruebas pasan y la cobertura de servicios es de al menos 80 %. |
| **Integración** | Los controladores junto con los servicios y la base de datos H2. | `@SpringBootTest` + MockMvc | Las pruebas unitarias del componente ya pasan. | Todos los endpoints responden con el código esperado; 0 defectos de prioridad alta abiertos. |
| **Sistema** | Flujos completos de principio a fin: registrar usuario → registrar material → prestar → anotarse en la lista → devolver → apartado al siguiente. | Postman / MockMvc | La aplicación arranca sin errores y las pruebas de integración pasan. | Los flujos principales funcionan completos y sin errores. |
| **Aceptación** | Revisar con el profesor (como "cliente") que las historias de usuario se cumplen. | Demostración y lista de chequeo | Las pruebas de sistema pasaron. | Cada HU cumple sus criterios de aceptación. |

---

## 5. ¿Qué NO se va a probar y por qué?

| Qué no se prueba | Por qué |
|------------------|---------|
| **Pruebas de carga y estrés** (cientos de usuarios al mismo tiempo) | Es un proyecto académico que corre en un solo computador con base de datos en memoria. Los resultados no serían reales y se necesitan herramientas como JMeter que están fuera del alcance del módulo. |
| **Seguridad avanzada** (pruebas de penetración, ataques) | La aplicación todavía no tiene inicio de sesión ni roles con contraseña. Solo se revisa la validación de datos de entrada. |
| **Interfaz gráfica (frontend)** | BiblioUni por ahora solo tiene la API REST, no tiene pantallas. |
| **Envío real de correos** | La notificación de la lista de espera queda indicada en el código, pero no se envían correos reales. Solo se prueba que la reserva cambie a estado NOTIFICADA. |
| **Compatibilidad con otros motores de base de datos** (MySQL, PostgreSQL) | En este módulo solo se usa H2. |
| **Clases de Spring y librerías externas** | Ya fueron probadas por quienes las crearon; solo probamos nuestro código. |

---

## 6. Pruebas del gancho innovador: lista de espera inteligente

### 6.1 ¿En qué consiste?

Cuando un material está prestado, el estudiante no tiene que estar preguntando a cada rato si ya lo devolvieron. Se anota en una **lista de espera** y puede ver en qué puesto va. Cuando el material se devuelve:

1. El sistema busca al primero de la fila.
2. El material queda **APARTADO** solo para esa persona por **48 horas** y su reserva pasa a **NOTIFICADA**.
3. Si no lo recoge en ese tiempo, la reserva queda **VENCIDA** y el material pasa al siguiente de la fila.
4. Si no hay nadie esperando, el material vuelve a quedar **DISPONIBLE**.

### 6.2 Componentes involucrados

`Reserva`, `ReservaService`, `ReservaController`, `ReservaRepository` y el método `devolver()` de `PrestamoService`.

### 6.3 Cómo se va a probar

| Qué se revisa | Tipo / nivel | Criterio de prueba (resultado esperado) |
|---------------|--------------|------------------------------------------|
| Anotarse en la lista de un material prestado | Unitaria (`ReservaService`) | Se crea la reserva con estado EN_ESPERA. |
| No se puede anotar si el material está disponible | Unitaria | Error: "El material está disponible…" (HTTP 400). |
| No anotarse dos veces en la misma lista | Unitaria | Error: "El usuario ya está en la lista…" (HTTP 400). |
| Orden de la fila | Unitaria | Con 3 personas en la fila, la posición devuelta es 1, 2 y 3 según el orden de llegada. |
| Al devolver se aparta al primero | Integración (`PrestamoService` + `ReservaService` + BD) | Material en APARTADO, reserva en NOTIFICADA y fecha de vencimiento = ahora + 48 h. |
| Solo el dueño del apartado puede prestarlo | Integración | Otro usuario recibe error 400; el dueño sí puede y su reserva queda CUMPLIDA. |
| Vence el apartado | Unitaria (simulando la fecha) | Si pasan más de 48 h, la reserva queda VENCIDA y el material pasa al siguiente. |
| Devolución sin fila | Unitaria | El material queda DISPONIBLE. |
| Endpoints `/api/reservas` | API (MockMvc) | POST devuelve 201; GET `/posicion` devuelve `{"posicion": n}`. |

---

## 7. Análisis de riesgos por componente

Se calificó la **probabilidad** de que haya un error y el **impacto** si pasa (1 = bajo, 3 = alto). El riesgo es probabilidad × impacto.

| Componente | Riesgo identificado | Prob. | Impacto | Nivel de riesgo | Cómo se mitiga |
|------------|---------------------|:-----:|:-------:|:---------------:|----------------|
| `PrestamoService` | Se presta un material que ya está prestado o se pasa el límite de 3. | 2 | 3 | **6 – Alto** | Pruebas unitarias con valores límite (2, 3 y 4 préstamos) y de integración. |
| `ReservaService` | La fila no respeta el orden o se aparta a la persona equivocada. | 3 | 3 | **9 – Alto** | Es lógica nueva: más escenarios unitarios y de integración. |
| Controladores REST | Devuelven un código HTTP equivocado (500 en vez de 404). | 2 | 2 | **4 – Medio** | Pruebas de API para cada camino triste. |
| Modelo / validaciones | Se guardan usuarios sin correo o materiales sin título. | 2 | 2 | **4 – Medio** | Pruebas de validación con datos inválidos. |
| Base de datos | Los cambios de estado no se guardan. | 1 | 3 | **3 – Medio** | Pruebas de integración que consultan la BD después de cada acción. |
| `MaterialService` (búsqueda) | La búsqueda no encuentra títulos con mayúsculas o minúsculas. | 1 | 1 | **1 – Bajo** | Una prueba unitaria sencilla. |

---

## 8. Matriz de trazabilidad (requisito → prueba)

| HU | Escenarios que la cubren (ver `escenarios.md`) |
|----|------------------------------------------------|
| HU-01 | EP-001, EP-011 |
| HU-02 | EP-002 |
| HU-03 | EP-010 |
| HU-04 | EP-003, EP-004, EP-005, EP-006 |
| HU-05 | EP-007, EP-012 |
| HU-06 | EP-008, EP-013 |
| HU-07 | EP-009, EP-014, EP-015 |

---

## 9. Métricas de cobertura esperadas

| Métrica | Meta |
|---------|------|
| Cobertura de líneas en servicios (JaCoCo) | ≥ 80 % |
| Cobertura de ramas (los `if`) en `PrestamoService` y `ReservaService` | ≥ 70 % |
| Endpoints con al menos una prueba de API | 100 % |
| Historias de usuario con al menos un escenario | 100 % |
| Pruebas que pasan antes de hacer *merge* a `develop` | 100 % |

El reporte de cobertura se genera solo al correr `mvn test` y queda en `target/site/jacoco/index.html`.

---

## 10. Entorno y herramientas

- Java 17, Spring Boot 3, Maven.
- JUnit 5, Mockito y MockMvc (vienen en `spring-boot-starter-test`).
- Base de datos H2 en memoria.
- JaCoCo para la cobertura y Surefire para los reportes de resultados.
- Postman para pruebas manuales de la API.
- GitHub: rama `feature/estrategia-pruebas` y Pull Request hacia `develop`.
