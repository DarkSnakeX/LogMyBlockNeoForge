package me.darksnakex.logmyblockneoforge.Events;

import me.darksnakex.logmyblockneoforge.Commands.BlockInspectCommand;
import me.darksnakex.logmyblockneoforge.Utils.WorldBlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;


import java.time.Instant;
import java.time.Duration;
import java.util.*;

import static me.darksnakex.logmyblockneoforge.LogMyBlockNeoForge.prefix;



public class BlockEventHandler {

    public static Map<WorldBlockPos, List<String>> blockEventMap = new LinkedHashMap<>();
    public static Map<WorldBlockPos, List<Instant>> blockEventTimes = new LinkedHashMap<>();
    private final Map<UUID, Instant> lastInspectTime = new LinkedHashMap<>();

    @SubscribeEvent
    public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            UUID playerUUID = event.getEntity().getUUID();
            String blockName = event.getLevel().getBlockState(event.getPos()).getBlock().getName().getString();

            if (BlockInspectCommand.isInspecting(playerUUID)) {
                event.setCanceled(true);
                Instant now = Instant.now();
                if (lastInspectTime.containsKey(playerUUID) && Duration.between(lastInspectTime.get(playerUUID), now).getSeconds() < 0.2) {
                    //event.getEntity().sendSystemMessage(Component.literal(prefix + "Please wait before inspecting again."));
                    return;
                }
                lastInspectTime.put(playerUUID, now);


                WorldBlockPos worldBlockPos = new WorldBlockPos(event.getLevel().toString(), event.getPos());
                List<String> history = getBlockHistory(worldBlockPos);

                if (history.isEmpty()) {
                    event.getEntity().sendSystemMessage(Component.literal(prefix + "There is no event history for this block."));
                } else {
                    event.getEntity().sendSystemMessage(Component.literal("----- "+prefix+"----- §7"+ event.getPos().toShortString()+"§f ----- §7"+ "Page 1"));
                    readHistory(history,1, event.getEntity(), event.getPos().toShortString());
                }
            } else {
                WorldBlockPos worldBlockPos = new WorldBlockPos(event.getLevel().toString(), event.getPos());
                String eventMessage = formatEventMessage(" right clicked ", event.getEntity().getName().getString(), blockName);
                blockEventMap.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(eventMessage);
                blockEventTimes.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(Instant.now());
            }
        }
    }

    public static void readHistory(List<String> history, int cont, Player player, String pos) {
        int cantidadElementos = 7;
        int totalElementos = history.size();

        int indiceFinal = totalElementos - (cont - 1) * cantidadElementos;
        int indiceInicial = indiceFinal - cantidadElementos;

        indiceInicial = Math.max(0, indiceInicial);

        indiceFinal = Math.min(totalElementos, indiceFinal);

        if (indiceInicial < indiceFinal) {

            for (int i = indiceFinal - 1; i >= indiceInicial; i--) {
                player.sendSystemMessage(Component.literal(history.get(i)));
            }
            player.sendSystemMessage(Component.literal(""));

            if (indiceFinal <= totalElementos) {
                MutableComponent clickableText = Component.literal("§2Next page -->");
                clickableText.withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/logmyblock lookup " + pos.replace(",","") + " " + (cont + 1))));
                player.sendSystemMessage(clickableText);
            }
        } else {
            player.sendSystemMessage(Component.literal("§2No more data available"));
        }
    }


    @SubscribeEvent
    public void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            UUID playerUUID = event.getEntity().getUUID();
            String blockName = event.getLevel().getBlockState(event.getPos()).getBlock().getName().getString();

            if (BlockInspectCommand.isInspecting(playerUUID)) {
                event.setCanceled(true);
                Instant now = Instant.now();
                if (lastInspectTime.containsKey(playerUUID) && Duration.between(lastInspectTime.get(playerUUID), now).getSeconds() < 0.2) {
                    //event.getEntity().sendSystemMessage(Component.literal(prefix + "Please wait before inspecting again."));
                    return;
                }
                lastInspectTime.put(playerUUID, now);

                WorldBlockPos worldBlockPos = new WorldBlockPos(event.getLevel().toString(), event.getPos());
                List<String> history = getBlockHistory(worldBlockPos);

                if (history.isEmpty()) {
                    event.getEntity().sendSystemMessage(Component.literal(prefix + "There is no event history for this block."));
                } else {
                    event.getEntity().sendSystemMessage(Component.literal("----- "+prefix+"----- §7"+ event.getPos().toShortString()+"§f ----- §7"+ "Page 1"));
                    readHistory(history,1, event.getEntity(), event.getPos().toShortString());
                }
            } else {
                WorldBlockPos worldBlockPos = new WorldBlockPos(event.getLevel().toString(), event.getPos());
                String eventMessage = formatEventMessage(" left clicked ", event.getEntity().getName().getString(), blockName);
                blockEventMap.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(eventMessage);
                blockEventTimes.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(Instant.now());
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Player player) {
            WorldBlockPos worldBlockPos = new WorldBlockPos(event.getEntity().level().toString(), event.getPos());
            String blockName = event.getLevel().getBlockState(event.getPos()).getBlock().getName().getString();

            String eventMessage = formatEventMessage(" placed ", player.getName().getString(), blockName);
            blockEventMap.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(eventMessage);
            blockEventTimes.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(Instant.now());
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!event.getLevel().isClientSide()) {
            WorldBlockPos worldBlockPos = new WorldBlockPos(event.getPlayer().level().toString(), event.getPos());
            String blockName = event.getLevel().getBlockState(event.getPos()).getBlock().getName().getString();
            String eventMessage = formatEventMessage(" broke ", player.getName().getString(), blockName);
            blockEventMap.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(eventMessage);
            blockEventTimes.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(Instant.now());
        }
    }

    @SubscribeEvent
    public void onChestPlayer(PlayerContainerEvent.Close event){

    }

    public static List<String> getBlockHistory(WorldBlockPos worldBlockPos) {
        List<String> events = blockEventMap.getOrDefault(worldBlockPos, new LinkedList<>());
        List<Instant> times = blockEventTimes.getOrDefault(worldBlockPos, new LinkedList<>());
        List<String> formattedHistory = new LinkedList<>();

        for (int i = 0; i < events.size(); i++) {
            String event = events.get(i);
            Instant eventTime = times.get(i);
            String timeAgo = formatTimeAgo(eventTime);
            formattedHistory.add(timeAgo + " - " + event);
        }

        return formattedHistory;
    }

    private String formatEventMessage(String action, String playerName, String blockName) {
        return "§a" + playerName + "§f" +action + "§3" + blockName;
    }

    private static String formatTimeAgo(Instant eventTime) {
        Duration duration = Duration.between(eventTime, Instant.now());
        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) {
            return "§7" + seconds + "/s ago§f";
        } else if (minutes < 60) {
            return "§7" + minutes + "/m ago§f";
        } else if (hours < 24) {
            return "§7" + hours + "/h ago§f";
        } else {
            return "§7" + days + "/d ago§f";
        }
    }
}