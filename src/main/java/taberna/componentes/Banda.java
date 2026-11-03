package taberna.componentes;

import static taberna.util.Validaciones.exigirNoNulo;
import static taberna.util.Validaciones.exigirPositivo;
import static taberna.util.Validaciones.exigirTextoNoVacio;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import taberna.bus.BusDeEventos;
import taberna.eventos.BandaTocandoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Publicador: cada cierto tiempo "toca" la siguiente canción de su repertorio y
 * publica un {@link BandaTocandoEvent}. No conoce al sistema de sonido, al panel
 * ni al sistema de humo: solo conoce el bus.
 */
public final class Banda {

    /** Canción del repertorio (inmutable). */
    public record Cancion(String nombre, int duracionEnSegundos) {
        public Cancion {
            exigirTextoNoVacio(nombre, "nombre");
            exigirPositivo(duracionEnSegundos, "duracionEnSegundos");
        }
    }

    private static final String ETIQUETA = "BANDA";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;
    private final ScheduledExecutorService planificadorDeTareas;
    private final String nombreDeLaBanda;
    private final List<Cancion> repertorio;
    private final AtomicInteger posicionDeLaSiguienteCancion = new AtomicInteger(0);
    private volatile ScheduledFuture<?> actuacionProgramada;

    public Banda(BusDeEventos busDeEventos,
                 ConsolaDeLaTaberna consola,
                 ScheduledExecutorService planificadorDeTareas,
                 String nombreDeLaBanda,
                 List<Cancion> repertorio) {
        this.busDeEventos = exigirNoNulo(busDeEventos, "busDeEventos");
        this.consola = exigirNoNulo(consola, "consola");
        this.planificadorDeTareas = exigirNoNulo(planificadorDeTareas, "planificadorDeTareas");
        this.nombreDeLaBanda = exigirTextoNoVacio(nombreDeLaBanda, "nombreDeLaBanda");
        exigirNoNulo(repertorio, "repertorio");
        if (repertorio.isEmpty()) {
            throw new IllegalArgumentException("El repertorio de la banda no puede estar vacío");
        }
        this.repertorio = List.copyOf(repertorio);
    }

    /** Empieza a tocar una canción cada {@code segundosEntreCanciones} segundos. */
    public void iniciarActuacion(long retardoInicialEnSegundos, long segundosEntreCanciones) {
        actuacionProgramada = planificadorDeTareas.scheduleWithFixedDelay(
                this::tocarSiguienteCancion,
                retardoInicialEnSegundos,
                segundosEntreCanciones,
                TimeUnit.SECONDS);
    }

    public void detenerActuacion() {
        ScheduledFuture<?> actuacion = actuacionProgramada;
        if (actuacion != null) {
            actuacion.cancel(false);
        }
    }

    private void tocarSiguienteCancion() {
        int posicion = posicionDeLaSiguienteCancion.getAndIncrement() % repertorio.size();
        Cancion cancion = repertorio.get(posicion);
        consola.imprimir(ColorAnsi.ROJO, ETIQUETA,
                "Empieza a tocar \"" + cancion.nombre() + "\" (" + nombreDeLaBanda + ")");
        busDeEventos.publicar(new BandaTocandoEvent(nombreDeLaBanda, cancion.nombre(), cancion.duracionEnSegundos()));
    }
}
