# Post-contenido — Refactorización Guiada por Pruebas: Code Smells, Paquetes y Acoplamiento en un Proyecto Java Real

## 1. Objetivo

El objetivo de este post-contenido es aplicar técnicas de refactorización sobre un proyecto Java utilizando pruebas automatizadas como red de seguridad, identificando y corrigiendo code smells y mejorando la organización arquitectónica del sistema.

El trabajo se desarrolla en dos partes. En la primera se refactorizan dos problemas de diseño presentes en el módulo de inventario: Long Method y Duplicate Switch Statements. En la segunda se analiza el acoplamiento entre los paquetes `inventario` y `reportes`, aplicando inversión de dependencias mediante una interfaz.

---

## 2. Parte 1. Identificación y refactorización de Code Smells

### 2.1. Code Smell 1: Long Method

**Clase:** `InventarioService.java`  
**Método:** `actualizarInventario()`

**Estado antes de la refactorización**

El método `actualizarInventario()` concentraba seis responsabilidades diferentes dentro de una sola operación:

1. Validación de los datos de entrada.
2. Búsqueda del producto en la colección.
3. Actualización y validación del stock.
4. Cálculo de la cantidad de reorden.
5. Registro del movimiento de inventario.
6. Generación de alertas cuando el producto presentaba bajo stock.

Esta concentración de responsabilidades dificultaba la lectura y el mantenimiento del método, debido a que diferentes operaciones del proceso estaban implementadas en un mismo bloque de código.

**Evidencia del código original:**

Archivo: `InventarioService.java`.  
Extensión del método compartido: **43 líneas**, contando comentarios y espacios en blanco.

**Técnica seleccionada: Extract Method (Extraer método)**

Se decidió aplicar la técnica Extract Method para separar las operaciones internas en métodos privados especializados, manteniendo `actualizarInventario()` como coordinador del proceso.

**Estado después de la refactorización**

Las responsabilidades quedaron distribuidas de la siguiente manera:

| Método | Responsabilidad |
|---|---|
| `actualizarInventario()` | Coordinar el proceso de actualización. |
| `datosEntradaValidos()` | Validar los datos recibidos. |
| `buscarProducto()` | Buscar el producto solicitado. |
| `calcularNuevoStock()` | Calcular las nuevas existencias. |
| `calcularCantidadReorden()` | Determinar las unidades necesarias para reordenar. |
| `registrarMovimiento()` | Registrar la operación realizada. |
| `generarRespuestaInventario()` | Generar la respuesta y la alerta correspondiente. |

Después de la refactorización, `actualizarInventario()` delega las operaciones específicas a métodos privados, mejorando la separación de responsabilidades y facilitando la comprensión del flujo principal.

**Comparación antes/después:**

| Indicador | Antes | Después |
|---|---:|---:|
| Líneas de `actualizarInventario()` | 43 | 34 |
| Métodos auxiliares extraídos | 0 | 6 |
| Líneas reducidas | — | 9 |
| Reducción porcentual | — | 20,9 % |

La reducción se calculó sobre los fragmentos del método compartidos, contando comentarios y líneas en blanco. Los números de línea absolutos del archivo dependen de la ubicación del método dentro de la clase.

### 2.2. Code Smell 2: Duplicate Switch Statements

**Clases involucradas:**
- `ClasificadorProducto.java`.
- `EtiquetadoProducto.java`.

**Estado antes de la refactorización**

Se identificó una duplicación estructural en dos clases que utilizaban estructuras `switch` sobre el mismo enum `TipoProducto`.

En `ClasificadorProducto`, el método `calcularRecargoEnvio()` evaluaba los cuatro tipos de producto para determinar el recargo correspondiente:

- LIBRO: 0.0.
- ELECTRONICO: 8 % del precio.
- ROPA: 3 % del precio.
- ALIMENTO: 2.50.

En `EtiquetadoProducto`, el método `generarEtiqueta()` contenía otro `switch` con los mismos cuatro tipos, pero para construir las etiquetas correspondientes.

**Evidencia del código original:** dos estructuras `switch` que evaluaban el mismo conjunto de valores en clases diferentes.

**Técnica seleccionada: Replace Conditional with Polymorphism (Reemplazar condicionales por polimorfismo)**

Se decidió convertir `TipoProducto` en un enum con métodos abstractos e implementaciones específicas para cada constante.

**Estado después de la refactorización**

El enum incorpora los siguientes comportamientos:

- `calcularRecargoEnvio(double precio)`.
- `generarEtiqueta(String nombre)`.

Cada constante implementa sus propias reglas.

`ClasificadorProducto` delega el cálculo del recargo en el enum, mientras que `EtiquetadoProducto` delega la generación de las etiquetas.

**Comparación antes/después:**

| Indicador | Antes | Después |
|---|---:|---:|
| Switch en `ClasificadorProducto` | 1 | 0 |
| Switch en `EtiquetadoProducto` | 1 | 0 |
| Total de switches duplicados | 2 | 0 |
| Tipos de producto | 4 | 4 |

La solución elimina la duplicación y permite agregar una nueva constante con sus comportamientos sin modificar las clases consumidoras.

---

## 3. Parte 2. Evolución arquitectónica y acoplamiento entre paquetes

### 3.1. Diagrama de paquetes antes de la refactorización

Inicialmente, la incorporación de `ReporteBajoStockService` introdujo una dependencia directa entre los paquetes `inventario` y `reportes`.

`InventarioService` importaba e inyectaba `ReporteBajoStockService`, mientras que `ReporteBajoStockService` recibía `InventarioService` mediante su constructor.

**Diagrama de dependencias antes:**

```text
       PAQUETE INVENTARIO
    +-------------------------+
    |                         |
    |   InventarioService     |
    |                         |
    +-------------------------+
               |
               | depende de
               v
       PAQUETE REPORTES
    +-------------------------+
    |                         |
    | ReporteBajoStockService |
    |                         |
    +-------------------------+
               |
               | depende de
               v
       PAQUETE INVENTARIO
```

**Problema identificado:**

Se generó una dependencia circular entre los dos paquetes:

- `InventarioService` dependía de `ReporteBajoStockService`.
- `ReporteBajoStockService` dependía de `InventarioService`.

Esta situación dificultaba la inicialización de los beans administrados por Spring y generaba acoplamiento entre las responsabilidades del inventario y los mecanismos de reportes.

### 3.2. Diagrama de paquetes después de la refactorización

Para invertir la dirección de las dependencias, se introdujo la interfaz `NotificadorBajoStock` dentro del paquete `inventario`.

Esta interfaz representa la necesidad del dominio de notificar cuando un producto presenta existencias inferiores al mínimo.

`InventarioService` utiliza dicha abstracción, mientras que `ReporteBajoStockService`, ubicado en el paquete `reportes`, implementa la interfaz.

**Diagrama de dependencias después:**

```text
       PAQUETE INVENTARIO
    +----------------------------+
    |                            |
    |    InventarioService       |
    |            |               |
    |            v               |
    |    NotificadorBajoStock    |
    |       <<interface>>        |
    |                            |
    +----------------------------+
                 ^
                 |
                 | implements
                 |
       PAQUETE REPORTES
    +----------------------------+
    |                            |
    |   ReporteBajoStockService  |
    |                            |
    +----------------------------+
```

**Resultado arquitectónico:**

- `NotificadorBajoStock` se encuentra en el paquete `inventario`.
- `InventarioService` depende de la interfaz y no de una implementación concreta.
- `ReporteBajoStockService` implementa la interfaz.
- El paquete `reportes` depende de `inventario`.
- `inventario` ya no necesita importar `ReporteBajoStockService`.
- Se elimina la dependencia circular entre los servicios.

La nueva organización respeta la regla de inversión de dependencias: los paquetes de infraestructura dependen del dominio, pero el dominio no depende de la infraestructura.

---

## 4. Decisiones de diseño justificadas

### Decisión de diseño 1: Extracción del cálculo de la cantidad de reorden

Se decidió extraer el cálculo de la cantidad de reorden como un método privado dentro de la clase `InventarioService`, en lugar de crear una clase colaboradora independiente como `CalculadoraReorden`.

Esta decisión se fundamenta en que, para el alcance actual del sistema, el cálculo forma parte del proceso de actualización del inventario y no se identifica una razón de cambio independiente que justifique asignarle una clase propia. Además, al tratarse de una operación sencilla y específica del servicio, mantenerla como un método privado permite reducir la complejidad de `actualizarInventario()` sin aumentar innecesariamente el número de clases ni introducir una abstracción adicional.

La extracción permite separar las responsabilidades internas del método: `actualizarInventario()` conserva la coordinación del proceso, mientras que el nuevo método privado se encarga exclusivamente de calcular la cantidad de reorden. Si en el futuro esta lógica incorpora reglas más complejas, necesita reutilizarse desde otros componentes o evoluciona por motivos independientes, se podrá evaluar su traslado a una clase colaboradora.

La refactorización se realiza de manera incremental, conservando el comportamiento original. Después de cada cambio pequeño se ejecuta `mvn test` para comprobar que las pruebas continúan pasando y que la red de seguridad permanece en verde.

### Decisión de diseño 2: Sustitución de condicionales mediante polimorfismo

Se decidió convertir `TipoProducto` en un enum con métodos abstractos e implementaciones específicas para cada constante, en lugar de extraer una interfaz de estrategia con una clase independiente por cada tipo de producto.

Esta elección se fundamenta en que el dominio establece un conjunto cerrado y estable de tipos de producto. Por tanto, encapsular los comportamientos de clasificación y etiquetado directamente en las constantes del enum permite centralizar las reglas, eliminar la duplicación de los bloques `switch` y reducir la cantidad de clases necesarias.

Con esta solución, `ClasificadorProducto` y `EtiquetadoProducto` delegan sus operaciones en `TipoProducto`, sin utilizar estructuras `switch` ni cadenas de condicionales `if-else` para identificar el tipo de producto. Cuando se incorpore un nuevo tipo, bastará con agregar una constante que implemente los comportamientos correspondientes, sin modificar las dos clases consumidoras.

También se reconoce una limitación de esta decisión: las constantes de un enum no permiten aprovechar directamente la inyección de dependencias de Spring. Si en el futuro cada tipo de producto necesitara colaboradores externos, servicios o dependencias administradas por el contenedor, sería más apropiado migrar a una interfaz de estrategia con implementaciones independientes registradas como beans.

Para el alcance actual, esa estructura adicional no se considera necesaria. La decisión prioriza la simplicidad y la correspondencia con la naturaleza cerrada del dominio.

### Decisión de diseño 3: Ubicación de NotificadorBajoStock

Se decidió ubicar la interfaz `NotificadorBajoStock` en el paquete `inventario` y no en `reportes`, siguiendo el principio establecido en la Guía de Contenido, sección 4.3: los paquetes de infraestructura dependen del dominio, mientras que el dominio nunca debe depender de la infraestructura.

En este proyecto, `inventario` representa el paquete de mayor nivel de abstracción, porque contiene las reglas y responsabilidades centrales del negocio, como la actualización de existencias, la identificación de productos con bajo stock y la determinación de cuándo es necesario generar una notificación. Por su parte, `reportes` representa un nivel inferior de abstracción, ya que se encarga de implementar los mecanismos concretos para generar y presentar la información.

La interfaz `NotificadorBajoStock` pertenece al dominio porque expresa una necesidad del negocio: notificar cuando un producto se encuentra por debajo de su stock mínimo. Sin embargo, el dominio no necesita conocer cómo se realiza esa notificación. Por esta razón, la interfaz se define en `inventario` y su implementación concreta se ubica en `reportes`.

De esta manera, `InventarioService` depende de una abstracción y no de una clase concreta de reportes. El paquete `reportes` implementa dicha abstracción y depende de la definición proporcionada por `inventario`, respetando la dirección de las dependencias.

Esta decisión evita que el dominio quede condicionado por detalles de infraestructura, reduce el acoplamiento entre paquetes y facilita sustituir o ampliar los mecanismos de notificación sin modificar las reglas centrales del inventario.

### Decisión de diseño 4: Granularidad de la notificación de bajo stock

Se decidió implementar el método `notificar(Producto producto, int cantidadReorden)` mediante notificaciones por evento individual, en lugar de utilizar una notificación por lote, como `notificar(List<Producto> productos)`.

Esta elección se fundamenta en que el proceso de actualización del inventario detecta las situaciones de bajo stock de manera individual. Cuando un producto queda por debajo de su stock mínimo, `InventarioService` comunica inmediatamente el producto afectado y la cantidad específica que necesita reordenarse. De esta manera, cada notificación representa un evento concreto del negocio y conserva la información necesaria para identificar la situación que lo originó.

Una ventaja de esta granularidad es que `InventarioService` no necesita reunir una colección de productos afectados ni conocer cuándo debe generarse un reporte general. Simplemente comunica cada evento mediante la interfaz `NotificadorBajoStock`. Por su parte, `ReporteBajoStockService` recibe las notificaciones individuales, almacena las alertas pendientes y posteriormente puede agruparlas cuando se solicita la generación del reporte.

La notificación por lote sería preferible en escenarios donde se procesan grandes cantidades de productos simultáneamente, por ejemplo, durante una importación masiva de inventario, una revisión nocturna de existencias o una auditoría general. En esas situaciones, reunir los productos que presentan bajo stock y procesarlos conjuntamente permitiría reducir las llamadas al mecanismo de notificación y generar un reporte consolidado.

Para el alcance actual se prioriza la notificación por evento individual porque corresponde al flujo de actualización del inventario y permite mantener separadas las responsabilidades: `InventarioService` detecta y comunica cada situación, mientras que `ReporteBajoStockService` administra las alertas recibidas y construye el reporte cuando se solicita.

---

## 5. Evidencia de pruebas de seguridad

Durante el proceso se utilizó el comando `mvn test` para comprobar que las refactorizaciones conservaran el comportamiento existente.

Las pruebas se ejecutaron de manera incremental, después de las modificaciones realizadas en cada etapa.

### 5.1. Resultados de la Parte 1

La suite de pruebas de la Parte 1 contiene 12 pruebas automatizadas.

**Resultado de la Parte 1, después de las refactorizaciones:**

| Indicador | Resultado |
|---|---:|
| Pruebas ejecutadas | 12 |
| Pruebas correctas | 12 |
| Fallos | 0 |
| Errores | 0 |
| Pruebas omitidas | 0 |
| Estado | BUILD SUCCESS |

Las 12 pruebas permiten verificar el comportamiento del inventario y la conservación de las reglas existentes después de aplicar Extract Method y sustituir los `switch` duplicados mediante polimorfismo.

### 5.2. Integración inicial de reportes

En el primer intento de integración de `ReporteBajoStockService` se ejecutaron las 12 pruebas existentes y se agregó una prueba de contexto de Spring.

El resultado registrado fue:

| Indicador | Resultado |
|---|---:|
| Pruebas ejecutadas | 13 |
| Pruebas correctas | 12 |
| Fallos | 0 |
| Errores | 1 |
| Pruebas omitidas | 0 |
| Estado | BUILD FAILURE |

El error se produjo en `RefactoringU11ApplicationTests.contextLoads`, porque Spring detectó una dependencia circular entre `InventarioService` y `ReporteBajoStockService`.

La causa se identificó en la inyección bidireccional entre los servicios. Para corregir la arquitectura se introdujo la interfaz `NotificadorBajoStock` en el paquete `inventario`, se hizo que `ReporteBajoStockService` la implementara y se eliminó su dependencia directa de `InventarioService`.

### 5.3. Resultado esperado después de la inversión de dependencias

La Parte 2 mantiene las 12 pruebas existentes y agrega una prueba adicional, para un total de 13 pruebas automatizadas.

**Distribución de las pruebas:**

| Parte | Pruebas |
|---|---:|
| Parte 1: pruebas existentes | 12 |
| Parte 2: prueba adicional | 1 |
| Total | 13 |

La verificación final se realiza mediante:

```bash
mvn clean test
```

El criterio de aceptación es obtener 13 pruebas ejecutadas, 0 fallos y 0 errores, con resultado `BUILD SUCCESS`.

---

## 6. Conclusión general

El trabajo permitió identificar y corregir dos code smells en la Parte 1: Long Method y Duplicate Switch Statements. Para el primero se aplicó Extract Method, distribuyendo las operaciones de actualización del inventario entre métodos privados especializados. Para el segundo se utilizó polimorfismo mediante un enum con métodos abstractos, centralizando los comportamientos de clasificación y etiquetado.

En la Parte 2 se analizó el acoplamiento introducido entre los paquetes `inventario` y `reportes`. La incorporación de la interfaz `NotificadorBajoStock` permitió invertir la dirección de las dependencias, dejando la abstracción en el paquete de dominio y la implementación concreta en el paquete de reportes.

Las cuatro decisiones de diseño priorizaron la simplicidad estructural, la separación de responsabilidades, el nivel de abstracción de cada paquete y la granularidad adecuada para comunicar los eventos de bajo stock.

En conjunto, las modificaciones buscan mejorar la mantenibilidad y extensibilidad del sistema, conservando las reglas de negocio existentes y permitiendo incorporar nuevas funcionalidades con menor impacto sobre el código previamente desarrollado.