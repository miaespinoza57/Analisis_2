package taberna.util;

/**
 * Colores ANSI para que cada componente tenga su propio color en la consola.
 */
public enum ColorAnsi {
    RESTABLECER("\u001B[0m"),
    ROJO("\u001B[91m"),
    VERDE("\u001B[92m"),
    AMARILLO("\u001B[93m"),
    AZUL("\u001B[94m"),
    MAGENTA("\u001B[95m"),
    CIAN("\u001B[96m"),
    NARANJA("\u001B[38;5;208m"),
    GRIS("\u001B[90m");

    private final String secuenciaDeEscape;

    ColorAnsi(String secuenciaDeEscape) {
        this.secuenciaDeEscape = secuenciaDeEscape;
    }

    /** Devuelve el texto envuelto con este color y restablece el color al final. */
    public String aplicarA(String texto) {
        return secuenciaDeEscape + texto + RESTABLECER.secuenciaDeEscape;
    }
}
