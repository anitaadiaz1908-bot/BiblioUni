# Evaluación de características de calidad ISO/IEC 25010 – BiblioUni

La norma ISO/IEC 25010 define un modelo de calidad del producto de software. En este documento reviso sus 8 características, qué tan importante es cada una para BiblioUni, cómo se podría medir y con qué técnica de prueba se va a revisar.

> **Nota sobre la versión de la norma:** la actividad usa las 8 características de la versión 2011. En la versión vigente (ISO/IEC 25010:2023) se agregó una novena característica, *Protección* (safety), y algunas cambiaron de nombre: Usabilidad pasó a llamarse *Capacidad de interacción* y Portabilidad a *Flexibilidad*. Protección no se evalúa aquí porque BiblioUni no maneja nada que pueda causar daño físico a personas.

---

## 1. Tabla de evaluación

| # | Característica | Definición (con mis palabras) | Subcaracterísticas relevantes | Relevancia | Justificación para BiblioUni | Ejemplo de cómo medirla (métrica y umbral) | Técnica de prueba |
|---|---|---|---|:---:|---|---|---|
| 1 | **Adecuación funcional** (Funcionalidad) | Que el sistema haga lo que tiene que hacer y lo haga bien. | Completitud, corrección, pertinencia funcional | **Alta** | Es el corazón del sistema: si se presta un libro ya prestado o no se respeta el límite de 3 préstamos, la biblioteca no sirve. | % de historias de usuario con todos sus escenarios aprobados. **Meta: 100 %.** | Caja negra: clases de equivalencia y valores límite (pruebas unitarias y funcionales). |
| 2 | **Eficiencia de desempeño** (Rendimiento) | Que responda rápido y sin gastar recursos de más. | Comportamiento en el tiempo, uso de recursos | **Baja** | Pocos usuarios a la vez y base de datos en memoria. No es crítico en esta etapa del proyecto. | Tiempo de respuesta de `GET /api/materiales` con 500 registros. **Meta: < 1 s.** | Medición sencilla del tiempo con Postman o MockMvc (EP-016). |
| 3 | **Compatibilidad** | Que pueda trabajar junto a otros sistemas y compartir información. | Interoperabilidad, coexistencia | **Media** | La API usa JSON y HTTP, que es lo que usaría una futura app móvil o página web de la universidad. | % de endpoints que devuelven JSON válido con el `Content-Type` correcto. **Meta: 100 %.** | Pruebas de API: revisar formato y encabezados de cada respuesta. |
| 4 | **Usabilidad** (Capacidad de interacción) | Que sea fácil de entender y usar, y que ayude cuando el usuario se equivoca. | Protección contra errores del usuario, facilidad de aprendizaje | **Media** | Aunque no hay pantallas, los mensajes de error de la API son los que vería el estudiante. Deben ser claros y en español. | % de errores que devuelven un mensaje claro en vez de un error 500. **Meta: 100 %.** | Basada en la experiencia: provocar errores comunes y revisar el mensaje (EP-017). |
| 5 | **Fiabilidad** | Que funcione bien siempre, sin caerse y sin dañar los datos. | Madurez, disponibilidad, tolerancia a fallos | **Alta** | Si un error deja un libro "prestado" sin préstamo, o la lista de espera se desordena, se pierde la confianza en el sistema. | N.º de defectos de prioridad alta encontrados en pruebas de integración. **Meta: 0 abiertos al cerrar cada nivel.** | Pruebas de integración con la BD y pruebas de regresión con `mvn test` en cada cambio. |
| 6 | **Seguridad** | Que proteja los datos y solo deje hacer lo permitido. | Integridad, confidencialidad | **Media** | Se guardan nombres y correos de estudiantes. Por ahora no hay inicio de sesión, pero sí hay que evitar datos inválidos o que se dañe la información. | % de entradas inválidas rechazadas (correo sin @, campos vacíos, ids inexistentes). **Meta: 100 %.** | Caja negra con datos inválidos (EP-006, EP-010, EP-011). |
| 7 | **Mantenibilidad** | Que el código sea fácil de entender, cambiar y probar. | Modularidad, capacidad de ser probado, capacidad de ser modificado | **Alta** | El proyecto se va a seguir cambiando en las Unidades 2 y 3. Si el código está desordenado, cada cambio dañaría algo. Las capas (modelo, servicio, controlador) ayudan. | Cobertura de líneas en servicios con JaCoCo. **Meta: ≥ 80 %.** | Caja blanca: cobertura de sentencias y ramas con JaCoCo. |
| 8 | **Portabilidad** (Flexibilidad) | Que se pueda instalar y ejecutar en distintos equipos o ambientes. | Adaptabilidad, facilidad de instalación | **Baja** | Al ser Java con Maven funciona en Windows, Linux o Mac. Solo se necesita que corra en los equipos del equipo y del profesor. | Tiempo para clonar el repositorio y ejecutar `mvn test` en un equipo nuevo siguiendo el README. **Meta: < 15 min.** | Prueba manual de instalación en otro computador. |

---

## 2. Matriz de criticidad

Para escoger las más críticas se calificó cada característica de 1 a 3 en tres criterios: impacto si falla, probabilidad de que falle y cuánto se usa en el día a día.

| Característica | Impacto | Probabilidad | Uso | **Total** | Prioridad |
|---|:---:|:---:|:---:|:---:|:---:|
| Adecuación funcional | 3 | 3 | 3 | **9** | 1 |
| Fiabilidad | 3 | 2 | 3 | **8** | 2 |
| Mantenibilidad | 2 | 3 | 2 | **7** | 3 |
| Seguridad | 3 | 1 | 2 | 6 | 4 |
| Usabilidad | 2 | 2 | 2 | 6 | 5 |
| Compatibilidad | 2 | 1 | 2 | 5 | 6 |
| Eficiencia de desempeño | 1 | 1 | 2 | 4 | 7 |
| Portabilidad | 1 | 1 | 1 | 3 | 8 |

*(Seguridad y Usabilidad empatan en 6; se dejó primero Seguridad porque su impacto es mayor.)*

---

## 3. Las 3 características más críticas y cómo se abordarán

### 3.1 Adecuación funcional
Es la más importante porque BiblioUni existe para prestar materiales siguiendo unas reglas. Si las reglas fallan, lo demás no importa.

**Cómo se va a probar:**
- Pruebas unitarias de `PrestamoService` y `ReservaService` con Mockito para cada regla (límite de 3 préstamos, material no disponible, apartado de 48 h).
- Clases de equivalencia y valores límite (2, 3 y 4 préstamos; 47 h 59 min y 48 h 01 min).
- Matriz de trazabilidad para asegurar que cada historia de usuario tenga al menos un escenario (ver `estrategia.md`).

### 3.2 Fiabilidad
Los préstamos y la lista de espera cambian varios datos al mismo tiempo (préstamo, material y reserva). Si uno se guarda y el otro no, la información queda inconsistente.

**Cómo se va a probar:**
- Pruebas de integración con `@SpringBootTest` y H2 que, después de cada acción, consulten la base de datos para revisar que todo quedó bien guardado.
- Pruebas de los caminos tristes para comprobar que, cuando hay un error, no se guarda nada a medias.
- Correr todas las pruebas con `mvn test` antes de cada Pull Request (regresión).

### 3.3 Mantenibilidad
Como el proyecto va a crecer en las siguientes unidades, el código tiene que ser fácil de probar y modificar.

**Cómo se va a probar:**
- Medir la cobertura con JaCoCo (meta ≥ 80 % en servicios).
- Mantener la carpeta `src/test/java` con la misma estructura de paquetes que el código principal (`servicio/`, `controlador/`).
- Revisar en cada Pull Request que el código nuevo tenga sus pruebas.

---

## 4. Trade-offs (lo que se gana y se pierde entre características)

- **Seguridad vs. Usabilidad:** validar mucho los datos hace el sistema más seguro, pero si los mensajes no son claros el estudiante no entiende qué hizo mal. Por eso cada validación tiene un mensaje en español.
- **Fiabilidad vs. Eficiencia de desempeño:** guardar varios cambios dentro de una transacción (`@Transactional`) hace el sistema más fiable, pero un poco más lento. Como el rendimiento tiene prioridad baja, se prefirió la fiabilidad.
- **Mantenibilidad vs. tiempo de desarrollo:** escribir pruebas y separar el código en capas toma más tiempo al inicio, pero ahorra tiempo cuando hay que hacer cambios después.

---

## 5. Referencias

- ISO/IEC 25010:2023. *Systems and software engineering — Systems and software Quality Requirements and Evaluation (SQuaRE) — Product quality model.* International Organization for Standardization.
- ISO/IEC 25010:2011. *Systems and software engineering — SQuaRE — System and software quality models.*
- ISTQB. (2023). *Certified Tester Foundation Level (CTFL) Syllabus, versión 4.0.*
