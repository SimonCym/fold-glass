# Fold Glass · Android demo 0.3

Demo nativa a pantalla completa. La escena ocupa toda la ventana: sin título, menú inferior ni deslizador. El fondo y los iconos son ilustrativos; no es un launcher ni modifica One UI.

## Instalar y usar

1. Copia `FoldGlass-0.3-debug.apk` al teléfono Android 13 o superior y ábrela. Conserva el certificado de 0.2 para permitir actualizarla.
2. Abre **Fold Glass**: empieza un revelado de cristal de 1,8 segundos desde la primera imagen visible.
3. Abre y cierra el teléfono con la app en primer plano. El sensor se registra automáticamente si está disponible.
4. **Doble toque:** repetir el revelado sin plegar el teléfono.
5. **Pulsación larga:** abrir el diagnóstico y, si hace falta, copiarlo. No hay controles permanentes.
6. Desliza desde un borde para recuperar temporalmente las barras de navegación del sistema y salir normalmente.

## Qué cambió

La versión anterior dependía de recibir ángulos intermedios mientras su ventana era visible. Además, eliminaba el cristal después de 650–950 ms de reposo. Si Android mostraba la pantalla interior después de esos eventos, o enviaba primero 180°, podía aparecer directamente nítida.

La versión 0.3 separa dos entradas:

- **Movimiento físico:** lecturas decimales del sensor, suavizadas y reversibles. A media apertura el efecto se conserva 2,5 segundos de reposo y desaparece suavemente durante el siguiente segundo.
- **Aparición de la pantalla:** un revelado visual de 1,8 segundos al iniciar/reanudar la app, cambiar entre exterior e interior o encenderse la pantalla principal. Se inicializa antes de mostrar la escena y se reinicia cuando la ventana obtiene su primera visibilidad. Funciona aunque la primera lectura sea ya 180° o no exista sensor.

El revelado temporal es una animación de presentación, **no una reconstrucción del ángulo físico que el sistema no haya entregado**. Se mezcla con el efecto del sensor cuando ambos están activos.

El shader tiene desenfoque, refracción, profundidad y reflejos más visibles. Si Android rechaza su compilación, se usa un desenfoque alternativo con `RenderEffect`; el diagnóstico indica ese caso. Las pantallas adicionales aptas para `Presentation` se intentan abrir automáticamente, sin encenderlas por la fuerza.

## Comprobación en el Fold

- Al abrir la app debe verse el revelado antes de quedar nítida, incluso si el teléfono ya está abierto.
- Un doble toque debe repetir ese efecto. Esto permite comprobar el dibujo sin depender de la bisagra.
- Al pasar de la pantalla exterior a la interior debe comenzar un nuevo revelado.
- Al abrir lentamente, las lecturas de la bisagra deben afectar también al efecto.
- Si el doble toque funciona pero la apertura física no, mantén pulsado y copia el diagnóstico después de abrir el teléfono. Incluye número de lecturas, último ángulo recibido, antigüedad, tamaño de la superficie, pantallas adicionales y renderizador.

## Compilar

Abre la carpeta en Android Studio con JDK 17 y SDK 35. También puedes ejecutar `gradlew.bat :app:assembleDebug :app:lintDebug` en Windows o `sh gradlew :app:assembleDebug :app:lintDebug` en macOS/Linux.

Versiones fijadas: AGP 8.9.2, Gradle 8.11.1, minSdk 33, compile/target SDK 35. La APK se genera en `app/build/outputs/apk/debug/app-debug.apk`. El paquete fuente no incluye herramientas, cachés, SDK ni claves de firma.

## Pruebas del controlador

Con JDK 17, desde esta carpeta:

```text
javac -encoding UTF-8 -d build/math-tests app/src/main/java/studio/foldglass/GlassMath.java app/src/main/java/studio/foldglass/OpeningMotion.java tests/GlassMathTest.java tests/OpeningMotionTest.java
java -cp build/math-tests studio.foldglass.GlassMathTest
java -cp build/math-tests studio.foldglass.OpeningMotionTest
```

Se comprueban la primera imagen con cristal, lectura inicial tardía de 180°, ausencia de sensor, reanudación, reposo en modo Flex, apertura lenta, valores inválidos y evolución a 30/60/120 Hz.

## Alcance de la validación

Se compiló y verificó la APK, y se ejecutaron las pruebas del controlador. **Esta versión no se ha ejecutado en un Fold ni en un emulador durante su desarrollo.** El informe previo del usuario corresponde a la versión anterior; la corrección en su dispositivo queda pendiente de comprobar. El detalle está en `VALIDATION.md`.

La app no solicita Internet, capturas, accesibilidad, superposiciones ni root. No tiene acceso a las superficies de otras aplicaciones y no puede garantizar ambas pantallas encendidas simultáneamente. La clasificación de pantalla exterior/interior usa un umbral de 600 dp. Las proporciones y alineación óptica todavía requieren calibración en hardware.

## Referencias

- [Post original](https://www.reddit.com/r/GalaxyFold/comments/1wcacld/tried_to_recreate_the_iphone_duo_animation_on_my/)
- [Sensor de bisagra](https://developer.android.com/reference/android/hardware/Sensor#TYPE_HINGE_ANGLE)
- [AGSL](https://developer.android.com/develop/ui/views/graphics/agsl)
- [Modo inmersivo](https://developer.android.com/develop/ui/views/layout/immersive)
- [Presentation](https://developer.android.com/reference/android/app/Presentation)
