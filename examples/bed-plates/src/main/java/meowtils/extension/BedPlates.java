package meowtils.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.ClientTickEvent.Phase;
import wtf.tatp.meowtils.event.WorldEvent.Type;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.module.bedwars.BedESP;
import wtf.tatp.meowtils.util.ColorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import wtf.tatp.meowtils.extension.render.Draw;
import wtf.tatp.meowtils.extension.render.Shapes;
import wtf.tatp.meowtils.module.render.WorldOverlay;
import org.lwjgl.glfw.GLFW;

public class BedPlates extends Extension {
   @Config
   public boolean enabled = false;
   @Config
   public int key = 0;
   @Config
   public boolean alignTop = false;
   @Config
   public boolean autoScale = true;
   @Config
   public float scale = 0.8F;
   @Config
   public float plateOpacity = 48.0F;
   @Config
   public float borderOpacity = 62.0F;
   @Config
   public float shadowOpacity = 22.0F;
   @Config
   public int renderDistance = 150;
   @Config
   public float yOffset = 1.0F;
   @Config
   public String displayMode = "Static";
   @Config
   public int displayKey = 0;
   @Config
   public boolean bedwarsOnly = false;
   private static final Set<String> INVALID_VARIANTS = new HashSet<>(
      Arrays.asList(
         "leaves",
         "water",
         "lava",
         "torch",
         "redstone_torch",
         "fire",
         "bed",
         "wooden_slab",
         "stone_slab",
         "stone_slab2",
         "double_wooden_slab",
         "double_stone_slab",
         "piston",
         "sticky_piston",
         "piston_extension",
         "log",
         "log2",
         "oak_stairs",
         "spruce_stairs",
         "birch_stairs",
         "jungle_stairs",
         "acacia_stairs",
         "dark_oak_stairs",
         "stone_stairs",
         "cobblestone_stairs",
         "brick_stairs",
         "stone_brick_stairs",
         "sandstone_stairs",
         "nether_brick_stairs",
         "quartz_stairs",
         "red_sandstone_stairs",
         "redstone_wire",
         "daylight_sensor",
         "farmland",
         "rails",
         "activator_rail",
         "detector_rail",
         "powered_rail",
         "ladder",
         "furnace",
         "chest",
         "trapped_chest",
         "sign",
         "dispenser",
         "dropper",
         "hopper",
         "lever",
         "pressure_plate",
         "button",
         "snow",
         "jukebox",
         "cake",
         "trapdoor",
         "anvil",
         "monster_egg",
         "stone_bricks",
         "quartz_block",
         "quartz_pillar",
         "chiseled_quartz_block",
         "oak_door",
         "spruce_door",
         "birch_door",
         "jungle_door",
         "acacia_door",
         "dark_oak_door",
         "wheat",
         "carrots",
         "potatoes",
         "beetroots",
         "cactus",
         "sugar_cane",
         "flower_pot",
         "skull"
      )
   );
   private final Map<String, BedPlates.BedData> bedPositions = new HashMap<>();
   private final Set<String> searchedBlocks = new HashSet<>();
   private final Map<String, ItemStack> stackCache = new HashMap<>();
   private final Set<Integer> yLevels = new HashSet<>();
   private final List<BedPlates.PlateRenderData> projectedPlates = new ArrayList<>();
   private boolean display = true;
   private boolean lastPressed;
   private int renderFrameId;
   private int lastProjectedFrameId = Integer.MIN_VALUE;
   private int lastOverlayFrameId = Integer.MIN_VALUE;

   public BedPlates() {
      super("BedPlates", "curxxed");
      this.tooltip("Renders bed defense layers above visible beds.");
      this.addToggle(new ToggleValue("Align top center", "alignTop", this));
      this.addToggle(new ToggleValue("Auto scale", "autoScale", this));
      this.addSlider(new SliderValue("Scale", 0.1, 1.5, 0.1, null, "scale", this, float.class));
      this.addSlider(new SliderValue("Plate opacity", 5.0, 100.0, 1.0, "%", "plateOpacity", this, float.class));
      this.addSlider(new SliderValue("Border opacity", 5.0, 100.0, 1.0, "%", "borderOpacity", this, float.class));
      this.addSlider(new SliderValue("Shadow opacity", 0.0, 100.0, 1.0, "%", "shadowOpacity", this, float.class));
      this.addSlider(new SliderValue("Render distance", 10.0, 200.0, 1.0, "blocks", "renderDistance", this, int.class));
      this.addSlider(new SliderValue("Y offset", -10.0, 10.0, 0.5, "blocks", "yOffset", this, float.class));
      this.addMode(new ModeValue("Display mode", Arrays.asList("Static", "Toggle", "Hold"), "displayMode", this));
      this.addBind(new BindValue("Display bind", "displayKey", this));
      this.addToggle(new ToggleValue("Bedwars only", "bedwarsOnly", this));
   }

   @EventTarget
   public void onClientTick(ClientTickEvent event) {
      if (event.getPhase() == Phase.POST) {
         if (this.mc.player != null && this.mc.level != null) {
            if (!this.bedwarsOnly || !Bedwars.GAME.isNotActive() || !Duels.BEDWARS.isNotActive()) {
               int ticks = this.mc.player.tickCount;
               if (ticks % 20 == 0) {
                  this.searchForBeds();
               }

               this.importKnownBeds();

               if (ticks % 3 == 0) {
                  this.findYLevels();
               }

               if (ticks % 300 == 0) {
                  this.searchedBlocks.clear();
               }

               this.updateBeds();
            }
         }
      }
   }

   @EventTarget
   public void onRenderWorldLast(RenderWorldLastEvent event) {

   }

   @EventTarget
   public void onRenderTick(RenderTickEvent event) {
      if (event.getPhase() == wtf.tatp.meowtils.event.RenderTickEvent.Phase.PRE) {
         this.renderFrameId++;
      }
   }

   @EventTarget
   public void onRenderGameOverlay(RenderGameOverlayEvent event) {
      if(mc.player==null || mc.level==null || (bedwarsOnly && Bedwars.GAME.isNotActive() && Duels.BEDWARS.isNotActive()) || !updateDisplayState())return;
      Draw.begin(event.getGraphics());
      try {
         float[] point=new float[2];
         for(BedData bed:bedPositions.values()){
            if(!bed.visible)continue;
            Map<String,Integer> layers=bed.layers==null||bed.layers.isEmpty()?Map.of("white_bed",1):bed.layers;
            Vec3 center=getBedCenter(bed.position1,bed.position2).add(0,yOffset,0);
            double distance=mc.gameRenderer.mainCamera().position().distanceTo(center);
            if(distance>renderDistance || !WorldOverlay.project(center.x,center.y,center.z,event.getGraphics().guiWidth(),event.getGraphics().guiHeight(),point))continue;
            float currentScale=autoScale?Math.max(.3f,scale*(1-(float)(distance/renderDistance))):scale;
            renderPlate(new PlateRenderData(point[0],point[1],currentScale,layers));
         }
      } finally { Draw.end(); }
   }

   @EventTarget
   public void onWorldEvent(WorldEvent event) {
      if (event.getType() == Type.LOAD || event.getType() == Type.UNLOAD) {
         this.clearData();
      }
   }

   public void onDisable() {
      this.clearData();
      this.display = true;
      this.lastPressed = false;
   }

   public void onReset() {
      this.clearData();
      this.display = true;
      this.lastPressed = false;
   }

   private void clearData() {
      this.bedPositions.clear();
      this.searchedBlocks.clear();
      this.stackCache.clear();
      this.projectedPlates.clear();
      this.yLevels.clear();
      this.lastProjectedFrameId = Integer.MIN_VALUE;
      this.lastOverlayFrameId = Integer.MIN_VALUE;
   }

   private boolean updateDisplayState() {
      if ("Static".equals(this.displayMode)) {
         this.display = true;
         this.lastPressed = false;
         return true;
      } else {
         boolean noGui = this.mc.gui.screen() == null;
         boolean keyPressed = this.displayKey != 0 && GLFW.glfwGetKey(mc.getWindow().handle(),this.displayKey)==GLFW.GLFW_PRESS;
         if ("Toggle".equals(this.displayMode)) {
            if (noGui && keyPressed && !this.lastPressed) {
               this.display = !this.display;
            }
         } else {
            this.display = noGui && keyPressed;
         }

         this.lastPressed = keyPressed;
         return this.display;
      }
   }

   private void renderPlate(BedPlates.PlateRenderData plate) {
      List<String> layers = new ArrayList<>(plate.layerCounts.keySet());
      if (!layers.isEmpty()) {
         float itemSize = 16.0F * plate.scale;
         float itemPadding = 2.0F * plate.scale;
         float boxSize = itemSize + itemPadding;
         float rectWidth = layers.size() * boxSize;
         float shellPadding = 2.4F * plate.scale;
         float bodyInset = 1.2F * plate.scale;
         float shadowInset = 0.8F * plate.scale;
         float startX = plate.x - rectWidth / 2.0F;
         float startY = this.alignTop ? plate.y : plate.y - boxSize;
         float shellX1 = startX - shellPadding;
         float shellY1 = startY - shellPadding;
         float shellX2 = startX + rectWidth + shellPadding;
         float shellY2 = startY + boxSize + shellPadding;
         float shellRadius = 5.0F * plate.scale;
         float bodyRadius = 4.2F * plate.scale;
         float bodyX1 = shellX1 + bodyInset;
         float bodyY1 = shellY1 + bodyInset;
         float bodyX2 = shellX2 - bodyInset;
         float bodyY2 = shellY2 - bodyInset;
         int shadowColor = ColorUtil.rgba(9, 11, 14, this.alphaFromPercent(this.shadowOpacity));
         int shellColor = ColorUtil.rgba(76, 92, 116, this.alphaFromPercent(this.borderOpacity));
         int bodyColor = ColorUtil.rgba(27, 31, 39, this.alphaFromPercent(this.plateOpacity));
         this.drawRoundedRect(
            shellX1 + shadowInset, shellY1 + shadowInset, shellX2 - shadowInset, shellY2 - shadowInset, Math.max(0.1F, shellRadius - shadowInset), shadowColor
         );
         this.drawRoundedRect(shellX1, shellY1, shellX2, shellY2, shellRadius, shellColor);
         this.drawRoundedRect(bodyX1, bodyY1, bodyX2, bodyY2, bodyRadius, bodyColor);

         for (int i = 0; i < layers.size(); i++) {
            String layer = layers.get(i);
            ItemStack stack = this.getStackFromName(layer);
            float itemX = startX + i * boxSize;
            var g=Draw.g();g.pose().pushMatrix();
            try {g.pose().translate(itemX+itemPadding/2,startY+itemPadding/2);g.pose().scale(plate.scale,plate.scale);g.item(stack,0,0);}finally{g.pose().popMatrix();}
         }
      }
   }

   private ItemStack getStackFromName(String name) {
      ItemStack cached = this.stackCache.get(name);
      if (cached != null) {
         return cached.copy();
      } else {
         ItemStack stack = this.toItemStack(name);
         this.stackCache.put(name, stack);
         return stack.copy();
      }
   }

   private ItemStack toItemStack(String key) {
      return switch(key){
         case "water" -> new ItemStack(Items.WATER_BUCKET);
         case "lava" -> new ItemStack(Items.LAVA_BUCKET);
         case "fire" -> new ItemStack(Items.FLINT_AND_STEEL);
         default -> {
            var id=Identifier.tryParse(key.contains(":")?key:"minecraft:"+key);
            var item=id==null?Items.BARRIER:BuiltInRegistries.ITEM.getValue(id);
            yield new ItemStack(item==null || item==Items.AIR?Items.BARRIER:item);
         }
      };
   }

   private boolean isNumber(String text) {
      if (text != null && !text.isEmpty()) {
         for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void importKnownBeds() {
      if (this.mc.player == null || this.mc.level == null) {
         return;
      }
      for (BlockPos pos : BedESP.BEDS) {
         if (!this.isBedAt(pos)) {
            continue;
         }
         BlockPos pair = this.findBedPair(pos);
         BlockPos[] normalized = this.normalizeBedPair(pos, pair == null ? pos : pair);
         this.rememberBed(normalized[0], normalized[1]);
      }
   }

   private void rememberBed(BlockPos first, BlockPos second) {
      String bedKey = this.getBedKey(first, second);
      this.bedPositions.remove(this.getBedKey(first, first));
      this.bedPositions.remove(this.getBedKey(second, second));
      if (this.bedPositions.containsKey(bedKey)) {
         return;
      }
      BedPlates.BedData bedData = new BedPlates.BedData(first, second);
      bedData.distance = this.mc.player.position().distanceTo(this.getBedCenter(first, second));
      bedData.lastDistance = bedData.distance;
      bedData.visible = true;
      bedData.layers = this.getBedDefenseLayers(first, second);
      bedData.lastCheck = System.currentTimeMillis();
      this.bedPositions.put(bedKey, bedData);
      this.yLevels.add(first.getY());
      this.yLevels.add(second.getY());
   }

   private void updateBeds() {
      if (!this.bedPositions.isEmpty()) {
         if (this.mc.player != null && this.mc.level != null) {
            Vec3 playerPos = this.mc.player.position();
            long now = System.currentTimeMillis();

            for (BedPlates.BedData bedData : this.bedPositions.values()) {
               bedData.lastDistance = bedData.distance;
               bedData.distance = playerPos.distanceTo(this.getBedCenter(bedData.position1, bedData.position2));
               bedData.visible = this.isBedVisible(bedData.position1, bedData.position2);
               if (bedData.visible) {
                  int delay = this.getDelay(bedData.distance);
                  if (now > bedData.lastCheck + delay) {
                     bedData.layers = this.getBedDefenseLayers(bedData.position1, bedData.position2);
                     bedData.lastCheck = now;
                  }
               }
            }
         }
      }
   }

   private int getDelay(double distance) {
      if (distance > 100.0) {
         return 4000;
      } else if (distance > 50.0) {
         return 3000;
      } else {
         return distance > 25.0 ? 2000 : 1000;
      }
   }

   private void searchForBeds() {
      if (this.mc.player != null && this.mc.level != null) {
         List<Player> players = new ArrayList<>(this.mc.level.players());
         Set<Integer> levels = new HashSet<>(this.yLevels);
         for (Player player : players) {
            int y = player.blockPosition().getY();
            for (int dy = -24; dy <= 4; dy++) {
               levels.add(y + dy);
            }
         }

         if (!levels.isEmpty()) {
            for (Player player : players) {
               BlockPos playerPos = player.blockPosition();
               int startX = playerPos.getX() - 20;
               int endX = playerPos.getX() + 20;
               int startZ = playerPos.getZ() - 20;
               int endZ = playerPos.getZ() + 20;

               for (Integer yLevel : levels) {
                  int y = yLevel;

                  for (int x = startX; x <= endX; x++) {
                     for (int z = startZ; z <= endZ; z++) {
                        String scanKey = "1" + x + "," + y + "," + z;
                        if (!this.searchedBlocks.contains(scanKey)) {
                           this.searchedBlocks.add(scanKey);
                           BlockPos pos = new BlockPos(x, y, z);
                           if (this.isBedAt(pos)) {
                              BlockPos pair = this.findBedPair(pos);
                              if (pair != null) {
                                 BlockPos[] normalized = this.normalizeBedPair(pos, pair);
                                 this.rememberBed(normalized[0], normalized[1]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void findYLevels() {
      if (this.mc.level != null) {
         for (Player player : this.mc.level.players()) {
            if (player.swinging && player.getMainHandItem() != null && player.getMainHandItem().getItem() instanceof BlockItem) {
               BlockPos base = player.blockPosition();

               for (int x = base.getX() - 4; x <= base.getX() + 4; x++) {
                  for (int y = base.getY() - 4; y <= base.getY() + 4; y++) {
                     for (int z = base.getZ() - 4; z <= base.getZ() + 4; z++) {
                        String scanKey = "2" + x + "," + y + "," + z;
                        if (!this.searchedBlocks.contains(scanKey)) {
                           this.searchedBlocks.add(scanKey);
                           if (this.isBedAt(new BlockPos(x, y, z))) {
                              this.yLevels.add(y);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private Map<String, Integer> getBedDefenseLayers(BlockPos position1, BlockPos position2) {
      Map<String, Integer> finalCounts = new HashMap<>();
      Set<BlockPos> scannedPos = new HashSet<>();
      scannedPos.add(position1);
      scannedPos.add(position2);
      int maxLayers = 5;
      int airLayers = 0;
      int bedMinY = Math.min(position1.getY(), position2.getY());

      for (int layer = 1; layer <= maxLayers; layer++) {
         Map<String, Integer> layerCounts = new HashMap<>();
         int layerTotalBlocks = 0;
         int layerAirBlocks = 0;
         int minX = Math.min(position1.getX(), position2.getX()) - layer;
         int maxX = Math.max(position1.getX(), position2.getX()) + layer;
         int minY = bedMinY;
         int maxY = Math.max(position1.getY(), position2.getY()) + layer;
         int minZ = Math.min(position1.getZ(), position2.getZ()) - layer;
         int maxZ = Math.max(position1.getZ(), position2.getZ()) + layer;

         for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
               for (int z = minZ; z <= maxZ; z++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  int dist1 = Math.max(
                     Math.abs(x - position1.getX()), Math.max(Math.abs(y - position1.getY()), Math.abs(z - position1.getZ()))
                  );
                  int dist2 = Math.max(
                     Math.abs(x - position2.getX()), Math.max(Math.abs(y - position2.getY()), Math.abs(z - position2.getZ()))
                  );
                  int minDist = Math.min(dist1, dist2);
                  if (minDist == layer && !scannedPos.contains(pos)) {
                     scannedPos.add(pos);
                     String type = this.getBlockType(pos);
                     layerCounts.compute(type, (k, old) -> old == null ? 1 : old + 1);
                     if ("air".equals(type)) {
                        layerAirBlocks++;
                     }

                     layerTotalBlocks++;
                  }
               }
            }
         }

         if (layerTotalBlocks != 0 && !((float)layerAirBlocks / layerTotalBlocks > 0.85F)) {
            for (Entry<String, Integer> entry : layerCounts.entrySet()) {
               String blockType = entry.getKey();
               int count = entry.getValue();
               if (!"air".equals(blockType) && !((float)count / layerTotalBlocks < 0.1F)) {
                  finalCounts.compute(blockType, (k, old) -> old == null ? count : old + count);
               }
            }
         } else if (++airLayers >= 2) {
            break;
         }
      }

      List<Entry<String, Integer>> sorted = new ArrayList<>(finalCounts.entrySet());
      sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
      Map<String, Integer> ordered = new LinkedHashMap<>();

      for (Entry<String, Integer> entryx : sorted) {
         ordered.put(entryx.getKey(), entryx.getValue());
      }

      return ordered;
   }

   private String getBlockType(BlockPos pos) {
      if(mc.level==null || !mc.level.hasChunkAt(pos))return "air";
      var state=mc.level.getBlockState(pos);
      if(state.isAir() || state.getBlock() instanceof BedBlock)return "air";
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
   }

   private boolean isBedVisible(BlockPos pos1, BlockPos pos2) {
      return this.isBedAt(pos1) || this.isBedAt(pos2);
   }

   private boolean isBedAt(BlockPos pos) {
      if (this.mc.level != null && this.mc.level.hasChunkAt(pos)) {
         BlockState state = this.mc.level.getBlockState(pos);
         return state.getBlock() instanceof BedBlock;
      } else {
         return false;
      }
   }

   private BlockPos findBedPair(BlockPos bedPos) {
      if (this.mc.level != null && this.mc.level.hasChunkAt(bedPos)) {
         BlockState state = this.mc.level.getBlockState(bedPos);
         if (!(state.getBlock() instanceof BedBlock)) {
            return null;
         } else {
            BedPart part = (BedPart)state.getValue(BedBlock.PART);
            Direction facing = (Direction)state.getValue(BedBlock.FACING);
            BlockPos pairPos = part == BedPart.HEAD ? bedPos.relative(facing.getOpposite()) : bedPos.relative(facing);
            if (this.isBedAt(pairPos)) {
               return pairPos;
            } else {
               for (Direction horizontal : Direction.Plane.HORIZONTAL) {
                  BlockPos check = bedPos.relative(horizontal);
                  if (this.isBedAt(check)) {
                     return check;
                  }
               }

               return null;
            }
         }
      } else {
         return null;
      }
   }

   private Vec3 getBedCenter(BlockPos pos1, BlockPos pos2) {
      return new Vec3(
         (pos1.getX() + pos2.getX()) / 2.0 + 0.5, pos1.getY() + 0.5, (pos1.getZ() + pos2.getZ()) / 2.0 + 0.5
      );
   }

   private BlockPos[] normalizeBedPair(BlockPos a, BlockPos b) {
      return this.comparePos(a, b) <= 0 ? new BlockPos[]{a, b} : new BlockPos[]{b, a};
   }

   private String getBedKey(BlockPos pos1, BlockPos pos2) {
      return pos1.getX()
         + ","
         + pos1.getY()
         + ","
         + pos1.getZ()
         + "|"
         + pos2.getX()
         + ","
         + pos2.getY()
         + ","
         + pos2.getZ();
   }

   private int comparePos(BlockPos a, BlockPos b) {
      if (a.getX() != b.getX()) {
         return Integer.compare(a.getX(), b.getX());
      } else {
         return a.getY() != b.getY()
            ? Integer.compare(a.getY(), b.getY())
            : Integer.compare(a.getZ(), b.getZ());
      }
   }

   private String normalizeName(Object nameObject) {
      if (nameObject == null) {
         return "";
      } else {
         String raw;
         if (nameObject instanceof Identifier) {
            raw = ((Identifier)nameObject).toString();
         } else {
            raw = String.valueOf(nameObject);
         }

         int idx = raw.indexOf(58);
         if (idx >= 0 && idx + 1 < raw.length()) {
            raw = raw.substring(idx + 1);
         }

         return raw.toLowerCase(Locale.ROOT);
      }
   }

   private void drawRoundedRect(float x, float y, float x2, float y2, float radius, int color) {
      Shapes.round(x,y,x2-x,y2-y,radius,color);
   }

   private int alphaFromPercent(float percent) {
      float clamped = Math.max(0.0F, Math.min(100.0F, percent));
      return Math.round(clamped / 100.0F * 255.0F);
   }

   private static class BedData {
      private final BlockPos position1;
      private final BlockPos position2;
      private double distance;
      private double lastDistance;
      private boolean visible;
      private long lastCheck;
      private Map<String, Integer> layers = new LinkedHashMap<>();

      private BedData(BlockPos position1, BlockPos position2) {
         this.position1 = position1;
         this.position2 = position2;
      }
   }

   private static class PlateRenderData {
      private final float x;
      private final float y;
      private final float scale;
      private final Map<String, Integer> layerCounts;

      private PlateRenderData(float x, float y, float scale, Map<String, Integer> layerCounts) {
         this.x = x;
         this.y = y;
         this.scale = scale;
         this.layerCounts = layerCounts;
      }
   }
}
