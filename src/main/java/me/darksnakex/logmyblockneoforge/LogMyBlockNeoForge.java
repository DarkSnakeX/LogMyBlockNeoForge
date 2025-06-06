package me.darksnakex.logmyblockneoforge;

import com.mojang.brigadier.CommandDispatcher;
import me.darksnakex.logmyblockneoforge.Commands.BlockInspectCommand;
import me.darksnakex.logmyblockneoforge.Commands.BlockLookupCommand;
import me.darksnakex.logmyblockneoforge.Events.BlockEventHandler;
import me.darksnakex.logmyblockneoforge.Events.InventoryEventHandler;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import static me.darksnakex.logmyblockneoforge.BlockEventSaveData.loadBlockEvents;
import static me.darksnakex.logmyblockneoforge.BlockEventSaveData.saveBlockEvents;
import static net.neoforged.neoforge.common.NeoForge.EVENT_BUS;


@Mod(LogMyBlockNeoForge.MODID)
public class LogMyBlockNeoForge
{
    public static String prefix = "§e[§3Log§aMy§6Block§e] §f";
    public static final String MODID = "logmyblockneoforge";

    public LogMyBlockNeoForge(){
        EVENT_BUS.register(new BlockEventHandler());
        EVENT_BUS.register(new InventoryEventHandler());
        EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        loadBlockEvents();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        saveBlockEvents();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        new BlockInspectCommand(dispatcher);
        new BlockLookupCommand(dispatcher);
    }
}
