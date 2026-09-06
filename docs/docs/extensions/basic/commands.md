# Commands

Extensions register **Fabric client commands** (Brigadier), not the old `ClientCommand` class.

```java
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.Meowtils;

Extension.registerCommand(
    LiteralArgumentBuilder.<FabricClientCommandSource>literal("hello")
        .executes(context -> {
            Meowtils.addMessage("Command ran!");
            return 1;
        })
);
```

Aliases:

```java
wtf.tatp.meowtils.CommandManager.register("hello",
    root -> root.executes(c -> { Meowtils.addMessage("hi"); return 1; }),
    "hi", "hey");
```

`init()` may run before the dispatcher exists. `CommandManager` queues the builder and installs it when Fabric registers client commands, and again on `/reload`.
