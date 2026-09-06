# Server Safety

Meowtils is not a cheat client. Features that would be unfair on a strict server start **off**. You choose what to enable.

Some modules perform actions for you (AutoChest with a low delay, some DelayRemover options). On a strict anticheat that can still get you flagged. Use them at your own risk.

## Restricting features

The [GUI module](../modules/meowtils/gui.md) can hide modules by risk label.

## Risk labels

!!! Success "Legit"

    No real combat advantage, or the same idea already exists in common QOL mods. Treated as no-risk.

!!! Abstract "Safe"

    Client-side only. May still be an advantage (ESP, overlays). Treated as low-risk because the server does not see the render.

!!! Danger "Blatant"

    Performs actions or movement the server can see. Treated as high-risk.

## Always-on pieces

Commands (`/meow` and the rest) do not enable modules. Auto-update only replaces the local jar.
