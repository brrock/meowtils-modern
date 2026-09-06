package wtf.tatp.meowtils.module.advanced;

import org.junit.jupiter.api.Test;
import wtf.tatp.meowtils.module.advanced.AutoChestGate.Kind;

import static org.junit.jupiter.api.Assertions.*;

class AutoChestGateTest {
    private static final String LOCAL_CHEST = "Chest";
    private static final String LOCAL_ENDER = "Ender Chest";

    @Test
    void hypixelBlankEnderDefaultAllowsDeposit() {
        Kind kind = AutoChestGate.classify(false, "", true, LOCAL_CHEST, LOCAL_ENDER);
        assertEquals(Kind.BLANK, kind);
        assertTrue(AutoChestGate.allow(true, false, kind, false));
    }

    @Test
    void hypixelBlankNormalChestRequiresToggle() {
        Kind kind = AutoChestGate.classify(false, "", true, LOCAL_CHEST, LOCAL_ENDER);
        assertFalse(AutoChestGate.allow(true, false, kind, false) && false);
        assertTrue(AutoChestGate.allow(false, true, kind, false));
        assertFalse(AutoChestGate.allow(false, false, kind, false));
    }

    @Test
    void shopLoreOnBlankTitleIsRejected() {
        Kind kind = AutoChestGate.classify(false, "", true, LOCAL_CHEST, LOCAL_ENDER);
        assertFalse(AutoChestGate.allow(true, true, kind, true));
    }

    @Test
    void viaEnderTranslationKeyAndI18n() {
        assertEquals(Kind.ENDER, AutoChestGate.classify(false, "container.enderchest", true, LOCAL_CHEST, LOCAL_ENDER));
        assertEquals(Kind.ENDER, AutoChestGate.classify(false, "§5Ender Chest", true, LOCAL_CHEST, LOCAL_ENDER));
        assertEquals(Kind.ENDER, AutoChestGate.classify(false, "Enderchest", false, LOCAL_CHEST, LOCAL_ENDER));
        assertTrue(AutoChestGate.allow(true, false, Kind.ENDER, false));
        assertFalse(AutoChestGate.allow(false, true, Kind.ENDER, false));
    }

    @Test
    void viaChestTranslationKeyAndI18n() {
        assertEquals(Kind.NORMAL, AutoChestGate.classify(false, "container.chest", true, LOCAL_CHEST, LOCAL_ENDER));
        assertEquals(Kind.NORMAL, AutoChestGate.classify(false, "Chest", false, LOCAL_CHEST, LOCAL_ENDER));
        assertTrue(AutoChestGate.allow(false, true, Kind.NORMAL, false));
        assertFalse(AutoChestGate.allow(true, false, Kind.NORMAL, false));
    }

    @Test
    void typedEnderChestWinsOverBlankTitle() {
        assertEquals(Kind.ENDER, AutoChestGate.classify(true, "", true, LOCAL_CHEST, LOCAL_ENDER));
        assertTrue(AutoChestGate.allow(true, false, Kind.ENDER, false));
    }

    @Test
    void namedShopAndOtherTitlesAreRejected() {
        assertEquals(Kind.OTHER, AutoChestGate.classify(false, "Quick Buy", true, LOCAL_CHEST, LOCAL_ENDER));
        assertEquals(Kind.OTHER, AutoChestGate.classify(false, "Upgrades & Traps", true, LOCAL_CHEST, LOCAL_ENDER));
        assertFalse(AutoChestGate.allow(true, true, Kind.OTHER, false));
    }

    @Test
    void resourceChecksMatchOriginalToggles() {
        assertTrue(AutoChestGate.allowedResource("gold", true, true, true, true));
        assertFalse(AutoChestGate.allowedResource("gold", true, false, true, true));
        assertFalse(AutoChestGate.allowedResource("diamond_sword", true, true, true, true));
        assertTrue(AutoChestGate.allowedResource("diamond", false, false, true, false));
    }

    @Test
    void viaResourceItemIdsMatchBedwarsAliases() {
        assertEquals("iron", AutoChestGate.resourceKindFromId("iron_ingot"));
        assertEquals("gold", AutoChestGate.resourceKindFromId("golden_ingot"));
        assertEquals("diamond", AutoChestGate.resourceKindFromId("diamond"));
        assertEquals("", AutoChestGate.resourceKindFromId("diamond_sword"));
        assertTrue(AutoChestGate.allowedResourceId("iron_ingot", true, false, false, false));
        assertTrue(AutoChestGate.allowedResourceId("golden_ingot", false, true, false, false));
        assertFalse(AutoChestGate.allowedResourceId("diamond_sword", true, true, true, true));
        assertTrue(AutoChestGate.allowedResourceId("diamond", false, false, true, false));
    }

    @Test
    void namedShopTitlesRejectEvenWhenChestKindWouldAllow() {
        Kind quickBuy = AutoChestGate.classify(false, "Quick Buy", true, LOCAL_CHEST, LOCAL_ENDER);
        assertEquals(Kind.OTHER, quickBuy);
        assertFalse(AutoChestGate.allow(true, true, quickBuy, true));
        assertFalse(AutoChestGate.allow(true, true, quickBuy, false));
    }
}
