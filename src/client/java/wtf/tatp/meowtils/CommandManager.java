package wtf.tatp.meowtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.CommandDispatcher;

/** Queued client-command registry; extensions may register commands during init(). */
public final class CommandManager {
    private static final List<LiteralArgumentBuilder<FabricClientCommandSource>> commands = new CopyOnWriteArrayList<>();
    private static volatile CommandDispatcher<FabricClientCommandSource> dispatcher;
    private CommandManager() {}

    public static void register(LiteralArgumentBuilder<FabricClientCommandSource> command) {
        if (command == null) throw new IllegalArgumentException("Command cannot be null");
        commands.add(command);
        CommandDispatcher<FabricClientCommandSource> current = dispatcher;
        if (current != null) current.register(command);
    }

    /** Builds the same tree for a name and each alias. */
    public static void register(String name, Function<LiteralArgumentBuilder<FabricClientCommandSource>, LiteralArgumentBuilder<FabricClientCommandSource>> configure, String... aliases) {
        register(configure.apply(LiteralArgumentBuilder.literal(name)));
        if (aliases == null) return;
        for (String alias : aliases) {
            if (alias == null || alias.isBlank() || alias.equals(name)) continue;
            register(configure.apply(LiteralArgumentBuilder.literal(alias)));
        }
    }

    public static List<LiteralArgumentBuilder<FabricClientCommandSource>> commands() { return List.copyOf(commands); }
    public static void install(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        CommandManager.dispatcher = dispatcher;
        commands.forEach(dispatcher::register);
    }
}
