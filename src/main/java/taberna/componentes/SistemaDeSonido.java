package taberna.componentes;

import static taberna.util.Validaciones.exigirNoNulo;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.BandaTocandoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Suscriptor: cuando la banda toca una canción, ajusta el ecualizador.
 */
public final class SistemaDeSonido implements Suscriptor<BandaTocandoEvent> {

    private static final String ETIQUETA = "SONIDO";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;

    public SistemaDeSonido(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
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
        consola.imprimir(ColorAnsi.CIAN, ETIQUETA,
                "Ajustando ecualizador para: " + bandaTocando.nombreDeLaCancion());
    }
}
