package dev.zontreck.otemod.chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import dev.zontreck.libzontreck.chat.ChatColor;
import dev.zontreck.libzontreck.chat.HoverTip;
import dev.zontreck.libzontreck.util.ItemUtils;
import dev.zontreck.otemod.OTEMod;
import dev.zontreck.otemod.configs.OTEServerConfig;
import dev.zontreck.otemod.configs.PlayerFlyCache;
import dev.zontreck.otemod.enchantments.ModEnchantments;
import dev.zontreck.otemod.implementation.profiles.Profile;
import dev.zontreck.otemod.implementation.profiles.UserProfileNotYetExistsException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid=OTEMod.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ChatServerOverride {

    @SubscribeEvent
    public void onJoin(final PlayerEvent.PlayerLoggedInEvent ev)
    {
        //Player joined, send custom alert
        if(ev.getEntity().level.isClientSide)return;
        ServerPlayer play = (ServerPlayer)ev.getEntity();
        Profile prof = Profile.factory(play);
        

        if(prof.flying)
        {
            play.getAbilities().flying=true;
            play.onUpdateAbilities();
        }

        Abilities playerAbilities = play.getAbilities();
        boolean mayFly = false;
        ItemStack feet = play.getItemBySlot(EquipmentSlot.FEET);
        if(ItemUtils.getEnchantmentLevel(ModEnchantments.FLIGHT_ENCHANTMENT.get(), feet)>0)mayFly = true;

        playerAbilities.mayfly=mayFly;
        PlayerFlyCache c = PlayerFlyCache.cachePlayer(play);
        c.Flying=prof.flying;
        c.FlyEnabled = mayFly;
        c.Assert(play);

        if(!OTEServerConfig.USE_CUSTOM_JOINLEAVE.get()) return;
        
        ChatServerOverride.broadcast(new TextComponent(ChatColor.doColors("!Dark_Gray![!Dark_Green!+!Dark_Gray!] !Bold!!Dark_Aqua!"+prof.nickname)), ev.getEntity().getServer());
        
    }

    @SubscribeEvent
    public void onLeave(final PlayerEvent.PlayerLoggedOutEvent ev)
    {
        if(ev.getEntity().level.isClientSide)return;
        // Get player profile, send disconnect alert, then commit profile and remove it from memory
        Profile px=null;
        try {
            px = Profile.get_profile_of(ev.getEntity().getStringUUID());
        } catch (UserProfileNotYetExistsException e) {
            e.printStackTrace();
        }
        Profile.unload(px);
        ServerPlayer sp = (ServerPlayer)ev.getEntity();


        if(px==null)return;

        if(!OTEServerConfig.USE_CUSTOM_JOINLEAVE.get()) return;

        // Send the alert
        ChatServerOverride.broadcast(new TextComponent(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "-" + ChatColor.DARK_GRAY + "] "+ChatColor.BOLD + ChatColor.DARK_AQUA + px.nickname), ev.getEntity().getServer());

        px.flying=sp.getAbilities().flying;
        px.commit();
    }

    @SubscribeEvent
    public void onClone(final PlayerEvent.Clone ev)
    {
        if(ev.getEntity().level.isClientSide)return;
        // Fix for fly ability not copying to new instance on death or other circumstances
        Player old = ev.getOriginal();
        Player n = ev.getPlayer();

        PlayerFlyCache c = PlayerFlyCache.cachePlayer((ServerPlayer)old);
        c.Assert((ServerPlayer)n);
    }

    @SubscribeEvent
    public void onChat(final ServerChatEvent ev){
        if(ev.getPlayer().level.isClientSide)return;
        // Player has chatted, apply override
        if(!OTEServerConfig.USE_CUSTOM_CHATREPLACER.get()) return;

        
        ServerPlayer sp = ev.getPlayer();
        // Get profile
        Profile XD=null;
        try {
            XD = Profile.get_profile_of(sp.getStringUUID());
        } catch (UserProfileNotYetExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Override the chat!
        String prefixStr = "";
        if(XD.prefix != ""){
            prefixStr = ChatColor.DARK_GRAY + "[" + ChatColor.BOLD + XD.prefix_color + XD.prefix + ChatColor.resetChat() + ChatColor.DARK_GRAY + "] ";
        }

        String msg = ev.getMessage();
        msg= ChatColor.doColors(msg);

        String nameStr = ChatColor.resetChat() + "< "+ XD.name_color + XD.nickname + ChatColor.resetChat() + " >";
        String message = ": "+XD.chat_color + msg;
        Style hover = Style.EMPTY;
        hover=hover.withFont(Style.DEFAULT_FONT).withHoverEvent(HoverTip.get(ChatColor.MINECOIN_GOLD+"User Name: "+XD.username));
        ev.setCanceled(true);

        ChatServerOverride.broadcast(new TextComponent(prefixStr+nameStr+message).setStyle(hover), ev.getPlayer().server);
    }


    public static void broadcastAbove(Component message, MinecraftServer s)
    {
        s.execute(new Runnable() {
            public void run(){

                // This will broadcast to all players
                for(ServerPlayer play : s.getPlayerList().getPlayers())
                {
                    play.displayClientMessage(message, true); // Send the message!
                }
                LogToConsole(new TextComponent("[all] ").append(message));
            }
        });
    }
    public static void LogToConsole(Component Message)
    {
        OTEMod.LOGGER.info(Message.getString());
    }
    public static void broadcast(Component message, MinecraftServer s)
    {
        s.execute(new Runnable() {
            public void run(){

                // This will broadcast to all players
                for(ServerPlayer play : s.getPlayerList().getPlayers())
                {
                    play.displayClientMessage(message, false); // Send the message!
                }
                LogToConsole(new TextComponent("[all] ").append(message));
            }
        });
    }
    public static void broadcastTo(UUID ID, Component message, MinecraftServer s)
    {
        s.execute(new Runnable() {
            public void run(){

                ServerPlayer play = s.getPlayerList().getPlayer(ID);
                play.displayClientMessage(message, false); // Send the message!
                
                LogToConsole(new TextComponent("[server] -> ["+play.getName().toString()+"] ").append(message));
            }
        });
    }
    public static void broadcastToAbove(UUID ID, Component message, MinecraftServer s)
    {
        s.execute(new Runnable() {
            public void run(){

                ServerPlayer play = s.getPlayerList().getPlayer(ID);
                play.displayClientMessage(message, true); // Send the message!
                
                LogToConsole(new TextComponent("[server] -> ["+play.getName().toString()+"] ").append(message));
            }
        });
    }
}
