package taberna;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import taberna.bus.BusDeEventos;
import taberna.bus.BusDeEventosEnMemoria;
import taberna.bus.Suscriptor;
import taberna.eventos.BandaTocandoEvent;
import taberna.eventos.BebidaServidaEvent;
import taberna.eventos.Evento;
import taberna.eventos.ItemDelPedido;
import taberna.eventos.PedidoRealizadoEvent;

/**
 * Pruebas sencillas sin librerías externas. Se ejecutan con:
 * {@code java -cp out taberna.PruebasDelBusDeEventos}
 */
public final class PruebasDelBusDeEventos {

    private static int pruebasEjecutadas = 0;

    public static void main(String[] argumentos) throws Exception {
        Logger.getLogger(BusDeEventosEnMemoria.class.getName()).setLevel(Level.OFF);

        suscriptorRecibeSoloSuTipoDeEvento();
        desuscribirDejaDeNotificar();
        noSeDuplicanSuscriptores();
        suscriptorDeTipoBaseRecibeTodosLosEventos();
        unaExcepcionNoDetieneLasNotificaciones();
        sePuedeDesuscribirDuranteLaPublicacion();
        losArgumentosNulosSeRechazan();
        losEventosSonInmutablesYValidados();
        publicarDesdeVariosHilosNoPierdeEventos();

        System.out.println("OK: " + pruebasEjecutadas + " pruebas pasaron correctamente");
    }

    private static void suscriptorRecibeSoloSuTipoDeEvento() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        List<BebidaServidaEvent> recibidos = new ArrayList<>();
        bus.suscribir(BebidaServidaEvent.class, recibidos::add);

        bus.publicar(new BebidaServidaEvent(1, "cerveza"));
        bus.publicar(new BandaTocandoEvent("Banda", "Canción", 100));

        verificar(recibidos.size() == 1, "solo debe recibir BebidaServidaEvent");
    }

    private static void desuscribirDejaDeNotificar() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        AtomicInteger notificaciones = new AtomicInteger();
        Suscriptor<BebidaServidaEvent> suscriptor = evento -> notificaciones.incrementAndGet();

        bus.suscribir(BebidaServidaEvent.class, suscriptor);
        bus.publicar(new BebidaServidaEvent(1, "cerveza"));
        boolean removido = bus.desuscribir(BebidaServidaEvent.class, suscriptor);
        bus.publicar(new BebidaServidaEvent(1, "cerveza"));

        verificar(removido, "desuscribir debe devolver true si estaba suscrito");
        verificar(notificaciones.get() == 1, "tras desuscribir no debe recibir más eventos");
        verificar(!bus.desuscribir(BebidaServidaEvent.class, suscriptor), "desuscribir de nuevo debe devolver false");
    }

    private static void noSeDuplicanSuscriptores() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        AtomicInteger notificaciones = new AtomicInteger();
        Suscriptor<BebidaServidaEvent> suscriptor = evento -> notificaciones.incrementAndGet();

        bus.suscribir(BebidaServidaEvent.class, suscriptor);
        bus.suscribir(BebidaServidaEvent.class, suscriptor);
        bus.publicar(new BebidaServidaEvent(1, "cerveza"));

        verificar(notificaciones.get() == 1, "un suscriptor repetido solo se notifica una vez");
        verificar(bus.cantidadDeSuscriptores(BebidaServidaEvent.class) == 1, "debe haber un solo suscriptor");
    }

    private static void suscriptorDeTipoBaseRecibeTodosLosEventos() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        List<Evento> recibidos = new ArrayList<>();
        bus.suscribir(Evento.class, recibidos::add);

        bus.publicar(new BebidaServidaEvent(1, "cerveza"));
        bus.publicar(new BandaTocandoEvent("Banda", "Canción", 100));

        verificar(recibidos.size() == 2, "un suscriptor de Evento.class recibe todos los eventos");
    }

    private static void unaExcepcionNoDetieneLasNotificaciones() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        AtomicInteger notificaciones = new AtomicInteger();
        bus.suscribir(BebidaServidaEvent.class, evento -> {
            throw new IllegalStateException("fallo simulado");
        });
        bus.suscribir(BebidaServidaEvent.class, evento -> notificaciones.incrementAndGet());

        bus.publicar(new BebidaServidaEvent(1, "cerveza"));

        verificar(notificaciones.get() == 1, "el segundo suscriptor debe recibir el evento pese al fallo del primero");
    }

    private static void sePuedeDesuscribirDuranteLaPublicacion() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        AtomicInteger notificaciones = new AtomicInteger();
        AtomicReference<Suscriptor<BebidaServidaEvent>> autoDesuscribible = new AtomicReference<>();
        autoDesuscribible.set(evento -> {
            notificaciones.incrementAndGet();
            bus.desuscribir(BebidaServidaEvent.class, autoDesuscribible.get());
        });
        bus.suscribir(BebidaServidaEvent.class, autoDesuscribible.get());

        bus.publicar(new BebidaServidaEvent(1, "cerveza"));
        bus.publicar(new BebidaServidaEvent(1, "cerveza"));

        verificar(notificaciones.get() == 1, "desuscribirse durante la publicación no debe fallar");
    }

    private static void losArgumentosNulosSeRechazan() {
        BusDeEventos bus = new BusDeEventosEnMemoria();
        verificarExcepcion(NullPointerException.class, () -> bus.publicar(null));
        verificarExcepcion(NullPointerException.class, () -> bus.suscribir(null, evento -> { }));
        verificarExcepcion(NullPointerException.class, () -> bus.suscribir(BebidaServidaEvent.class, null));
    }

    private static void losEventosSonInmutablesYValidados() {
        List<ItemDelPedido> itemsOriginales = new ArrayList<>(List.of(ItemDelPedido.bebida("cerveza")));
        PedidoRealizadoEvent pedido = new PedidoRealizadoEvent(5, "pedido_1", itemsOriginales);

        itemsOriginales.add(ItemDelPedido.comida("pizza"));

        verificar(pedido.items().size() == 1, "el evento debe guardar una copia defensiva de los items");
        verificarExcepcion(UnsupportedOperationException.class, () -> pedido.items().add(ItemDelPedido.comida("tacos")));
        verificarExcepcion(IllegalArgumentException.class, () -> new BebidaServidaEvent(0, "cerveza"));
        verificarExcepcion(IllegalArgumentException.class, () -> new BandaTocandoEvent("Banda", "Canción", 0));
        verificarExcepcion(NullPointerException.class, () -> new BebidaServidaEvent(1, null));
    }

    private static void publicarDesdeVariosHilosNoPierdeEventos() throws InterruptedException {
        final int cantidadDeHilos = 8;
        final int eventosPorHilo = 1_000;
        BusDeEventos bus = new BusDeEventosEnMemoria();
        AtomicInteger notificaciones = new AtomicInteger();
        bus.suscribir(BebidaServidaEvent.class, evento -> notificaciones.incrementAndGet());

        ExecutorService ejecutor = Executors.newFixedThreadPool(cantidadDeHilos);
        CountDownLatch terminados = new CountDownLatch(cantidadDeHilos);
        for (int hilo = 0; hilo < cantidadDeHilos; hilo++) {
            ejecutor.execute(() -> {
                for (int i = 0; i < eventosPorHilo; i++) {
                    bus.publicar(new BebidaServidaEvent(1, "cerveza"));
                }
                terminados.countDown();
            });
        }
        terminados.await(10, TimeUnit.SECONDS);
        ejecutor.shutdown();

        verificar(notificaciones.get() == cantidadDeHilos * eventosPorHilo, "no debe perderse ningún evento");
    }

    private static void verificar(boolean condicion, String descripcion) {
        pruebasEjecutadas++;
        if (!condicion) {
            throw new AssertionError("FALLÓ: " + descripcion);
        }
    }

    private static void verificarExcepcion(Class<? extends Throwable> tipoEsperado, Runnable accion) {
        pruebasEjecutadas++;
        try {
            accion.run();
        } catch (Throwable error) {
            if (tipoEsperado.isInstance(error)) {
                return;
            }
            throw new AssertionError("FALLÓ: se esperaba " + tipoEsperado.getSimpleName()
                    + " pero se obtuvo " + error.getClass().getSimpleName());
        }
        throw new AssertionError("FALLÓ: se esperaba " + tipoEsperado.getSimpleName() + " y no hubo excepción");
    }
}
