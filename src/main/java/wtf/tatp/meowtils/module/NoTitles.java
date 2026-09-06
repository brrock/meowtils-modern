package wtf.tatp.meowtils.module;

import wtf.tatp.meowtils.gui.Module;

/** Suppresses title and subtitle overlays. */
public final class NoTitles extends Module {
    public static NoTitles INSTANCE;
    public NoTitles() { super("NoTitles", Category.Utility); INSTANCE = this; }
}
