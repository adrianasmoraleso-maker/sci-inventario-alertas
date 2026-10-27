package adapters.in;

import domain.LotStatus;
import domain.ProductStatus;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Tema central de la interfaz: colores, tipografía, espaciados y estilos de estado.
 * Ninguna pantalla define colores, fuentes ni medidas propias; todo sale de aquí.
 */
public final class Theme {
    private Theme() { }

    // ---- Paleta ----
    public static final Color SIDEBAR_BG = new Color(0x0F1B33);
    public static final Color SIDEBAR_ITEM_ACTIVE = new Color(0x24365C);
    public static final Color SIDEBAR_ITEM_HOVER = new Color(0x1A2A4A);
    public static final Color SIDEBAR_TEXT = new Color(0xB8C3D9);
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    public static final Color BACKGROUND = new Color(0xF3F5F9);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(0xDDE2EC);
    public static final Color ROW_SEPARATOR = new Color(0xECEFF5);
    public static final Color TABLE_HEADER_BG = new Color(0xF1F3F8);

    public static final Color PRIMARY = new Color(0x1B2B4B);
    public static final Color PRIMARY_HOVER = new Color(0x2C416E);
    public static final Color PRIMARY_TEXT = Color.WHITE;
    public static final Color DISABLED_BG = new Color(0xC9CFDB);
    public static final Color DISABLED_TEXT = new Color(0x9AA3B5);

    public static final Color TEXT = new Color(0x1B2438);
    public static final Color TEXT_MUTED = new Color(0x667085);
    public static final Color ERROR = new Color(0xC62828);
    public static final Color FOCUS = new Color(0x3F5FA8);
    public static final Color SHADOW = new Color(0x0F1B33);

    // ---- Tonos de estado ----
    public enum Tone {
        GREEN(new Color(0x1E7F4F), new Color(0xDCF3E6)),
        AMBER(new Color(0x9A5B00), new Color(0xFDEBCB)),
        RED(new Color(0xB3261E), new Color(0xFBDCD9)),
        BLUE(new Color(0x2B5CB8), new Color(0xDEE9FB)),
        GRAY(new Color(0x475066), new Color(0xE8EBF2));

        public final Color fg;
        public final Color bg;

        Tone(Color fg, Color bg) { this.fg = fg; this.bg = bg; }
    }

    public static Tone toneOf(LotStatus s) {
        return switch (s) {
            case VIGENTE -> Tone.GREEN;
            case PROXIMO_A_CADUCAR -> Tone.AMBER;
            case CADUCADO -> Tone.RED;
        };
    }

    public static Tone toneOf(ProductStatus s) {
        return switch (s) {
            case DISPONIBLE -> Tone.GREEN;
            case STOCK_BAJO -> Tone.AMBER;
            case SIN_STOCK -> Tone.RED;
        };
    }

    // ---- Tipografía (una sola familia sans-serif) ----
    public static final String FONT_FAMILY = resolveFamily();

    private static String resolveFamily() {
        List<String> available = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String candidate : new String[]{"Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue",
                "Roboto", "Noto Sans", "DejaVu Sans", "Arial"}) {
            if (available.contains(candidate)) return candidate;
        }
        return Font.SANS_SERIF;
    }

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, 1).deriveFont(style, size);
    }

    public static final Font FONT_BODY = font(Font.PLAIN, 13f);
    public static final Font FONT_BODY_BOLD = font(Font.BOLD, 13f);
    public static final Font FONT_SMALL = font(Font.PLAIN, 12f);
    public static final Font FONT_SMALL_BOLD = font(Font.BOLD, 12f);
    public static final Font FONT_TITLE = font(Font.BOLD, 24f);
    public static final Font FONT_CARD_TITLE = font(Font.BOLD, 16f);
    public static final Font FONT_FIGURE = font(Font.BOLD, 26f);
    public static final Font FONT_NAV = font(Font.PLAIN, 14f);
    public static final Font FONT_BRAND = font(Font.BOLD, 16f);

    // ---- Espaciados y medidas ----
    public static final int SP_XS = 4;
    public static final int SP_S = 8;
    public static final int SP_M = 12;
    public static final int SP_L = 16;
    public static final int SP_XL = 24;
    public static final int RADIUS = 14;
    public static final int RADIUS_SMALL = 8;
    public static final int SHADOW_SIZE = 6;
    public static final int CONTROL_HEIGHT = 38;
    public static final int SMALL_BUTTON_HEIGHT = 28;
    public static final int ROW_HEIGHT = 50;
    public static final int SIDEBAR_WIDTH = 220;
    public static final Dimension MIN_WINDOW = new Dimension(1000, 650);
    public static final Dimension DEFAULT_WINDOW = new Dimension(1280, 760);

    private static boolean installed;

    /** Aplica el tema a los componentes estándar de Swing (se llama una sola vez). */
    public static synchronized void install() {
        if (installed) return;
        installed = true;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // se usa el aspecto por defecto
        }
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (key.toString().endsWith(".font")) {
                UIManager.put(key, new FontUIResource(FONT_BODY));
            }
        }
        UIManager.put("ToolTip.font", new FontUIResource(FONT_SMALL));
        UIManager.put("ToolTip.background", PRIMARY);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(SP_XS, SP_S, SP_XS, SP_S));
    }
}
