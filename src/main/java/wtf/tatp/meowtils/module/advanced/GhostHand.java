package wtf.tatp.meowtils.module.advanced;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.SessionManager;
import wtf.tatp.meowtils.util.ItemIds;
import wtf.tatp.meowtils.util.Settings;
import wtf.tatp.meowtils.util.TeamUtil;

/** Lets the crosshair pass through configured entities so the client interacts with the block behind them. */
public final class GhostHand extends Module {
    public GhostHand() {
        super("GhostHand", Category.Advanced);
        tag(ModuleTag.BLATANT);
        tooltip("Allows you to interact through entities.");
        addMode(new ModeValue("Transparent", List.of("Always", "None"), "transparent", this));
        addToggle(new ToggleValue("Use item whitelist", "useItemWhitelist", this));
        addToggle(new ToggleValue("Use block whitelist", "useBlockWhitelist", this));
        addToggle(new ToggleValue("Bedwars only", "bedwarsOnly", this));
        addCheck(new CheckValue("Through teammates", "interactTeammates", this));
        addCheck(new CheckValue("Through enemies", "interactEnemies", this));
        addCheck(new CheckValue("Through armorstands", "interactArmorstands", this));
        addExpand(new ExpandValue("Item whitelist", e -> {
            e.addCheck(new CheckValue("§bSwords", "swords", this));
            e.addCheck(new CheckValue("§6Tools", "tools", this));
            e.addCheck(new CheckValue("§eHand", "hand", this));
            e.addCheck(new CheckValue("Blocks", "blocks", this));
            e.addCheck(new CheckValue("§9Buckets", "buckets", this));
            e.addCheck(new CheckValue("§7Flint and steel", "flintAndSteel", this));
        }, this));
        addExpand(new ExpandValue("Block whitelist", e -> {
            e.addCheck(new CheckValue("§cBeds", "beds", this));
            e.addCheck(new CheckValue("§8Obsidian", "obsidian", this));
            e.addCheck(new CheckValue("Defense blocks", "defenseBlocks", this));
            e.addCheck(new CheckValue("§6Chests", "chests", this));
        }, this));
    }

    public boolean shouldActivate(Entity entity) {
        return shouldPassThrough(entity);
    }

    public static boolean shouldPassThrough(Entity entity) {
        GhostHand ghost = get(GhostHand.class);
        if (ghost == null || !ghost.getState() || entity == null) return false;
        if (Settings.bool(ghost, "bedwarsOnly", false) && !SessionManager.bedwarsGame) return false;
        return isWhitelistedEntity(entity) && isWhitelistedItem() && isWhitelistedBlock();
    }

    private static boolean isWhitelistedEntity(Entity entity) {
        GhostHand ghost = get(GhostHand.class);
        if (ghost == null) return false;
        if (entity instanceof Player player && !TeamUtil.isBot(player)) {
            if (Settings.bool(ghost, "interactTeammates", true) && TeamUtil.isTeam(player)) return true;
            if (Settings.bool(ghost, "interactEnemies", false)) return true;
        }
        return Settings.bool(ghost, "interactArmorstands", false) && entity instanceof ArmorStand;
    }

    private static boolean isWhitelistedItem() {
        Minecraft mc = Minecraft.getInstance();
        GhostHand ghost = get(GhostHand.class);
        if (ghost == null || mc.player == null) return false;
        if (!Settings.bool(ghost, "useItemWhitelist", true)) return true;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return Settings.bool(ghost, "hand", true);
        if (stack.is(ItemTags.SWORDS) && Settings.bool(ghost, "swords", false)) return true;
        if (Settings.bool(ghost, "tools", true) && (stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES) || stack.is(Items.SHEARS))) return true;
        if (stack.getItem() instanceof BlockItem && Settings.bool(ghost, "blocks", false)) return true;
        if (Settings.bool(ghost, "buckets", false) && (stack.is(Items.BUCKET) || stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET))) return true;
        return stack.is(Items.FLINT_AND_STEEL) && Settings.bool(ghost, "flintAndSteel", false);
    }

    private static boolean isWhitelistedBlock() {
        Minecraft mc = Minecraft.getInstance();
        GhostHand ghost = get(GhostHand.class);
        if (ghost == null || mc.player == null || mc.level == null) return false;
        if (!Settings.bool(ghost, "useBlockWhitelist", false)) return true;
        Entity camera = mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity();
        HitResult hit = camera.pick(mc.player.blockInteractionRange(), 1.0f, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return false;
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if ((state.getBlock() instanceof BedBlock || state.is(BlockTags.BEDS)) && Settings.bool(ghost, "beds", true)) return true;
        if (ItemIds.isBlock(state, "obsidian") && Settings.bool(ghost, "obsidian", true)) return true;
        if (Settings.bool(ghost, "defenseBlocks", true) && isDefenseBlock(state)) return true;
        return (state.getBlock() instanceof ChestBlock || state.getBlock() instanceof EnderChestBlock)
                && Settings.bool(ghost, "chests", true);
    }

    private static boolean isDefenseBlock(BlockState state) {
        return state.is(BlockTags.WOOL) || state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)
                || ItemIds.isBlock(state, "stone", "packed_ice", "terracotta", "concrete");
    }
}
