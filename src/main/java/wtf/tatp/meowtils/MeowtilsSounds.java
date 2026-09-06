package wtf.tatp.meowtils;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Registers the original packed crit samples before client init. */
public final class MeowtilsSounds implements ModInitializer {
    public static final Identifier CRIT_ID = Identifier.fromNamespaceAndPath("meowtils", "crit");
    public static final SoundEvent CRIT = SoundEvent.createVariableRangeEvent(CRIT_ID);

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, CRIT_ID, CRIT);
    }
}
