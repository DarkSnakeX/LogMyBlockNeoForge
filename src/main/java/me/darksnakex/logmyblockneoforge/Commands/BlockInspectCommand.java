package me.darksnakex.blocktracker.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.darksnakex.blocktracker.Logmyblock.prefix;

public class BlockInspectCommand {

    private static final Set<UUID> inspectingPlayers = new HashSet<>();


    public BlockInspectCommand(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("logmyblock")
                .then(Commands.literal("inspect")
                                .executes(this::blockInspect)));
    }

    private int blockInspect(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID playerUUID = source.getEntity().getUUID();

        if (inspectingPlayers.contains(playerUUID)) {
            inspectingPlayers.remove(playerUUID);
            source.getEntity().sendSystemMessage(Component.literal(prefix + "Inspection mode disabled."));
        } else {
            inspectingPlayers.add(playerUUID);
            source.getEntity().sendSystemMessage(Component.literal(prefix + "Inspection mode enabled."));
        }

        return 1;
    }

    public static boolean isInspecting(UUID playerUUID) {
        return inspectingPlayers.contains(playerUUID);
    }
}