package taberna.extensiones;

import static taberna.util.Validaciones.exigirNoNulo;

import taberna.bus.BusDeEventos;
import taberna.util.ColorAnsi;
import taberna.util.ConsolaDeLaTaberna;

/**
 * Demuestra el principio Abierto/Cerrado (Open/Closed): se agregan TRES componentes
 * nuevos (humo, iluminación y auditoría) sin modificar ni un carácter de
 * {@code Banda}, {@code BusDeEventosEnMemoria} ni de ningún componente existente.
 * Solo se necesita el bus para conectarlos.
 */
public final class DemostracionDeExtensibilidad {

    private final ConsolaDeLaTaberna consola;
    private final SistemaDeHumo sistemaDeHumo;
    private final SistemaDeIluminacion sistemaDeIluminacion;
    private final RegistroDeAuditoria registroDeAuditoria;

    public DemostracionDeExtensibilidad(BusDeEventos busDeEventos, ConsolaDeLaTaberna consola) {
        exigirNoNulo(busDeEventos, "busDeEventos");
        this.consola = exigirNoNulo(consola, "consola");
        this.sistemaDeHumo = new SistemaDeHumo(busDeEventos, consola);
        this.sistemaDeIluminacion = new SistemaDeIluminacion(busDeEventos, consola);
        this.registroDeAuditoria = new RegistroDeAuditoria(busDeEventos, consola);
    }

    /** Conecta los nuevos componentes al bus en caliente, con la taberna ya funcionando. */
    public void conectarNuevosComponentes() {
        consola.imprimir(ColorAnsi.AMARILLO, "EXTENSION", "Conectando SistemaDeHumo, SistemaDeIluminacion y RegistroDeAuditoria...");
        sistemaDeHumo.activar();
        sistemaDeIluminacion.activar();
        registroDeAuditoria.activar();
    }

    /** Demuestra la desuscripción dinámica: las luces dejan de reaccionar a la banda. */
    public void desconectarIluminacion() {
        sistemaDeIluminacion.desactivar();
        consola.imprimir(ColorAnsi.AMARILLO, "EXTENSION", "SistemaDeIluminacion desconectado del bus");
    }

    public void imprimirResumenDeAuditoria() {
        registroDeAuditoria.imprimirResumen();
    }
}
