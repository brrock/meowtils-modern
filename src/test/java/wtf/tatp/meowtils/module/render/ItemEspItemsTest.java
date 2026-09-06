package wtf.tatp.meowtils.module.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ItemEspItemsTest {
    @Test
    void originalListsDoNotUseContainsDiamond() {
        assertTrue(ItemEspItems.match("diamond", "diamond", "diamond_sword"));
        assertTrue(ItemEspItems.match("diamond_sword", "diamond", "diamond_sword"));
        assertFalse(ItemEspItems.match("diamond_ore", "diamond", "diamond_sword"));
        assertFalse(ItemEspItems.match("diamond_block", "diamond", "diamond_sword"));
        assertFalse(ItemEspItems.match("diamond_horse_armor", "diamond", "diamond_sword"));
        assertTrue(ItemEspItems.match("gold_ingot", "gold_ingot", "golden_sword"));
        assertTrue(ItemEspItems.match("golden_sword", "gold_ingot", "golden_sword"));
        assertTrue(ItemEspItems.match("gold_sword", "gold_ingot", "golden_sword"));
        assertFalse(ItemEspItems.match("golden_hoe", "gold_ingot", "golden_sword"));
        assertTrue(ItemEspItems.match("emerald_block", "emerald", "emerald_block"));
        assertFalse(ItemEspItems.match("emerald_ore", "emerald", "emerald_block"));
    }
}
