# Extension Limitations

Extensions load at runtime from a `URLClassLoader`. They cannot transform game classes.

## Mixins

??? failure "Not supported"

    Mixins are not supported inside a `.meowtils` archive.

If you need mixins, write a normal Fabric mod instead.

## Reflection

??? success "Supported"

    Reflection works.

26.2 is not Notch-obfuscated. Use the named Minecraft / Fabric APIs. Do not look up `func_` / `field_` names — the loader treats those as a 1.8.9 leftover and refuses the archive.

## Fabric callbacks

You can call Fabric APIs from an extension because the parent classloader is the Meowtils / game loader. Prefer Meowtils events so `/reload` can unregister your listener.

## Rendering

No raw OpenGL. Use `HudFont`, `Meowtils.drawString(graphics, ...)`, `RenderWorldLastEvent.submitOutline`, or `WorldOverlay`.
