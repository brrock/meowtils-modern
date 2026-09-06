package wtf.tatp.meowtils.module.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeChangerTest {
    @Test
    void hourConversionMatchesOriginalSixAmEpoch() {
        assertEquals(0L, TimeChanger.toIngameTime(6));
        assertEquals(6000L, TimeChanger.toIngameTime(12));
        assertEquals(12000L, TimeChanger.toIngameTime(18));
        assertEquals(18000L, TimeChanger.toIngameTime(0));
        assertEquals(23000L, TimeChanger.toIngameTime(5));
    }

    @Test
    void applyKeepsDayCountAndReplacesTimeOfDay() {
        assertEquals(102000L, TimeChanger.applyClientDayTime(100000L, 6000L));
        assertEquals(18000L, TimeChanger.applyClientDayTime(0L, 18000L));
        assertEquals(100000L, TimeChanger.applyClientDayTime(100000L, Long.MIN_VALUE));
    }
}
