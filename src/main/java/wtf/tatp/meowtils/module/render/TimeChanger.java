package wtf.tatp.meowtils.module.render;
import java.time.LocalTime;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
public final class TimeChanger extends Module {
 private int tickCounter;
 public TimeChanger(){super("TimeChanger",Category.Render);tag(ModuleTag.LEGIT);tooltip("Changes time clientside.");}
 public static final long DAY_LENGTH = 24000L;
 public static long time(){var m=get(TimeChanger.class);if(m==null||!m.getState())return Long.MIN_VALUE;Object real=m.settingsStorage().get("realTime");if(Boolean.parseBoolean(String.valueOf(real)))return realTime();double hour=((Number)m.settingsStorage().getOrDefault("time",12d)).doubleValue();return toIngameTime(hour);}
 public static long realTime(){LocalTime n=LocalTime.now();return toIngameTime(n.getHour()+n.getMinute()/60d+n.getSecond()/3600d);}
 public static long toIngameTime(double hour){double shifted=hour-6;if(shifted<0)shifted+=24;return (long)(shifted*1000);}
 /** Keep the day count from {@code base} and replace the 0–23999 time-of-day with {@code value}. */
 public static long applyClientDayTime(long base,long value){if(value==Long.MIN_VALUE)return base;return base-base%DAY_LENGTH+value;}
 @EventTarget public void tick(ClientTickEvent e){if(e.getPhase()!=ClientTickEvent.Phase.POST||mc.player==null||mc.level==null||Boolean.parseBoolean(String.valueOf(settingsStorage().getOrDefault("realTime",false)))||++tickCounter<2){return;}tickCounter=0;int up=((Number)settingsStorage().getOrDefault("increaseKey",0d)).intValue();int down=((Number)settingsStorage().getOrDefault("decreaseKey",0d)).intValue();int hour=((Number)settingsStorage().getOrDefault("time",12d)).intValue();if(up>0&&com.mojang.blaze3d.platform.InputConstants.isKeyDown(mc.getWindow(),up)&&hour<24)settingsStorage().put("time",hour+1);else if(down>0&&com.mojang.blaze3d.platform.InputConstants.isKeyDown(mc.getWindow(),down)&&hour>0)settingsStorage().put("time",hour-1);}
}
