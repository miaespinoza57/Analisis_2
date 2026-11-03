package taberna.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fábrica de hilos con nombre legible (útil al depurar) y marcados como daemon.
 */
public final class HilosNombrados {

    private HilosNombrados() {
        // Clase utilitaria: no se instancia.
    }

    public static ThreadFactory conPrefijo(String prefijoDelNombre) {
        AtomicInteger contadorDeHilos = new AtomicInteger(1);
        return tarea -> {
            Thread hilo = new Thread(tarea, prefijoDelNombre + "-" + contadorDeHilos.getAndIncrement());
            hilo.setDaemon(true);
            return hilo;
        };
    }
}
