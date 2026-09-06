package meowtils.statsfaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderStringEvent;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.stats.util.BedwarsStatsUtil;

public class StatsFakerModule extends Extension {
   private static final Pattern LEVEL_PATTERN = Pattern.compile("Level\\s+([0-9,]+)");
   @Config
   public boolean enabled = false;
   @Config
   public int key = 0;
   @Config
   public String bedwarsMode = "Set";
   @Config
   public boolean customBedwarsLevel = false;
   @Config
   public String bedwarsStar = "100";
   @Config
   public boolean customTotalWins = false;
   @Config
   public String totalWins = "0";
   @Config
   public boolean customWinstreak = false;
   @Config
   public String winstreak = "0";
   @Config
   public boolean customTotalKills = false;
   @Config
   public String totalKills = "0";
   @Config
   public boolean customNetworkLevel = false;
   @Config
   public String networkLevel = "100";
   @Config
   public boolean customTokens = false;
   @Config
   public String tokens = "0";

   public StatsFakerModule() {
      super("StatsFaker", "Mega");
      this.info("Changes your account information for max ego boost.");
      this.addExpand(new ExpandValue("Bedwars", expand -> {
         expand.addToggle(new ToggleValue("Custom Bedwars level", "customBedwarsLevel", this));
         expand.addText(new TextValue("Star", "Numbers only", "bedwarsStar", this));
         expand.addToggle(new ToggleValue("Custom Total Wins", "customTotalWins", this));
         expand.addText(new TextValue("Total Wins", "Numbers only", "totalWins", this));
         expand.addToggle(new ToggleValue("Custom Winstreak", "customWinstreak", this));
         expand.addText(new TextValue("Winstreak", "Numbers only", "winstreak", this));
         expand.addToggle(new ToggleValue("Custom Total Kills", "customTotalKills", this));
         expand.addText(new TextValue("Total Kills", "Numbers only", "totalKills", this));
         expand.addToggle(new ToggleValue("Custom Tokens", "customTokens", this));
         expand.addText(new TextValue("Tokens", "Numbers only", "tokens", this));
      }, this));
      this.addExpand(new ExpandValue("Network", expand -> {
         expand.addToggle(new ToggleValue("Custom Network level", "customNetworkLevel", this));
         expand.addText(new TextValue("Level", "Numbers only", "networkLevel", this));
      }, this));
      this.addMode(new ModeValue("Mode", Arrays.asList("Set", "Add"), "bedwarsMode", this));
   }

   @EventTarget(
      priority = EventPriority.LOWEST
   )
   public void onRenderString(RenderStringEvent event) {
      if (this.enabled && event.getString() != null) {
         if (this.mc.player != null && this.mc.level != null) {
            String text = event.getString();
            boolean inBedwarsMatch = Bedwars.GAME.isActive() || Bedwars.PRE_GAME.isActive();
            if (!inBedwarsMatch || !this.isSidebarLine(text)) {
               if (Bedwars.LOBBY.isActive() && !inBedwarsMatch) {
                  if (this.customBedwarsLevel) {
                     this.replaceLobbyScoreboardLevel(event, text);
                     text = event.getString();
                     this.replaceLobbyHologramLevel(event, text);
                     text = event.getString();
                  }

                  if (this.customTotalWins) {
                     this.replaceLobbyTotalWinsScoreboard(event, text);
                     text = event.getString();
                     this.replaceLobbyTotalWinsHologram(event, text);
                     text = event.getString();
                  }

                  if (this.customWinstreak) {
                     this.replaceLobbyWinstreakHologram(event, text);
                     text = event.getString();
                  }

                  if (this.customTotalKills) {
                     this.replaceLobbyTotalKillsScoreboard(event, text);
                     text = event.getString();
                  }

                  if (this.customTokens) {
                     this.replaceLobbyTokensScoreboard(event, text);
                     text = event.getString();
                  }
               }

               if (this.customBedwarsLevel && Bedwars.GAME.isActive() && Bedwars.PRE_GAME.isNotActive()) {
                  this.replaceXpBarLevel(event, text, this.getBedwarsXpTargetLevel());
                  text = event.getString();
               }

               if (this.customNetworkLevel && !inBedwarsMatch) {
                  this.replaceXpBarLevel(event, text, this.getNetworkLevel());
                  text = event.getString();
                  this.replaceLobbyNetworkScoreboardLevel(event, text);
                  text = event.getString();
               }

               if (this.customBedwarsLevel && Bedwars.ALL.isActive()) {
                  this.replaceRewardSummaryLevelLine(event, text);
                  text = event.getString();
                  this.replaceOwnChatStar(event, text);
                  text = event.getString();
               }
            }
         }
      }
   }

   private void replaceLobbyScoreboardLevel(RenderStringEvent event, String text) {
      if (text.contains("Level")) {
         if (this.isSidebarLine(text)) {
            int levelPrefixIndex = text.indexOf("Level: ");
            if (levelPrefixIndex != -1) {
               int valueStart = levelPrefixIndex + "Level: ".length();
               int resolvedLevel = this.resolveBedwarsValue(text, "Level: ", this.getBedwarsStar());
               String formattedLevel = this.getFormattedStarNoBrackets(resolvedLevel);
               event.setString(text.substring(0, valueStart) + formattedLevel);
            }
         }
      }
   }

   private void replaceXpBarLevel(RenderStringEvent event, String text, int fakeLevel) {
      String clean = ColorUtil.unformattedText(text);
      if (clean != null) {
         if (clean.equals(String.valueOf(this.mc.player.experienceLevel))) {
            if (this.isXpBarRenderContext()) {
               if (!this.isScoreboardRenderContext()) {
                  if (!this.isSidebarLine(text)) {
                     if (!this.isItemOverlayRender()) {
                        event.setString(String.valueOf(fakeLevel));
                     }
                  }
               }
            }
         }
      }
   }

   private void replaceLobbyHologramLevel(RenderStringEvent event, String text) {
      String clean = ColorUtil.unformattedText(text);
      if (clean != null) {
         int levelPrefixIndex = clean.indexOf("Your Level: ");
         if (levelPrefixIndex != -1) {
            int valueStart = levelPrefixIndex + "Your Level: ".length();
            int resolvedLevel = this.resolveBedwarsValue(clean, "Your Level: ", this.getBedwarsStar());
            String replaced = clean.substring(0, valueStart) + BedwarsStatsUtil.getFormattedLevel(resolvedLevel);
            event.setString(replaced);
         }
      }
   }

   private void replaceLobbyNetworkScoreboardLevel(RenderStringEvent event, String text) {
      if (this.isSidebarLine(text)) {
         int levelPrefixIndex = text.indexOf("Hypixel Level: ");
         if (levelPrefixIndex != -1) {
            int valueStart = levelPrefixIndex + "Hypixel Level: ".length();
            event.setString(text.substring(0, valueStart) + "§a" + this.getNetworkLevel());
         }
      }
   }

   private void replaceLobbyTotalWinsScoreboard(RenderStringEvent event, String text) {
      if (this.isSidebarLine(text)) {
         int prefixIndex = text.indexOf("Total Wins: ");
         if (prefixIndex != -1) {
            int valueStart = prefixIndex + "Total Wins: ".length();
            int resolved = this.resolveBedwarsValue(text, "Total Wins: ", this.getTotalWinsValue());
            event.setString(text.substring(0, valueStart) + "§a" + this.formatWithCommas(resolved));
         }
      }
   }

   private void replaceLobbyTotalWinsHologram(RenderStringEvent event, String text) {
      if (!this.isSidebarLine(text)) {
         String clean = ColorUtil.unformattedText(text);
         if (clean != null) {
            int prefixIndex = clean.indexOf("Total Wins: ");
            if (prefixIndex != -1) {
               int valueStart = prefixIndex + "Total Wins: ".length();
               int resolved = this.resolveBedwarsValue(clean, "Total Wins: ", this.getTotalWinsValue());
               event.setString(clean.substring(0, valueStart) + "§a" + this.formatWithCommas(resolved));
            }
         }
      }
   }

   private void replaceLobbyWinstreakHologram(RenderStringEvent event, String text) {
      if (!this.isSidebarLine(text)) {
         String clean = ColorUtil.unformattedText(text);
         if (clean != null) {
            int prefixIndex = clean.indexOf("Current Winstreak: ");
            if (prefixIndex != -1) {
               int valueStart = prefixIndex + "Current Winstreak: ".length();
               int resolved = this.resolveBedwarsValue(clean, "Current Winstreak: ", this.getWinstreakValue());
               event.setString(clean.substring(0, valueStart) + "§a" + this.formatWithCommas(resolved));
            }
         }
      }
   }

   private void replaceLobbyTotalKillsScoreboard(RenderStringEvent event, String text) {
      if (this.isSidebarLine(text)) {
         int prefixIndex = text.indexOf("Total Kills: ");
         if (prefixIndex != -1) {
            int valueStart = prefixIndex + "Total Kills: ".length();
            int resolved = this.resolveBedwarsValue(text, "Total Kills: ", this.getTotalKillsValue());
            event.setString(text.substring(0, valueStart) + "§a" + this.formatWithCommas(resolved));
         }
      }
   }

   private void replaceLobbyTokensScoreboard(RenderStringEvent event, String text) {
      if (this.isSidebarLine(text)) {
         int prefixIndex = text.indexOf("Tokens: ");
         if (prefixIndex != -1) {
            int valueStart = prefixIndex + "Tokens: ".length();
            int resolved = this.resolveBedwarsValue(text, "Tokens: ", this.getTokensValue());
            event.setString(text.substring(0, valueStart) + "§2" + this.formatWithCommas(resolved));
         }
      }
   }

   private void replaceOwnChatStar(RenderStringEvent event, String text) {
      String clean = ColorUtil.unformattedText(text);
      if (clean != null) {
         if (clean.contains(this.mc.player.getName().getString() + ":")) {
            int firstStarIndex = this.getFirstStarIndex(text);
            if (firstStarIndex != -1) {
               int openBracket = text.lastIndexOf(91, firstStarIndex);
               int closeBracket = text.indexOf(93, firstStarIndex);
               if (openBracket != -1 && closeBracket != -1 && closeBracket > openBracket) {
                  int targetLevel = this.getBedwarsStar();
                  if ("Add".equals(this.bedwarsMode)) {
                     String currentStarGroup = text.substring(openBracket, closeBracket + 1);
                     targetLevel += this.extractStarLevel(currentStarGroup);
                  }

                  String replacement = BedwarsStatsUtil.getFormattedLevel(Math.max(0, targetLevel));
                  String trailing = text.substring(closeBracket + 1);

                  while (trailing.startsWith("  ")) {
                     trailing = trailing.substring(1);
                  }

                  if (!trailing.isEmpty() && trailing.charAt(0) != ' ') {
                     trailing = " " + trailing;
                  }

                  event.setString(text.substring(0, openBracket) + replacement + trailing);
               }
            }
         }
      }
   }

   private int extractStarLevel(String bracketGroup) {
      String clean = ColorUtil.unformattedText(bracketGroup);
      if (clean == null) {
         return 0;
      } else {
         int firstStar = this.getFirstStarIndex(clean);
         if (firstStar == -1) {
            return 0;
         } else {
            StringBuilder digits = new StringBuilder();

            for (int i = 0; i < firstStar; i++) {
               char c = clean.charAt(i);
               if (c >= '0' && c <= '9' || c == ',') {
                  digits.append(c);
               }
            }

            return digits.length() == 0 ? 0 : this.parseNonNegativeInt(digits.toString(), 0);
         }
      }
   }

   private void replaceRewardSummaryLevelLine(RenderStringEvent event, String text) {
      if (!this.isSidebarLine(text)) {
         String clean = ColorUtil.unformattedText(text);
         if (clean != null && clean.contains("Level ")) {
            Matcher matcher = LEVEL_PATTERN.matcher(text);
            List<int[]> matches = new ArrayList<>();
            List<Integer> existingValues = new ArrayList<>();
            List<Integer> replacedValues = new ArrayList<>();

            for (int matchIndex = 0; matcher.find(); matchIndex++) {
               int existing = this.parseNonNegativeInt(matcher.group(1), 0);
               int replaced = "Add".equals(this.bedwarsMode) ? existing + this.getBedwarsStar() : this.getBedwarsStar() + matchIndex;
               matches.add(new int[]{matcher.start(), matcher.end()});
               existingValues.add(existing);
               replacedValues.add(Math.max(0, replaced));
            }

            if (!matches.isEmpty()) {
               if (matches.size() < 2) {
                  int[] single = matches.get(0);
                  String replacement = "Level " + replacedValues.get(0);
                  event.setString(text.substring(0, single[0]) + replacement + text.substring(single[1]));
               } else {
                  int[] first = matches.get(0);
                  int[] second = matches.get(1);
                  String firstReplacement = "Level " + replacedValues.get(0);
                  String secondReplacement = "Level " + replacedValues.get(1);
                  String beforeFirst = text.substring(0, first[0]);
                  String betweenOriginal = text.substring(first[1], second[0]);
                  String afterSecond = text.substring(second[1]);
                  int leadingSpaces = 0;

                  while (leadingSpaces < betweenOriginal.length() && betweenOriginal.charAt(leadingSpaces) == ' ') {
                     leadingSpaces++;
                  }

                  String betweenRest = betweenOriginal.substring(leadingSpaces);
                  int minGap = 6;
                  int originalFirstLevel = Math.max(0, existingValues.get(0));
                  int fakeFirstLevel = Math.max(0, replacedValues.get(0));
                  int originalFirstDigits = String.valueOf(originalFirstLevel).length();
                  int fakeFirstDigits = String.valueOf(fakeFirstLevel).length();
                  int growth = Math.max(0, fakeFirstDigits - originalFirstDigits) * 2;
                  int newLeadingSpaces = Math.max(minGap, leadingSpaces - growth);
                  StringBuilder adjustedBetween = new StringBuilder(newLeadingSpaces + betweenRest.length());

                  for (int i = 0; i < newLeadingSpaces; i++) {
                     adjustedBetween.append(' ');
                  }

                  adjustedBetween.append(betweenRest);
                  event.setString(beforeFirst + firstReplacement + adjustedBetween + secondReplacement + afterSecond);
               }
            }
         }
      }
   }

   private int getFirstStarIndex(String text) {
      int star1 = text.indexOf(10027);
      int star2 = text.indexOf(10026);
      int star3 = text.indexOf(9885);
      int star4 = text.indexOf(10021);
      int best = -1;
      if (star1 != -1) {
         best = star1;
      }

      if (star2 != -1 && (best == -1 || star2 < best)) {
         best = star2;
      }

      if (star3 != -1 && (best == -1 || star3 < best)) {
         best = star3;
      }

      if (star4 != -1 && (best == -1 || star4 < best)) {
         best = star4;
      }

      return best;
   }

   private String getFormattedStarNoBrackets(int level) {
      return BedwarsStatsUtil.getFormattedLevel(level).replace("[", "").replace("]", "");
   }

   private int getBedwarsStar() {
      if (this.bedwarsStar == null) {
         return 100;
      } else {
         try {
            int value = Integer.parseInt(this.bedwarsStar.trim());
            return Math.max(0, value);
         } catch (NumberFormatException var2) {
            return 100;
         }
      }
   }

   private int getNetworkLevel() {
      if (this.networkLevel == null) {
         return 100;
      } else {
         try {
            int value = Integer.parseInt(this.networkLevel.trim());
            return Math.max(0, value);
         } catch (NumberFormatException var2) {
            return 100;
         }
      }
   }

   private int getBedwarsXpTargetLevel() {
      int configured = this.getBedwarsStar();
      return "Add".equals(this.bedwarsMode) ? Math.max(0, this.mc.player.experienceLevel + configured) : configured;
   }

   private int getTotalWinsValue() {
      return this.parseNonNegativeInt(this.totalWins, 0);
   }

   private int getWinstreakValue() {
      return this.parseNonNegativeInt(this.winstreak, 0);
   }

   private int getTotalKillsValue() {
      return this.parseNonNegativeInt(this.totalKills, 0);
   }

   private int getTokensValue() {
      return this.parseNonNegativeInt(this.tokens, 0);
   }

   private int parseNonNegativeInt(String input, int fallback) {
      if (input == null) {
         return fallback;
      } else {
         try {
            int value = Integer.parseInt(input.trim().replace(",", ""));
            return Math.max(0, value);
         } catch (NumberFormatException var4) {
            return fallback;
         }
      }
   }

   private String formatWithCommas(int value) {
      return String.format(Locale.US, "%,d", value);
   }

   private int resolveBedwarsValue(String text, String prefix, int configuredValue) {
      if (!"Add".equals(this.bedwarsMode)) {
         return configuredValue;
      } else {
         String clean = ColorUtil.unformattedText(text);
         if (clean == null) {
            return configuredValue;
         } else {
            int prefixIndex = clean.indexOf(prefix);
            if (prefixIndex == -1) {
               return configuredValue;
            } else {
               int valueStart = prefixIndex + prefix.length();
               int currentValue = this.parseLeadingInt(clean.substring(valueStart));
               return Math.max(0, currentValue + configuredValue);
            }
         }
      }
   }

   private int parseLeadingInt(String value) {
      if (value != null && !value.isEmpty()) {
         int i = 0;

         while (i < value.length() && value.charAt(i) == ' ') {
            i++;
         }

         StringBuilder number;
         for (number = new StringBuilder(); i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c < '0' || c > '9') && c != ',') {
               break;
            }

            number.append(c);
         }

         if (number.length() == 0) {
            return 0;
         } else {
            try {
               return Integer.parseInt(number.toString().replace(",", ""));
            } catch (NumberFormatException var5) {
               return 0;
            }
         }
      } else {
         return 0;
      }
   }

   private boolean isItemOverlayRender() {
      return java.util.Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(e ->
          e.getClassName().equals("net.minecraft.client.gui.GuiGraphicsExtractor") && e.getMethodName().toLowerCase(java.util.Locale.ROOT).contains("item"));
   }

   private boolean isXpBarRenderContext() {
      return java.util.Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(e ->
          e.getClassName().startsWith("net.minecraft.client.gui.") && (e.getMethodName().toLowerCase(java.util.Locale.ROOT).contains("experience") || e.getClassName().endsWith(".ExperienceBar")));
   }

   private boolean isScoreboardRenderContext() {
      return java.util.Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(e ->
          e.getClassName().startsWith("net.minecraft.client.gui.") && e.getMethodName().toLowerCase(java.util.Locale.ROOT).contains("scoreboard"));
   }

   private boolean isSidebarLine(String text) {
      String target = ColorUtil.plainLower(text);
      return !target.isEmpty() && wtf.tatp.meowtils.util.ScoreboardUtil.getSidebarLines().contains(target);
   }
}
