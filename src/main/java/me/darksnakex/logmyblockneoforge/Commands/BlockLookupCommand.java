package me.darksnakex.blocktracker.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.darksnakex.blocktracker.Utils.WorldBlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Objects;

import static me.darksnakex.blocktracker.Events.BlockEventHandler.getBlockHistory;
import static me.darksnakex.blocktracker.Events.BlockEventHandler.readHistory;
import static me.darksnakex.blocktracker.Logmyblock.prefix;

public class BlockLookupCommand {

    public BlockLookupCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("logmyblock")
                .then(Commands.literal("lookup")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(this::lookupBlockHistory)
                                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                                        .executes(this::lookupBlockHistory)))))));
    }

    private int lookupBlockHistory(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel world = source.getLevel();
        int x = IntegerArgumentType.getInteger(context, "x");
        int y = IntegerArgumentType.getInteger(context, "y");
        int z = IntegerArgumentType.getInteger(context, "z");
        int numero = context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals("page")) ? IntegerArgumentType.getInteger(context, "page") : 1;
        BlockPos blockPos = new BlockPos(x, y, z);
        WorldBlockPos worldBlockPos = new WorldBlockPos(world.toString(), blockPos);

        List<String> history = getBlockHistory(worldBlockPos);

        if (history.isEmpty()) {
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.literal(prefix + "No more data found."));
        } else {
            Objects.requireNonNull(source.getEntity()).sendSystemMessage(Component.literal("----- "+prefix+"----- §7"+ blockPos.toShortString()+"§f ----- §7"+ "Page "+numero));
            readHistory(history,numero, source.getPlayer(), blockPos.toShortString());
        }

        return 1;
    }
}