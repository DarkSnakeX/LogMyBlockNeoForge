package me.darksnakex.logmyblockneoforge.Events;

import me.darksnakex.logmyblockneoforge.Utils.WorldBlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;


import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import static me.darksnakex.logmyblockneoforge.Events.BlockEventHandler.blockEventMap;
import static me.darksnakex.logmyblockneoforge.Events.BlockEventHandler.blockEventTimes;


public class InventoryEventHandler {

    private static final Map<UUID, ItemStack[]> chestContents = new HashMap<>();
    private static final Map<UUID, BlockPos> playerChestPos = new HashMap<>();

    @SubscribeEvent
    public void onChestOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        AbstractContainerMenu container = event.getContainer();

        if (container instanceof ChestMenu) {
            int cont = ((ChestMenu) container).getContainer().getContainerSize();
            ItemStack[] items = new ItemStack[cont];
            for (int i = 0; i < cont; i++) {
                items[i] = container.getItems().get(i).copy();
            }
            chestContents.put(player.getUUID(), items);
        }
    }

    @SubscribeEvent
    public void onChestClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        AbstractContainerMenu container = event.getContainer();

        if (container instanceof ChestMenu) {
            UUID playerUUID = player.getUUID();
            ItemStack[] oldItems = chestContents.get(playerUUID);
            BlockPos blockPos = playerChestPos.get(playerUUID);
            if (oldItems != null && blockPos != null) {
                int cont = ((ChestMenu) container).getContainer().getContainerSize();
                for (int i = 0; i < cont; i++) {
                    ItemStack newItem = container.getItems().get(i);
                    ItemStack oldItem = oldItems[i];

                    if (!ItemStack.matches(newItem, oldItem)) {
                        BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
                        if (blockEntity instanceof ChestBlockEntity) {
                            WorldBlockPos worldBlockPos = new WorldBlockPos(player.level().toString(), blockPos);
                            String eventMessage = getString(newItem, player, oldItem);
                            blockEventMap.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(eventMessage);
                            blockEventTimes.computeIfAbsent(worldBlockPos, k -> new LinkedList<>()).add(Instant.now());
                        }
                    }
                }
                chestContents.remove(playerUUID);
                playerChestPos.remove(playerUUID);
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        if (player.level().getBlockState(pos).getBlock() == Blocks.CHEST) {
            playerChestPos.put(player.getUUID(), pos);
        }
    }

    private static String getString(ItemStack newItem, Player player, ItemStack oldItem) {
        String eventMessage;
        if (newItem.isEmpty()) {
            eventMessage = "§a" + player.getName().getString() + "§f took §3" + oldItem.getCount() + "x " + oldItem.getHoverName().getString();
        } else if (oldItem.isEmpty()) {
            eventMessage = "§a" + player.getName().getString() + "§f placed §3" + newItem.getCount() + "x " + newItem.getHoverName().getString();
        } else {
            eventMessage = "§a" + player.getName().getString() + "§f swapped §3" + oldItem.getCount() + "x " + oldItem.getHoverName().getString() + "§f with §3" + newItem.getCount() + "x " + newItem.getHoverName().getString();
        }
        return eventMessage;
    }
}