package taberna.extensiones;

import static taberna.util.Validaciones.exigirNoNulo;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import taberna.bus.BusDeEventos;
import taberna.bus.Suscriptor;
import taberna.eventos.Evento;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Componente NUEVO (extensión): cuenta cuántos eventos de cada tipo ocurrieron.
 * Se suscribe a {@link Evento}, así que ve todo lo que pasa en la taberna sin que
 * ningún otro componente sepa que existe.
 */
public final class RegistroDeAuditoria implements Suscriptor<Evento> {

    private static final String ETIQUETA = "AUDITORIA";

    private final BusDeEventos busDeEventos;
    private final ConsolaDeLaTaberna consola;
    private final Map<String, LongAdder> cantidadDeEventosPorTipo = new ConcurrentHashMap<>();

    public RegistroDeAuditoria(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
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
        cantidadDeEventosPorTipo
                .computeIfAbsent(evento.nombreDelTipoDeEvento(), tipo -> new LongAdder())
                .increment();
    }

    /** Devuelve una copia ordenada alfabéticamente: tipo de evento -> cantidad. */
    public Map<String, Long> resumen() {
        Map<String, Long> resumenOrdenado = new TreeMap<>();
        cantidadDeEventosPorTipo.forEach((tipo, contador) -> resumenOrdenado.put(tipo, contador.sum()));
        return resumenOrdenado;
    }

    public void imprimirResumen() {
        consola.imprimirSeparador("Resumen de auditoría");
        resumen().forEach((tipoDeEvento, cantidad) ->
                consola.imprimir(ColorAnsi.GRIS, ETIQUETA, String.format("%-22s %d", tipoDeEvento, cantidad)));
    }
}
