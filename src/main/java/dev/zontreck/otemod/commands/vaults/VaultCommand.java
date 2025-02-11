package dev.zontreck.otemod.commands.vaults;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.zontreck.libzontreck.util.ChatHelpers;
import dev.zontreck.otemod.OTEMod;
import dev.zontreck.otemod.implementation.vault.NoMoreVaultException;
import dev.zontreck.otemod.implementation.vault.VaultContainer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

public class VaultCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("pv").executes(c-> vault(c.getSource(), 0)).then(Commands.argument("number", IntegerArgumentType.integer()).executes(c -> vault(c.getSource(), IntegerArgumentType.getInteger(c, "number")))));
        dispatcher.register(Commands.literal("vault").executes(c-> vault(c.getSource(), 0)).then(Commands.argument("number", IntegerArgumentType.integer()).executes(c -> vault(c.getSource(), IntegerArgumentType.getInteger(c, "number")))));
        
        //dispatcher.register(Commands.literal("sethome").then(Commands.argument("nickname", StringArgumentType.string())).executes(command -> {
            //String arg = StringArgumentType.getString(command, "nickname");
            //return setHome(command.getSource(), arg);
        //}));
    }

    private static int vault(CommandSourceStack source, int i) {
        //VaultContainer cont = new VaultContainer(i, source.getPlayer().getUUID());
        //cont.startOpen(source.getPlayer());
        ServerPlayer play = (ServerPlayer)source.getEntity();
        if(i <0)
        {
            ChatHelpers.broadcastTo(play.getUUID(), ChatHelpers.macro(OTEMod.OTEPrefix+" !Dark_Red!You can only specify a vault number in the positive range"), source.getServer());
            return 0;
        }
        doOpen(play, i);
        
        
        return 0;
    }

    public static void doOpen(ServerPlayer p, int i){

        VaultContainer container;
        try {
            container = new VaultContainer(p, i);
        } catch (NoMoreVaultException e) {
            ChatHelpers.broadcastTo(p.getUUID(), ChatHelpers.macro(OTEMod.OTEPrefix+" !Dark_Red!You cannot open anymore vaults. Craft a new vault!"), p.server);
            return;
        }
        
        NetworkHooks.openScreen(p, new SimpleMenuProvider(container.serverMenu, Component.literal("Vault " + i)));
        
        // Add to the master vault registry
        if(VaultContainer.VAULT_REGISTRY.containsKey(p.getUUID()))VaultContainer.VAULT_REGISTRY.remove(p.getUUID());
        VaultContainer.VAULT_REGISTRY.put(p.getUUID(), container);
    }
}
