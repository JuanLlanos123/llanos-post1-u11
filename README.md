### Decisión de diseño 1: Extracción del cálculo de la cantidad de reorden

Se decidió extraer el cálculo de la cantidad de reorden como un método privado dentro de la clase `InventarioService`, en lugar de crear una clase colaboradora independiente como `CalculadoraReorden`.

Esta decisión se fundamenta en que, para el alcance actual del sistema, el cálculo forma parte del proceso de actualización del inventario y no se identifica una razón de cambio independiente que justifique asignarle una clase propia. Además, al tratarse de una operación sencilla y específica del servicio, mantenerla como método privado permite reducir la complejidad de `actualizarInventario()` sin aumentar innecesariamente el número de clases ni introducir una abstracción adicional.

La extracción permite separar las responsabilidades internas del método: `actualizarInventario()` conserva la coordinación del proceso, mientras que el nuevo método privado se encarga exclusivamente de calcular la cantidad de reorden. Si en el futuro esta lógica incorpora reglas más complejas, necesita reutilizarse desde otros componentes o evoluciona por motivos independientes, se podrá evaluar su traslado a una clase colaboradora.

La refactorización se realizará de manera incremental, conservando el comportamiento original. Después de cada cambio pequeño se ejecutará `mvn test` para comprobar que las pruebas continúan pasando y que la red de seguridad permanece en verde.


### Decisión de diseño 2: Sustitución de condicionales mediante polimorfismo

Se decidió convertir `TipoProducto` en un enum con métodos abstractos e implementaciones específicas para cada constante, en lugar de extraer una interfaz de estrategia con una clase independiente por cada tipo de producto.

Esta elección se fundamenta en que el dominio establece un conjunto cerrado y estable de tipos de producto. Por tanto, encapsular los comportamientos de clasificación y etiquetado directamente en las constantes del enum permite centralizar las reglas, eliminar la duplicación de los bloques `switch` y reducir la cantidad de clases necesarias.

Con esta solución, `ClasificadorProducto` y `EtiquetadoProducto` delegan sus operaciones en `TipoProducto`, sin utilizar estructuras `switch` ni cadenas de condicionales `if-else` para identificar el tipo de producto. Cuando se incorpore un nuevo tipo, bastará con agregar una constante que implemente los comportamientos correspondientes, sin modificar las dos clases consumidoras.

También se reconoce una limitación de esta decisión: las constantes de un enum no permiten aprovechar directamente la inyección de dependencias de Spring. Si en el futuro cada tipo de producto necesitara colaboradores externos, servicios o dependencias administradas por el contenedor, sería más apropiado migrar a una interfaz de estrategia con implementaciones independientes registradas como beans. Para el alcance actual, esa estructura adicional no se considera necesaria.

La decisión prioriza la simplicidad y la correspondencia con la naturaleza cerrada del dominio, manteniendo la posibilidad de evolucionar hacia estrategias independientes si cambian los requisitos.