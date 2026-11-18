# CentroTareas

## Observación del Paso 1

Al pulsar **Tarea larga (MAL)** la pantalla se congela durante 4 segundos: el botón **Tócame** deja de sumar, ningún botón responde y la interfaz no se vuelve a dibujar hasta que termina la espera.

El texto **Trabajando...** nunca llega a verse porque `Thread.sleep(4000)` se ejecuta en el hilo principal, que es el mismo que dibuja la pantalla. El valor del mensaje sí cambia a «Trabajando...», pero el hilo principal queda bloqueado y no puede redibujar la interfaz. Cuando por fin se libera, el mensaje ya cambió a «Terminó», así que solo se ve el resultado final.

Si la espera fuera de 10 segundos, Android detectaría que la app no responde a los toques durante más de 5 segundos y mostraría el diálogo **ANR (Application Not Responding)**: «CentroTareas no responde», con las opciones de esperar o cerrar la app.
