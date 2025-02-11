package dev.zontreck.otemod.networking.packets;

import dev.zontreck.otemod.commands.vaults.StarterCommand;
import dev.zontreck.otemod.commands.vaults.VaultCommand;
import dev.zontreck.otemod.implementation.vault.StarterContainer;
import dev.zontreck.otemod.implementation.vault.VaultContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// This packet is only ever sent from the client to the server when requesting to open vaults using the EaseOfUse Buttons
public class OpenStarterVaultC2SPacket {
    public OpenStarterVaultC2SPacket()
    {
    }

    public OpenStarterVaultC2SPacket(FriendlyByteBuf buf)
    {
    }
    public void toBytes(FriendlyByteBuf buf)
    {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(()->{
            // On server now
            ServerPlayer player = ctx.getSender();



            StarterCommand.doOpen(player);
        });

        return true;
    }

}
