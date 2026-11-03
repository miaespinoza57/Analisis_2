package taberna.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Salida de consola sincronizada: garantiza que las líneas impresas desde
 * distintos hilos (Banda, Barra, Cocina...) nunca se mezclen entre sí.
 * Cada línea lleva una marca de tiempo relativa al inicio de la simulación,
 * por ejemplo {@code [00:02.0]}.
 */
public final class ConsolaDeLaTaberna {

    private static final int ANCHO_DE_LA_ETIQUETA = 14;
    private static final int MILISEGUNDOS_POR_SEGUNDO = 1_000;
    private static final int MILISEGUNDOS_POR_DECIMA = 100;
    private static final int SEGUNDOS_POR_MINUTO = 60;
    private static final int NANOSEGUNDOS_POR_MILISEGUNDO = 1_000_000;

    private final PrintStream salida;
    private final long instanteDeInicioEnNanosegundos;

    public ConsolaDeLaTaberna() {
        this(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
    }

    public ConsolaDeLaTaberna(PrintStream salida) {
        this.salida = Validaciones.exigirNoNulo(salida, "salida");
        this.instanteDeInicioEnNanosegundos = System.nanoTime();
    }

    /** Imprime una línea con marca de tiempo, etiqueta coloreada del componente y mensaje. */
    public synchronized void imprimir(ColorAnsi color, String etiquetaDelComponente, String mensaje) {
        String etiqueta = String.format("%-" + ANCHO_DE_LA_ETIQUETA + "s", "[" + etiquetaDelComponente + "]");
        salida.println(ColorAnsi.GRIS.aplicarA(marcaDeTiempoRelativa()) + " "
                + color.aplicarA(etiqueta) + " " + mensaje);
    }

    /** Imprime un encabezado para separar las fases de la simulación. */
    public synchronized void imprimirSeparador(String titulo) {
        salida.println();
        salida.println(ColorAnsi.AMARILLO.aplicarA("=== " + titulo + " ==="));
    }

    private String marcaDeTiempoRelativa() {
        long milisegundosTranscurridos =
                (System.nanoTime() - instanteDeInicioEnNanosegundos) / NANOSEGUNDOS_POR_MILISEGUNDO;
        long segundosTotales = milisegundosTranscurridos / MILISEGUNDOS_POR_SEGUNDO;
        long decimasDeSegundo = (milisegundosTranscurridos % MILISEGUNDOS_POR_SEGUNDO) / MILISEGUNDOS_POR_DECIMA;
        return String.format("[%02d:%02d.%d]",
                segundosTotales / SEGUNDOS_POR_MINUTO,
                segundosTotales % SEGUNDOS_POR_MINUTO,
                decimasDeSegundo);
    }
}
