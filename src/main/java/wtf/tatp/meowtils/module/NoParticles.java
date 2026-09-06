package wtf.tatp.meowtils.module;

import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ToggleValue;

/** Prevents client particles from being created while enabled. */
public final class NoParticles extends Module {
    public static NoParticles INSTANCE;
    public final ToggleValue removeGlyph=new ToggleValue("Remove bedwars glyph","removeGlyph",this);
    public final ToggleValue removeSponge=new ToggleValue("Remove bedwars sponge","removeSponge",this);
    public final ToggleValue removeBreak=new ToggleValue("Remove block break","removeBreak",this);
    public NoParticles() { super("NoParticles", Category.Utility); INSTANCE=this; tag(ModuleTag.LEGIT); tooltip("Removes certain particles for visibility and performance."); removeGlyph.set(true); removeSponge.set(true); addToggle(removeGlyph); addToggle(removeSponge); addToggle(removeBreak); }
    public static boolean filter(net.minecraft.core.particles.ParticleOptions options) { if(INSTANCE==null||!INSTANCE.getState())return false; if(INSTANCE.removeGlyph.get()&&options instanceof net.minecraft.core.particles.DustParticleOptions)return true; if(INSTANCE.removeSponge.get()&&options.getType()==net.minecraft.core.particles.ParticleTypes.CLOUD)return true; return INSTANCE.removeBreak.get()&&options instanceof net.minecraft.core.particles.BlockParticleOption; }
}
