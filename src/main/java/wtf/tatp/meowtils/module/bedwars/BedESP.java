package wtf.tatp.meowtils.module.bedwars;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.module.render.WorldOverlay;
import wtf.tatp.meowtils.util.Settings;

/** Highlights bed heads/feet and adjacent obsidian. */
public final class BedESP extends Module {
    @wtf.tatp.meowtils.config.Config public int red = GUI.BLUE_DEFAULT, green = GUI.BLUE_DEFAULT, blue = GUI.BLUE_DEFAULT;
    @wtf.tatp.meowtils.config.Config public float opacity = 25f;
    private static boolean scanned;
    public static final Set<BlockPos> BEDS = ConcurrentHashMap.newKeySet();
    public static final Set<BlockPos> OBSIDIAN = ConcurrentHashMap.newKeySet();
    private static final Map<BlockPos, AABB> LAST_BOX = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Integer> MISSES = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Integer> OBSIDIAN_MISSES = new ConcurrentHashMap<>();
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    public BedESP() {
        super("BedESP", Category.Bedwars);
        tag(ModuleTag.SAFE);
        tooltip("Highlight beds and surrounding obsidian.");
        ColorLink colorLink = new ColorLink("red", "green", "blue", this);
        addColor(new ColorValue("Bed color", colorLink));
        addSaturation(new SaturationValue(colorLink));
        addBrightness(new BrightnessValue(colorLink));
        addOpacity(new OpacityValue("Opacity", "opacity", this));
        addMode(new ModeValue("Mode", List.of("Full", "Outline"), "mode", this));
        addToggle(new ToggleValue("Show obsidian", "showObsidian", this));
        addToggle(new ToggleValue("Bedwars only", "bedwarsOnly", this));
    }

    @EventTarget
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (Settings.bool(this, "bedwarsOnly", false) && Bedwars.GAME.isNotActive()) return;
        int alpha = BedwarsSupport.opacityAlpha(Settings.number(this, "opacity", opacity));
        int bedColor = BedwarsSupport.rgba(Settings.integer(this, "red", red), Settings.integer(this, "green", green), Settings.integer(this, "blue", blue), alpha);
        int obsidianColor = BedwarsSupport.rgba(170, 0, 170, alpha);
        boolean fill = WorldOverlay.wantsFill(this, "mode", "Full");
        for (BlockPos key : BEDS) {
            AABB box = boxFor(key);
            if (box == null) continue;
            WorldOverlay.espBox(event, box, bedColor, fill);
        }
        if (!Settings.bool(this, "showObsidian", true)) return;
        for (BlockPos pos : OBSIDIAN) {
            AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            WorldOverlay.espBox(event, box, obsidianColor, fill);
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (event.getPhase() != ClientTickEvent.Phase.POST || mc.player == null || mc.level == null) return;
        if (!(Settings.bool(this, "bedwarsOnly", false) && Bedwars.ALL.isNotActive()) && !scanned) {
            scanned = true;
            resetTracked();
            scanLoadedChunks();
        }
        pruneStale();
    }

    @EventTarget
    public void onWorldEvent(WorldEvent event) {
        if (event.getType() != WorldEvent.Type.UNLOAD) return;
        resetTracked();
        scanned = false;
    }

    public static void updateBlocks(BlockPos pos, BlockState state) {
        if (BedwarsSupport.isBed(state)) {
            remember(pos, state);
            return;
        }
        if (state != null && !state.isAir()) forget(pos);
        if (BedwarsSupport.isObsidian(state) || (state != null && state.isAir())) {
            for (BlockPos bed : BEDS) {
                if (bed.distSqr(pos) <= 4.0) checkObsidian(bed);
            }
        }
    }

    public static void updateChunk(LevelChunk chunk) {
        if (chunk == null) return;
        long started = System.nanoTime();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        var sections = chunk.getSections();
        for (int index = 0; index < sections.length; index++) {
            var section = sections[index];
            if (section == null || section.hasOnlyAir()) continue;
            int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
            if (!BedEspScan.touchesLegacyWorld(baseY)) continue;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int localY = 0; localY < 16; localY++) {
                        int y = baseY + localY;
                        if (y < 0 || y > 255) continue;
                        BlockState state = section.getBlockState(x, localY, z);
                        if (!BedwarsSupport.isBed(state)) continue;
                        remember(new BlockPos(minX + x, y, minZ + z), state);
                    }
                }
            }
        }
        long ms = (System.nanoTime() - started) / 1_000_000L;
        if (ms >= 8L) wtf.tatp.meowtils.Meowtils.warn("BedESP chunk scan took " + ms + "ms");
    }

    private static void remember(BlockPos pos, BlockState state) {
        BlockPos raw = pos.immutable();
        BlockPos mate = otherPart(raw, state);
        BlockPos key = raw.asLong() <= mate.asLong() ? raw.immutable() : mate.immutable();
        if (!key.equals(raw)) {
            BEDS.remove(raw);
            LAST_BOX.remove(raw);
            MISSES.remove(raw);
        }
        if (!key.equals(mate)) {
            BEDS.remove(mate);
            LAST_BOX.remove(mate);
            MISSES.remove(mate);
        }
        BEDS.add(key);
        MISSES.remove(key);
        LAST_BOX.put(key, union(raw, mate));
        checkObsidian(key);
    }

    private static void forget(BlockPos pos) {
        if (BEDS.remove(pos)) {
            LAST_BOX.remove(pos);
            MISSES.remove(pos);
            OBSIDIAN.removeIf(obsidian -> obsidian.distSqr(pos) <= 4.0);
            return;
        }
        BEDS.removeIf(key -> {
            if (!covers(key, pos)) return false;
            LAST_BOX.remove(key);
            MISSES.remove(key);
            OBSIDIAN.removeIf(obsidian -> obsidian.distSqr(key) <= 4.0);
            return true;
        });
    }

    private static boolean covers(BlockPos key, BlockPos pos) {
        AABB box = LAST_BOX.get(key);
        if (box != null) {
            return pos.getX() + 1 > box.minX && pos.getX() < box.maxX
                    && pos.getY() + 1 > box.minY && pos.getY() < box.maxY
                    && pos.getZ() + 1 > box.minZ && pos.getZ() < box.maxZ;
        }
        return key.equals(pos) || (key.getY() == pos.getY() && key.distManhattan(pos) == 1);
    }

    private static AABB boxFor(BlockPos key) {
        ClientLevel level = level();
        if (level == null) return LAST_BOX.get(key);
        BlockState state = level.getBlockState(key);
        if (BedwarsSupport.isBed(state)) {
            AABB box = union(key, otherPart(key, state));
            LAST_BOX.put(key, box);
            return box;
        }
        for (Direction facing : HORIZONTAL) {
            BlockPos neighbor = key.relative(facing);
            if (!BedwarsSupport.isBed(level.getBlockState(neighbor))) continue;
            AABB box = union(key, neighbor);
            LAST_BOX.put(key, box);
            return box;
        }
        return LAST_BOX.get(key);
    }

    private static AABB union(BlockPos first, BlockPos second) {
        return new AABB(
                Math.min(first.getX(), second.getX()), first.getY(), Math.min(first.getZ(), second.getZ()),
                Math.max(first.getX() + 1, second.getX() + 1), first.getY() + 0.5625, Math.max(first.getZ() + 1, second.getZ() + 1)
        );
    }

    private static void pruneStale() {
        ClientLevel level = level();
        if (level == null) return;
        BEDS.removeIf(key -> {
            if (!level.hasChunk(key.getX() >> 4, key.getZ() >> 4)) {
                MISSES.remove(key);
                return false;
            }
            if (bedStillPresent(level, key)) {
                MISSES.remove(key);
                return false;
            }
            int misses = MISSES.merge(key, 1, Integer::sum);
            if (!BedEspScan.isStale(misses)) return false;
            LAST_BOX.remove(key);
            MISSES.remove(key);
            OBSIDIAN.removeIf(obsidian -> obsidian.distSqr(key) <= 4.0);
            return true;
        });
        OBSIDIAN.removeIf(pos -> {
            if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                OBSIDIAN_MISSES.remove(pos);
                return false;
            }
            if (BedwarsSupport.isObsidian(level.getBlockState(pos))) {
                OBSIDIAN_MISSES.remove(pos);
                return false;
            }
            int misses = OBSIDIAN_MISSES.merge(pos, 1, Integer::sum);
            if (!BedEspScan.isStale(misses)) return false;
            OBSIDIAN_MISSES.remove(pos);
            return true;
        });
    }

    private static boolean bedStillPresent(ClientLevel level, BlockPos key) {
        if (BedwarsSupport.isBed(level.getBlockState(key))) return true;
        AABB box = LAST_BOX.get(key);
        if (box != null) {
            int minX = (int) Math.floor(box.minX);
            int minZ = (int) Math.floor(box.minZ);
            int maxX = (int) Math.ceil(box.maxX) - 1;
            int maxZ = (int) Math.ceil(box.maxZ) - 1;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (BedwarsSupport.isBed(level.getBlockState(new BlockPos(x, key.getY(), z)))) return true;
                }
            }
        }
        for (Direction facing : HORIZONTAL) {
            if (BedwarsSupport.isBed(level.getBlockState(key.relative(facing)))) return true;
        }
        return false;
    }

    private static void checkObsidian(BlockPos headPos) {
        ClientLevel level = level();
        if (level == null) return;
        OBSIDIAN.removeIf(pos -> pos.distSqr(headPos) <= 4.0);
        BlockState state = level.getBlockState(headPos);
        BlockPos foot = BedwarsSupport.isBed(state) ? otherPart(headPos, state) : mateFromBox(headPos);
        for (BlockPos part : new BlockPos[]{headPos, foot}) {
            for (Direction facing : Direction.values()) {
                BlockPos offset = part.relative(facing);
                if (BedwarsSupport.isObsidian(level.getBlockState(offset))) OBSIDIAN.add(offset.immutable());
            }
        }
    }

    private static BlockPos mateFromBox(BlockPos key) {
        AABB box = LAST_BOX.get(key);
        if (box == null) return key;
        int otherX = key.getX() == (int) Math.floor(box.minX) ? (int) Math.ceil(box.maxX) - 1 : (int) Math.floor(box.minX);
        int otherZ = key.getZ() == (int) Math.floor(box.minZ) ? (int) Math.ceil(box.maxZ) - 1 : (int) Math.floor(box.minZ);
        return new BlockPos(otherX, key.getY(), otherZ);
    }

    private static BlockPos otherPart(BlockPos pos, BlockState state) {
        ClientLevel level = level();
        if (level == null || state == null) return pos;
        if (state.hasProperty(BedBlock.FACING)) {
            Direction facing = state.getValue(BedBlock.FACING);
            if (state.hasProperty(BedBlock.PART)) {
                BlockPos mate = state.getValue(BedBlock.PART) == BedPart.HEAD
                        ? pos.relative(facing.getOpposite()) : pos.relative(facing);
                if (BedwarsSupport.isBed(level.getBlockState(mate))) return mate;
            } else {
                BlockPos along = pos.relative(facing);
                if (BedwarsSupport.isBed(level.getBlockState(along))) return along;
                BlockPos opposite = pos.relative(facing.getOpposite());
                if (BedwarsSupport.isBed(level.getBlockState(opposite))) return opposite;
            }
        }
        for (Direction facing : HORIZONTAL) {
            BlockPos mate = pos.relative(facing);
            if (BedwarsSupport.isBed(level.getBlockState(mate))) return mate;
        }
        return pos;
    }

    private static void scanLoadedChunks() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        int cx = client.player.blockPosition().getX() >> 4;
        int cz = client.player.blockPosition().getZ() >> 4;
        for (int x = cx - 8; x <= cx + 8; x++) {
            for (int z = cz - 8; z <= cz + 8; z++) {
                if (!client.level.hasChunk(x, z)) continue;
                updateChunk(client.level.getChunk(x, z));
            }
        }
    }

    private static void init() {
        scanLoadedChunks();
    }

    private static void resetTracked() {
        BEDS.clear();
        OBSIDIAN.clear();
        LAST_BOX.clear();
        MISSES.clear();
        OBSIDIAN_MISSES.clear();
    }

    private static ClientLevel level() {
        return Minecraft.getInstance().level;
    }

    @Override
    public void onEnable() {
        resetTracked();
        scanned = false;
        init();
    }

    @Override
    public void onDisable() {
        resetTracked();
        scanned = false;
    }
}
