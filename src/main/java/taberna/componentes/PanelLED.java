package taberna.componentes;

import static taberna.util.Validaciones.exigirNoNulo;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.BandaTocandoEvent;
import taberna.eventos.BebidaServidaEvent;
import taberna.eventos.ComidaPreparadaEvent;
import taberna.eventos.Evento;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Suscriptor de TODOS los eventos: muestra un anuncio en el panel de LEDs según
 * el tipo de evento recibido. Como se suscribe al tipo base {@link Evento}, también
 * mostrará los eventos nuevos que se creen en el futuro sin cambiar el bus.
 */
public final class PanelLED implements Suscriptor<Evento> {

    private static final String ETIQUETA = "PANEL LED";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;

    public PanelLED(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
        this.busDeEventos = exigirNoNulo(busDeEventos, "busDeEventos");
        this.consola = exigirNoNulo(consola, "consola");
    }

    public void activar() {
        busDeEventos.suscribir(Evento.class, this);
    }

    public void desactivar() {
        busDeEventos.desuscribir(Evento.class, this);
    }

    @Override
    public void alRecibirEvento(Evento evento) {
        consola.imprimir(ColorAnsi.MAGENTA, ETIQUETA, redactarAnuncio(evento));
    }

    private String redactarAnuncio(Evento evento) {
        if (evento instanceof PedidoRealizadoEvent pedido) {
            return "¡Nuevo pedido en la Mesa " + pedido.numeroDeMesa() + "! " + pedido.items();
        }
        if (evento instanceof BebidaServidaEvent bebidaServida) {
            return "¡Nueva bebida servida en la Mesa " + bebidaServida.numeroDeMesa()
                    + "! (" + bebidaServida.bebida() + ")";
        }
        if (evento instanceof ComidaPreparadaEvent comidaPreparada) {
            return "¡Comida preparada para el " + comidaPreparada.identificadorDePedido()
                    + "! (" + comidaPreparada.plato() + ")";
        }
        if (evento instanceof BandaTocandoEvent bandaTocando) {
            return "Ahora suena: " + bandaTocando.nombreDeLaBanda() + " - "
                    + bandaTocando.nombreDeLaCancion() + " (" + bandaTocando.duracionEnSegundos() + " s)";
        }
        return "Evento recibido: " + evento;
    }
}
