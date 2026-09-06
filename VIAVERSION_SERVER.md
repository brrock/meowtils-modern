# ViaVersion server reference (not ViaFabricPlus)

Cloned at `/Volumes/SSD/projects/ViaVersion`.

ViaVersion on the **server** lets a **newer client** (26.2) join a **lower server** (1.8.9-style / Hypixel). Do not use ViaFabricPlus.

## Read these first

- `common/src/main/java/com/viaversion/viaversion/protocols/v1_8to1_9/Protocol1_8To1_9.java` — swords, second hand, 1.8 → 1.9 rewrite
- `.../v1_8to1_9/packet/ClientboundPackets1_8.java`
- `.../v1_8to1_9/packet/ServerboundPackets1_8.java`
- `api/.../type/types/item/ItemType1_8.java`
- `.../v1_8to1_9/data/ArmorTypes1_8.java`
- `.../v1_8to1_9/data/EntityIds1_8.java`
- `.../v1_8to1_9/data/SoundCategories1_8.java`

## Semantics for this port

- Chat, titles, scoreboard, lore: still treat `§` and unformatted text after translation.
- Sword block: 1.8 use-item, not a shield. `Protocol1_8To1_9.isSword`.
- Items: modern names after rewrite; lore/custom NBT may still look 1.8.
- Sounds: 1.8 names (`random.orb`, `note.bass`, `mob.cat.meow`) are remapped.
- Chests: blank or `container.chest` window titles are normal on Hypixel-through-Via.
- Keep-alive / latency timing is not native 26.2.
