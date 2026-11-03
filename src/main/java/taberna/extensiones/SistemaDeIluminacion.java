package taberna.extensiones;

import static taberna.util.Validaciones.exigirNoNulo;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.BandaTocandoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Componente NUEVO (extensión): cambia el modo de las luces según la duración de la canción.
 */
public final class SistemaDeIluminacion implements Suscriptor<BandaTocandoEvent> {

    private static final int DURACION_MINIMA_DE_UNA_CANCION_EPICA_EN_SEGUNDOS = 300;
    private static final String ETIQUETA = "LUCES";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;

    public SistemaDeIluminacion(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
        this.busDeEventos = exigirNoNulo(busDeEventos, "busDeEventos");
        this.consola = exigirNoNulo(consola, "consola");
    }

    public void activar() {
        busDeEventos.suscribir(BandaTocandoEvent.class, this);
    }

    public void desactivar() {
        busDeEventos.desuscribir(BandaTocandoEvent.class, this);
    }

    @Override
    public void alRecibirEvento(BandaTocandoEvent bandaTocando) {
        String modoDeLuces = bandaTocando.duracionEnSegundos() >= DURACION_MINIMA_DE_UNA_CANCION_EPICA_EN_SEGUNDOS
                ? "estroboscópico"
                : "ambiente cálido";
        consola.imprimir(ColorAnsi.VERDE, ETIQUETA,
                "Luces en modo " + modoDeLuces + " para: " + bandaTocando.nombreDeLaCancion());
    }
}
