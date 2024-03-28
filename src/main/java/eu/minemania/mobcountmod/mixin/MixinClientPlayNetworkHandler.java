package eu.minemania.mobcountmod.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import eu.minemania.mobcountmod.command.ClientCommandManager;
import eu.minemania.mobcountmod.counter.DataManager;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import eu.minemania.mobcountmod.command.Command;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler
{
    @Shadow
    private ClientWorld world;

    @Shadow
    private CommandDispatcher<ServerCommandSource> commandDispatcher;

    @Shadow protected abstract ParseResults<CommandSource> parse(String command);

    @Shadow protected abstract void loadChunk(int x, int z, ChunkData chunkData);

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInitMCM(MinecraftClient client, ClientConnection clientConnection, ClientConnectionState clientConnectionState, CallbackInfo ci)
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

    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    private void entityStatus(EntityStatusS2CPacket packet, CallbackInfo ci)
    {
        Entity entity = packet.getEntity((World) this.world);
        if (entity == null)
        {
            return;
        }


        if (packet.getStatus() != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
        {
            return;
        }

        DataManager.addEntityCount(entity.getType());
    }
}