# Validación · Fold Glass 0.3.0

Fecha: 10 de septiembre de 2026.

## Compilación y paquete

- `:app:assembleDebug :app:lintDebug`: compilación correcta.
- Android Lint: 0 incidencias en la versión final.
- Firma de la APK verificada con `apksigner`; mismo certificado que 0.2.
- Manifiesto comprobado con `aapt`: `studio.foldglass`, versión 0.3.0 / versionCode 3, minSdk 33, target/compile SDK 35.
- JDK 17, Gradle 8.11.1 y AGP 8.9.2, con las herramientas portátiles previamente verificadas.
- El compilador avisa del uso de una API obsoleta de ventana para compatibilidad con Android 13/14. El SDK portátil carece de Platform Tools; esto no impidió compilar.

APK entregada: `FoldGlass-0.3-debug.apk`.

SHA-256:

```text
d32b4e5b395a6dff1b3dca7f40ad1bd958b247097c7bdc434973f0bbee3dc7c3
```

## Pruebas ejecutadas

`GlassMathTest` y `OpeningMotionTest` ejecutados con Java 17:

- Extremos, continuidad decimal, límites y suavizado del ángulo.
- Primer cuadro con intensidad de cristal completa aunque el primer evento indique 180°.
- Revelado todavía visible a los 950 y 1.400 ms y terminado después de 1.800 ms.
- Reinicio al reaparecer la pantalla, aun sin nuevos eventos del sensor.
- Ausencia total de sensor: la reproducción visual funciona y no inventa lecturas físicas.
- Postura parcial legible después del periodo de reposo extendido.
- Apertura lenta con incrementos de 0,1° sin confundirla con reposo.
- Eventos NaN/infinito ignorados.
- Salida finita y desvanecimiento monótono a 30, 60 y 120 Hz.
- El controlador deja de pedir cuadros cuando termina la transición y no hay movimiento.

Se revisó que la actividad use directamente `GlassView` a pantalla completa, sin el contenedor de controles de versiones anteriores, y que la escena se inicialice antes de adjuntarla a la ventana.

## Lo que aún no se ha verificado

**La APK 0.3 no se ha instalado ni ejecutado en un teléfono o emulador durante este desarrollo.** Las pruebas anteriores ejercitan el controlador Java; no ejecutan callbacks reales de Android, gestos, compilación/renderizado AGSL en una GPU ni las reglas de encendido de pantallas de Samsung.

El usuario informó que la versión previa aparecía nítida al abrir. El código 0.3 corrige rutas que podían producir ese resultado, pero no se dispone de registros de su dispositivo para confirmar la causa ni de una prueba posterior que confirme la corrección.

El revelado temporal no garantiza continuidad óptica entre dos paneles ni demuestra que el fabricante permita mantenerlos activos simultáneamente. El diagnóstico oculto está incluido para verificar sensor y renderizador en hardware sin restaurar un menú permanente.
