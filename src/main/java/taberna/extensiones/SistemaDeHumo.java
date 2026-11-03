package taberna.extensiones;

import static taberna.util.Validaciones.exigirNoNulo;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.BandaTocandoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Componente NUEVO (extensión): dispara efectos de humo cuando la banda toca
 * "Through the Fire and Flames". Se agregó sin tocar la Banda ni el bus.
 */
public final class SistemaDeHumo implements Suscriptor<BandaTocandoEvent> {

    public static final String CANCION_QUE_ACTIVA_EL_HUMO = "Through the Fire and Flames";
    private static final String ETIQUETA = "HUMO";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;

    public SistemaDeHumo(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
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
        if (!CANCION_QUE_ACTIVA_EL_HUMO.equalsIgnoreCase(bandaTocando.nombreDeLaCancion())) {
            return;
        }
        consola.imprimir(ColorAnsi.AZUL, ETIQUETA,
                "¡Canción épica! Disparando efectos de humo durante " + bandaTocando.duracionEnSegundos() + " s");
    }
}
