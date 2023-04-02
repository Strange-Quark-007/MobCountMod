package eu.minemania.mobcountmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;

import eu.minemania.mobcountmod.command.ClientCommandManager;
import eu.minemania.mobcountmod.interfaces.ITextFieldWidget;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinCommandSuggestor
{
    @Accessor("textField")
    abstract TextFieldWidget mcm_getTextField();
    @Shadow
    private ParseResults<CommandSource> parse;

    @Unique
    private int oldMaxLength;
    @Unique
    private boolean wasClientCommand = false;

    @Inject(method = "refresh", at = @At("RETURN"))
    public void onRefreshMCM(CallbackInfo ci)
    {
        boolean isClientCommand;
        if (parse == null)
        {
            isClientCommand = false;
        }
        else
        {
            StringReader reader = new StringReader(parse.getReader().getString());
            reader.skip(); // /
            String command = reader.canRead() ? reader.readUnquotedString() : "";
            isClientCommand = ClientCommandManager.isClientSideCommand(command);
        }

        if (isClientCommand && !wasClientCommand)
        {
            wasClientCommand = true;
            oldMaxLength = ((ITextFieldWidget) mcm_getTextField()).clientcommands_getMaxLengthMCM();
            mcm_getTextField().setMaxLength(Math.max(oldMaxLength, 32500));
        }
        else if (!isClientCommand && wasClientCommand)
        {
            wasClientCommand = false;
            mcm_getTextField().setMaxLength(oldMaxLength);
        }
    }
}