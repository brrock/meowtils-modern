package meowtils.notifications.tenacity;

import java.util.ArrayDeque;
import meowtils.notifications.NotificationsExtension;
import meowtils.notifications.tenacity.font.Fonts;
import meowtils.notifications.vape.VapeRenderer;
import meowtils.notifications.vape.VapeType;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.RenderTickEvent.Phase;
import wtf.tatp.meowtils.event.api.EventPriority;
import wtf.tatp.meowtils.event.api.EventTarget;

public class NotificationHook implements AutoCloseable {
   @Override public void close(){StockNotifications.clear();NotificationManager.clear();VapeRenderer.clear();pendingTests.clear();}
   @EventTarget(priority=EventPriority.HIGHEST)
   public void onNotification(wtf.tatp.meowtils.event.NotificationEvent event){
      var settings=NotificationsExtension.get();if(settings!=null && settings.getState())StockNotifications.capture(event);
   }

   private static final long TOGGLE_DURATION = 1500L;
   private static final long DUPLICATE_WINDOW = 500L;
   private static final ArrayDeque<Object[]> pendingTests = new ArrayDeque<>();
   private static final long TEST_INTERVAL = 600L;
   private static long nextTestTime;
   private static final String SECTION = String.valueOf('§');
   private String lastKey;
   private long lastKeyTime;

   public static void queueTests() {
      pendingTests.clear();
      nextTestTime = 0L;
      pendingTests.add(new Object[]{"TestModule", SECTION + "aEnabled", "INFO", 1500L});
      pendingTests.add(new Object[]{"TestModule", SECTION + "cDisabled", "INFO", 1500L});
      pendingTests.add(new Object[]{"Requeue", "Requeuing..", "INFO", 1500L});
      pendingTests.add(new Object[]{"AutoBlacklist", "Auto-blacklisted Notch.", "ALERT", 1000L});
      pendingTests.add(new Object[]{"Blacklisted Player", "Notch", "WARNING", 2000L});
      pendingTests.add(new Object[]{"AntiCheat", "Notch" + SECTION + "7 failed " + SECTION + "cKillaura", "WARNING", 1500L});
   }

   private static void drainTests() {
      if (!pendingTests.isEmpty()) {
         long now = System.currentTimeMillis();
         if (now >= nextTestTime) {
            nextTestTime = now + 600L;
            Object[] test = pendingTests.poll();
            StockNotifications.show((String)test[0], (String)test[1], (String)test[2], (Long)test[3]);
         }
      }
   }

   @EventTarget
   public void onRenderTick(RenderTickEvent event) {
      if (event.getPhase() == Phase.PRE) {
         NotificationRenderer.markFrame();
      }
   }

   @EventTarget(
      priority = EventPriority.HIGHEST
   )
   public void onRenderGameOverlay(RenderGameOverlayEvent event) {
      NotificationsExtension settings = NotificationsExtension.get();
      if (settings != null) {
         if (!settings.getState()) {
            StockNotifications.clear();
            pendingTests.clear();
            if (!NotificationManager.getNotifications().isEmpty()) {
               NotificationManager.clear();
            }

            if (!VapeRenderer.getNotifications().isEmpty()) {
               VapeRenderer.clear();
            }
         } else {
            Fonts.init();
            if (Fonts.ready()) {
               drainTests();
               this.capture(settings);
            }

            wtf.tatp.meowtils.extension.render.Draw.begin(event.getGraphics());
         try { NotificationRenderer.markFrame();NotificationRenderer.render(); } finally { wtf.tatp.meowtils.extension.render.Draw.end(); }
         }
      }
   }

   private void capture(NotificationsExtension settings) {
      StockNotifications.Captured captured = StockNotifications.consume();
      if (captured != null) {
         String title = captured.title == null ? "" : captured.title;
         String message = captured.message == null ? "" : captured.message;
         if (!this.isDuplicate(captured.type + "\u0000" + title + "\u0000" + message)) {
            float seconds = (float)Math.max(captured.displayTime, 1L) / 1000.0F;
            if (settings.isStyle("Vape V4")) {
               VapeRenderer.post(mapVapeType(captured.type), title, message, captured.displayTime);
            } else {
               Boolean toggle = toggleState(message, captured.displayTime);
               if (toggle != null) {
                  postToggle(title, toggle, seconds);
               } else {
                  NotificationManager.post(mapType(captured.type), title, message, seconds);
               }
            }
         }
      }
   }

   private boolean isDuplicate(String key) {
      long now = System.currentTimeMillis();
      if (key.equals(this.lastKey) && now - this.lastKeyTime < 500L) {
         return true;
      } else {
         this.lastKey = key;
         this.lastKeyTime = now;
         return false;
      }
   }

   private static Boolean toggleState(String message, long displayTime) {
      if (displayTime != 1500L) {
         return null;
      } else {
         String plain = strip(message);
         if ("Enabled".equalsIgnoreCase(plain)) {
            return Boolean.TRUE;
         } else {
            return "Disabled".equalsIgnoreCase(plain) ? Boolean.FALSE : null;
         }
      }
   }

   private static void postToggle(String moduleName, boolean on, float seconds) {
      String title = "Module toggled";
      String description = moduleName + " was " + (on ? "§aenabled\r" : "§cdisabled\r");
      NotificationManager.post(on ? NotificationType.SUCCESS : NotificationType.DISABLE, "Module toggled", description, seconds);
   }

   private static VapeType mapVapeType(String stockType) {
      if ("ALERT".equals(stockType)) {
         return VapeType.WARNING;
      } else {
         return "WARNING".equals(stockType) ? VapeType.ALERT : VapeType.INFO;
      }
   }

   private static NotificationType mapType(String stockType) {
      return !"ALERT".equals(stockType) && !"WARNING".equals(stockType) ? NotificationType.INFO : NotificationType.WARNING;
   }

   private static String strip(String text) {
      StringBuilder out = new StringBuilder(text.length());

      for (int i = 0; i < text.length(); i++) {
         char c = text.charAt(i);
         if (c == 167) {
            i++;
         } else if (c != '\r' && c != '\n') {
            out.append(c);
         }
      }

      return out.toString().trim();
   }
}
