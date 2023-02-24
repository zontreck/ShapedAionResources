package dev.zontreck.otemod.commands.warps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.zontreck.libzontreck.chat.ChatColor;
import dev.zontreck.libzontreck.chat.Clickable;
import dev.zontreck.libzontreck.exceptions.InvalidSideException;
import dev.zontreck.otemod.OTEMod;
import dev.zontreck.otemod.chat.ChatServerOverride;
import dev.zontreck.otemod.commands.teleport.RTPCommand;
import dev.zontreck.otemod.commands.teleport.TeleportActioner;
import dev.zontreck.otemod.commands.teleport.TeleportContainer;
import dev.zontreck.otemod.database.TeleportDestination;
import dev.zontreck.otemod.implementation.warps.NoSuchWarpException;
import dev.zontreck.otemod.implementation.warps.Warp;
import dev.zontreck.otemod.implementation.warps.WarpsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class WarpCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("warp").executes(c-> nowarp(c.getSource())).then(Commands.argument("name", StringArgumentType.string()).executes(c->warp(c.getSource(), StringArgumentType.getString(c, "name")))));
        
        //dispatcher.register(Commands.literal("sethome").then(Commands.argument("nickname", StringArgumentType.string())).executes(command -> {
            //String arg = StringArgumentType.getString(command, "nickname");
            //return setHome(command.getSource(), arg);
        //}));
    }

    private static int warp(CommandSourceStack source, String string) {
        
        final ServerPlayer p;
        try{
            p=source.getPlayerOrException();
            Warp warp = WarpsProvider.WARPS_INSTANCE.getNamedWarp(string);

            TeleportDestination dest = warp.destination;

            ServerLevel dimL=(ServerLevel) dest.getActualDimension();
            

            final int type = warp.RTP ? 1 : 0;
            final ServerLevel f_dim = dimL;

            Thread tx = new Thread(new Runnable(){
                public void run(){

                    if(type==1){
                        dest.Position = RTPCommand.findPosition(source.getLevel(), false);
                    }
    
                    TeleportActioner.ApplyTeleportEffect(p);
                    TeleportContainer tc = new TeleportContainer(p, dest.Position.asMinecraftVector(), dest.Rotation.asMinecraftVector(), f_dim);
                    TeleportActioner.PerformTeleport(tc);
                }
            });
            tx.start();
        }catch(NoSuchWarpException e)
        {
            ChatServerOverride.broadcastTo(source.getEntity().getUUID(), new TextComponent(ChatColor.DARK_RED+"No such warp"), source.getServer());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int nowarp(CommandSourceStack source) {
        ServerPlayer p = (ServerPlayer)source.getEntity();
        ChatServerOverride.broadcastTo(p.getUUID(), new TextComponent(ChatColor.DARK_RED + "You must supply the warp name. If you need to know what warps are available, please use the warps command, or click this message.").withStyle(Style.EMPTY.withFont(Style.DEFAULT_FONT).withClickEvent(Clickable.command("/warps"))), source.getServer());
        return 0;
    }

}
