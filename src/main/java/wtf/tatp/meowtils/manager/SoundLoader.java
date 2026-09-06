package wtf.tatp.meowtils.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.MeowtilsSounds;
import wtf.tatp.meowtils.manager.log.LogManager;
import wtf.tatp.meowtils.util.Util;

/**
 * Plays the original 2.0.1 Sound enum. Crit uses the packed Meowtils oggs;
 * the rest use the 26.2 events ViaVersion remaps 1.8 names onto.
 */
public final class SoundLoader {
    public enum Sound {
        PING, PING_DEEP, PING_MEDIUM, LEVEL, ANVIL, MEOW, ANVIL_BREAK, ERROR, ERROR_DEEP, CRIT
    }

    private SoundLoader() {}

    public static void init() {
        Meowtils.info("Sounds loaded!");
        LogManager.write("INFO", "Sounds loaded");
    }

    public static void play(Sound sound, int volume) {
        if (sound == null) return;
        if (sound == Sound.CRIT) {
            playEvent(MeowtilsSounds.CRIT, volume / 100.0f, 1.0f);
            return;
        }
        play(legacyName(sound), volume, pitch(sound));
    }

    public static void play(Util.Sound sound, int volume) {
        if (sound == null) return;
        play(Sound.valueOf(sound.name()), volume);
    }

    public static void play(String name18, int volume) {
        play(name18, volume, 1.0f);
    }

    public static void play(String name18, int volume, float pitch) {
        SoundEvent event = from18(name18);
        if (event == null) return;
        playEvent(event, volume / 100.0f, pitch);
    }

    static String legacyName(Sound sound) {
        return switch (sound) {
            case PING, PING_DEEP, PING_MEDIUM -> "random.orb";
            case LEVEL -> "random.levelup";
            case ANVIL -> "random.anvil_land";
            case MEOW -> "mob.cat.meow";
            case ANVIL_BREAK -> "random.anvil_use";
            case ERROR, ERROR_DEEP -> "note.bass";
            case CRIT -> "random.successful_hit";
        };
    }

    private static float pitch(Sound sound) {
        return switch (sound) {
            case PING_DEEP -> 0.2f;
            case PING_MEDIUM -> 0.5f;
            case LEVEL -> 2.0f;
            case ANVIL -> 1.8f;
            case ERROR_DEEP -> 0.6f;
            default -> 1.0f;
        };
    }

    public static SoundEvent from18(String name18) {
        if (name18 == null || name18.isBlank()) return null;
        String key = name18.startsWith("minecraft:") ? name18.substring("minecraft:".length()) : name18;
        return switch (key) {
            case "random.orb", "entity.experience_orb.pickup" -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case "random.levelup", "entity.player.levelup" -> SoundEvents.PLAYER_LEVELUP;
            case "random.anvil_land", "block.anvil.land" -> SoundEvents.ANVIL_LAND;
            case "random.anvil_use", "block.anvil.use" -> SoundEvents.ANVIL_USE;
            case "note.bass", "block.note.bass", "block.note_block.bass" -> SoundEvents.NOTE_BLOCK_BASS.value();
            case "mob.cat.meow", "entity.cat.ambient" -> SoundEvents.CAT_AMBIENT_BABY.value();
            case "random.successful_hit", "entity.arrow.hit_player" -> MeowtilsSounds.CRIT;
            default -> null;
        };
    }

    private static void playEvent(SoundEvent event, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null || event == null) return;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
    }
}
