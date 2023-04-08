package eu.minemania.mobcountmod.mixin;

import com.mojang.brigadier.StringReader;
import eu.minemania.mobcountmod.command.ClientCommandManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.telemetry.WorldSession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;

import eu.minemania.mobcountmod.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler
{
    @Shadow
    private CommandDispatcher<ServerCommandSource> commandDispatcher;

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInitMCM(MinecraftClient client, Screen screen, ClientConnection connection, ServerInfo serverInfo, GameProfile profile, WorldSession worldSession, CallbackInfo ci)
    {
        Command.registerCommands((CommandDispatcher<ServerCommandSource>) (Object) commandDispatcher);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "onCommandTree", at = @At("TAIL"))
    public void onOnCommandTreeMCM(CommandTreeS2CPacket packet, CallbackInfo ci)
    {
        Command.registerCommands((CommandDispatcher<ServerCommandSource>) (Object) commandDispatcher);
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void sendCommand(String message, CallbackInfo ci)
    {
        StringReader reader = new StringReader(message);
        int cursor = reader.getCursor();
        String commandName = reader.canRead() ? reader.readUnquotedString() : "";
        reader.setCursor(cursor);
        if (ClientCommandManager.isClientSideCommand(commandName))
        {
            ClientCommandManager.executeCommand(reader, message);
            ci.cancel();
        }
    }
}