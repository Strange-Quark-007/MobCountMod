package eu.minemania.mobcountmod.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.minemania.mobcountmod.config.Configs;
import eu.minemania.mobcountmod.config.InfoToggleHostile;
import eu.minemania.mobcountmod.config.InfoTogglePassive;
import eu.minemania.mobcountmod.counter.DataManager;
import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.predicate.entity.EntityPredicates;

public class MobCountRenderer
{
    private static final MobCountRenderer INSTANCE = new MobCountRenderer();
    private int totalPassive = 0;
    private int totalHostile = 0;
    private long infoUpdateTime;
    private final List<StringHolder> lineWrappersHostile = new ArrayList<>();
    private final List<StringHolder> lineWrappersPassive = new ArrayList<>();
    private final List<String> linesHostile = new ArrayList<>();
    private final List<String> linesPassive = new ArrayList<>();

    public static MobCountRenderer getInstance()
    {
        return INSTANCE;
    }

    public static void renderOverlays(MatrixStack matrixStack)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        DataManager.getInstance().hostileLimit(getInstance().totalHostile);
        if (mc.currentScreen == null && MinecraftClient.isHudEnabled())
        {
            getInstance().renderHUD(matrixStack);
        }
    }

    public void renderHUD(MatrixStack matrixStack)
    {
        long currentTime = System.currentTimeMillis();
        HudAlignment hudAlignment = (HudAlignment) Configs.Generic.HUD_ALIGNMENT.getOptionListValue();
        int colorBg = Configs.Generic.COLOR_BACK_DEFAULT.getColor().intValue;
        int colorFg = Configs.Generic.COLOR_FORE_DEFAULT.getColor().intValue;
        boolean customBg = Configs.Generic.CUSTOM_BG_COLOR.getBooleanValue();
        int yOff = 15;

        if (currentTime - this.infoUpdateTime >= 50)
        {
            this.updateLines();
            this.infoUpdateTime = currentTime;
        }

        if (hudAlignment == HudAlignment.BOTTOM_LEFT || hudAlignment == HudAlignment.BOTTOM_RIGHT)
        {
            yOff = 75;
        }

        if (DataManager.visibleCounter() == 1)
        {
            DataManager.getCounter().updateBBP();
            int xOff = 5;

            if (hudAlignment == HudAlignment.CENTER)
            {
                xOff = -125;
            }
            RenderUtils.renderText(xOff, yOff, 1, colorFg, colorBg, hudAlignment, customBg, true, this.linesPassive, matrixStack);
        }

        if (DataManager.visibleHostile() == 1)
        {
            DataManager.getCounter().updateBBH();
            RenderUtils.renderText(125, yOff, 1, colorFg, colorBg, hudAlignment, customBg, true, this.linesHostile, matrixStack);
        }
    }

    private void updateLines()
    {
        this.lineWrappersHostile.clear();
        this.lineWrappersPassive.clear();
        this.totalHostile = 0;
        this.totalPassive = 0;

        List<LinePosPassive> positionsPassive = new ArrayList<>();
        List<LinePosHostile> positionsHostile = new ArrayList<>();


        for (InfoTogglePassive toggle : InfoTogglePassive.values())
        {
            if (toggle.getBooleanValue())
            {
                positionsPassive.add(new LinePosPassive(toggle.getIntegerValue(), toggle));
            }
        }

        for (InfoToggleHostile toggle : InfoToggleHostile.values())
        {
            if (toggle.getBooleanValue())
            {
                positionsHostile.add(new LinePosHostile(toggle.getIntegerValue(), toggle));
            }
        }

        Collections.sort(positionsPassive);
        Collections.sort(positionsHostile);

        for (LinePosPassive pos : positionsPassive)
        {
            try
            {
                this.addLinePassive(pos.type);
            }
            catch (Exception e)
            {
                this.addLinePassive(pos.type.getName() + ": exception");
            }
        }

        for (LinePosHostile pos : positionsHostile)
        {
            try
            {
                this.addLineHostile(pos.type);
            }
            catch (Exception e)
            {
                this.addLineHostile(pos.type.getName() + ": exception");
            }
        }

        this.linesHostile.clear();
        this.linesPassive.clear();

        for (StringHolder holder : this.lineWrappersHostile)
        {
            this.linesHostile.add(holder.str);
        }

        for (StringHolder holder : this.lineWrappersPassive)
        {
            this.linesPassive.add(holder.str);
        }
    }

    private String getColor(int amount, boolean passive)
    {
        String color = GuiBase.TXT_WHITE;
        if (passive && amount > 149)
        {
            color = GuiBase.TXT_DARK_RED;
        }
        else if (!passive)
        {
            if (amount > 149)
            { // if 150+ mobs, display in red.
                color = GuiBase.TXT_DARK_RED;
            }
            else
            {
                DataManager.setPlaySoundCount(100);
            }
        }
        return color;
    }

    private void addLineHostile(String text)
    {
        if (!text.isEmpty())
        {
            this.lineWrappersHostile.add(new StringHolder(text));
        }
    }

    private void addLinePassive(String text)
    {
        if (!text.isEmpty())
        {
            this.lineWrappersPassive.add(new StringHolder(text));
        }
    }

    private <T extends Entity> String lineTextP(EntityType<T> entity)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        int size = mc.world.getEntitiesByType(entity, DataManager.getCounter().getPassiveBB(), EntityPredicates.EXCEPT_SPECTATOR).size() - (!StringUtils.translate(entity.getTranslationKey()).equals("Player") ? 0 : (mc.player.isSpectator() ? 0 : 1));
        totalPassive += size;
        return size == 0 && !Configs.Generic.DISPLAY_ALL.getBooleanValue() ? "" : String.format("%s: %s%d%s", StringUtils.translate(entity.getTranslationKey()), size > Configs.Generic.COUNT_PASSIVE.getIntegerValue() ? GuiBase.TXT_RED : GuiBase.TXT_GREEN, size, GuiBase.TXT_RST);
    }

    private <T extends Entity> String lineText(Class<? extends T> entity)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        int size = mc.world.getEntitiesByClass(entity, DataManager.getCounter().getPassiveBB(), EntityPredicates.EXCEPT_SPECTATOR).size();
        totalPassive += size;
        return size == 0 && !Configs.Generic.DISPLAY_ALL.getBooleanValue() ? "" : String.format("%s: %s%d%s", StringUtils.translate(EntityType.TROPICAL_FISH.getTranslationKey()), size > Configs.Generic.COUNT_PASSIVE.getIntegerValue() ? GuiBase.TXT_RED : GuiBase.TXT_GREEN, size, GuiBase.TXT_RST);
    }

    private <T extends Entity> String lineTextH(EntityType<T> entity)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        int size = mc.world.getEntitiesByType(entity, DataManager.getCounter().getHostileBB(), EntityPredicates.EXCEPT_SPECTATOR).size();
        totalHostile += size;
        return size == 0 && !Configs.Generic.DISPLAY_ALL.getBooleanValue() ? "" : String.format("%s: %s%d%s", StringUtils.translate(entity.getTranslationKey()), size > Configs.Generic.COUNT_HOSTILE.getIntegerValue() ? GuiBase.TXT_RED : GuiBase.TXT_GREEN, size, GuiBase.TXT_RST);
    }

    private void addLinePassive(InfoTogglePassive type)
    {
        if (type == InfoTogglePassive.RADIUS_COUNTER)
        {
            this.addLinePassive(String.format("%s %s%s%s", StringUtils.translate("mcm.message.mobcounter.radius", DataManager.getCounter().getRadiusP()), getColor(totalPassive, true), StringUtils.translate("mcm.message.mobcounter.total", this.totalPassive), GuiBase.TXT_RST));
        }
        else if (type == InfoTogglePassive.ALLAY)
        {
            this.addLinePassive(lineTextP(EntityType.ALLAY));
        }
        else if (type == InfoTogglePassive.AXOLOTL)
        {
            this.addLinePassive(lineTextP(EntityType.AXOLOTL));
        }
        else if (type == InfoTogglePassive.BAT)
        {
            this.addLinePassive(lineTextP(EntityType.BAT));
        }
        else if (type == InfoTogglePassive.BEE)
        {
            this.addLinePassive(lineTextP(EntityType.BEE));
        }
        else if (type == InfoTogglePassive.CAMEL)
        {
            this.addLinePassive(lineTextP(EntityType.CAMEL));
        }
        else if (type == InfoTogglePassive.CAT)
        {
            this.addLinePassive(lineTextP(EntityType.CAT));
        }
        else if (type == InfoTogglePassive.CHICKEN)
        {
            this.addLinePassive(lineTextP(EntityType.CHICKEN));
        }
        else if (type == InfoTogglePassive.COW)
        {
            this.addLinePassive(lineTextP(EntityType.COW));
        }
        else if (type == InfoTogglePassive.DOLPHIN)
        {
            this.addLinePassive(lineTextP(EntityType.DOLPHIN));
        }
        else if (type == InfoTogglePassive.DONKEY)
        {
            this.addLinePassive(lineTextP(EntityType.DONKEY));
        }
        else if (type == InfoTogglePassive.FISH)
        {
            this.addLinePassive(lineText(FishEntity.class));
        }
        else if (type == InfoTogglePassive.FOX)
        {
            this.addLinePassive(lineTextP(EntityType.FOX));
        }
        else if (type == InfoTogglePassive.FROG)
        {
            this.addLinePassive(lineTextP(EntityType.FROG));
        }
        else if (type == InfoTogglePassive.GLOWSQUID)
        {
            this.addLinePassive(lineTextP(EntityType.GLOW_SQUID));
        }
        else if (type == InfoTogglePassive.GOAT)
        {
            this.addLinePassive(lineTextP(EntityType.GOAT));
        }
        else if (type == InfoTogglePassive.HORSE)
        {
            this.addLinePassive(lineTextP(EntityType.HORSE));
        }
        else if (type == InfoTogglePassive.IRONGOLEM)
        {
            this.addLinePassive(lineTextP(EntityType.IRON_GOLEM));
        }
        else if (type == InfoTogglePassive.LLAMA)
        {
            this.addLinePassive(lineTextP(EntityType.LLAMA));
        }
        else if (type == InfoTogglePassive.MOOSHROOM)
        {
            this.addLinePassive(lineTextP(EntityType.MOOSHROOM));
        }
        else if (type == InfoTogglePassive.MULE)
        {
            this.addLinePassive(lineTextP(EntityType.MULE));
        }
        else if (type == InfoTogglePassive.OCELOT)
        {
            this.addLinePassive(lineTextP(EntityType.OCELOT));
        }
        else if (type == InfoTogglePassive.PANDA)
        {
            this.addLinePassive(lineTextP(EntityType.PANDA));
        }
        else if (type == InfoTogglePassive.PARROT)
        {
            this.addLinePassive(lineTextP(EntityType.PARROT));
        }
        else if (type == InfoTogglePassive.PIG)
        {
            this.addLinePassive(lineTextP(EntityType.PIG));
        }
        else if (type == InfoTogglePassive.PLAYER)
        {
            this.addLinePassive(lineTextP(EntityType.PLAYER));
        }
        else if (type == InfoTogglePassive.POLARBEAR)
        {
            this.addLinePassive(lineTextP(EntityType.POLAR_BEAR));
        }
        else if (type == InfoTogglePassive.RABBIT)
        {
            this.addLinePassive(lineTextP(EntityType.RABBIT));
        }
        else if (type == InfoTogglePassive.SHEEP)
        {
            this.addLinePassive(lineTextP(EntityType.SHEEP));
        }
        else if (type == InfoTogglePassive.SKELETON_HORSE)
        {
            this.addLinePassive(lineTextP(EntityType.SKELETON_HORSE));
        }
        else if (type == InfoTogglePassive.SNIFFER)
        {
            this.addLinePassive(lineTextP(EntityType.SNIFFER));
        }
        else if (type == InfoTogglePassive.SNOW_GOLEM)
        {
            this.addLinePassive(lineTextP(EntityType.SNOW_GOLEM));
        }
        else if (type == InfoTogglePassive.SQUID)
        {
            this.addLinePassive(lineTextP(EntityType.SQUID));
        }
        else if (type == InfoTogglePassive.STRIDER)
        {
            this.addLinePassive(lineTextP(EntityType.STRIDER));
        }
        else if (type == InfoTogglePassive.TADPOLE)
        {
            this.addLinePassive(lineTextP(EntityType.TADPOLE));
        }
        else if (type == InfoTogglePassive.TRADER_LLAMA)
        {
            this.addLinePassive(lineTextP(EntityType.TRADER_LLAMA));
        }
        else if (type == InfoTogglePassive.TURTLE)
        {
            this.addLinePassive(lineTextP(EntityType.TURTLE));
        }
        else if (type == InfoTogglePassive.VILLAGER)
        {
            this.addLinePassive(lineTextP(EntityType.VILLAGER));
        }
        else if (type == InfoTogglePassive.WANDERING_TRADER)
        {
            this.addLinePassive(lineTextP(EntityType.WANDERING_TRADER));
        }
        else if (type == InfoTogglePassive.WOLF)
        {
            this.addLinePassive(lineTextP(EntityType.WOLF));
        }
        else if (type == InfoTogglePassive.ZOMBIE_HORSE)
        {
            this.addLinePassive(lineTextP(EntityType.ZOMBIE_HORSE));
        }
    }

    private void addLineHostile(InfoToggleHostile type)
    {
        if (type == InfoToggleHostile.RADIUS_COUNTER)
        {
            this.addLineHostile(String.format("%s %s%s%s", StringUtils.translate("mcm.message.mobcounter.radius", DataManager.getCounter().getRadiusH()), getColor(totalHostile, false), StringUtils.translate("mcm.message.mobcounter.total", this.totalHostile), GuiBase.TXT_RST));
        }
        else if (type == InfoToggleHostile.BLAZE)
        {
            this.addLineHostile(lineTextH(EntityType.BLAZE));
        }
        else if (type == InfoToggleHostile.CAVE_SPIDER)
        {
            this.addLineHostile(lineTextH(EntityType.CAVE_SPIDER));
        }
        else if (type == InfoToggleHostile.CREEPER)
        {
            this.addLineHostile(lineTextH(EntityType.CREEPER));
        }
        else if (type == InfoToggleHostile.DROWNED)
        {
            this.addLineHostile(lineTextH(EntityType.DROWNED));
        }
        else if (type == InfoToggleHostile.ELDER_GUARDIAN)
        {
            this.addLineHostile(lineTextH(EntityType.ELDER_GUARDIAN));
        }
        else if (type == InfoToggleHostile.ENDERMAN)
        {
            this.addLineHostile(lineTextH(EntityType.ENDERMAN));
        }
        else if (type == InfoToggleHostile.ENDERMITE)
        {
            this.addLineHostile(lineTextH(EntityType.ENDERMITE));
        }
        else if (type == InfoToggleHostile.EVOKER)
        {
            this.addLineHostile(lineTextH(EntityType.EVOKER));
        }
        else if (type == InfoToggleHostile.GHAST)
        {
            this.addLineHostile(lineTextH(EntityType.GHAST));
        }
        else if (type == InfoToggleHostile.GUARDIAN)
        {
            this.addLineHostile(lineTextH(EntityType.GUARDIAN));
        }
        else if (type == InfoToggleHostile.HOGLIN)
        {
            this.addLineHostile(lineTextH(EntityType.HOGLIN));
        }
        else if (type == InfoToggleHostile.HUSK)
        {
            this.addLineHostile(lineTextH(EntityType.HUSK));
        }
        else if (type == InfoToggleHostile.ILLUSIONER)
        {
            this.addLineHostile(lineTextH(EntityType.ILLUSIONER));
        }
        else if (type == InfoToggleHostile.MAGMA_CUBE)
        {
            this.addLineHostile(lineTextH(EntityType.MAGMA_CUBE));
        }
        else if (type == InfoToggleHostile.PHANTOM)
        {
            this.addLineHostile(lineTextH(EntityType.PHANTOM));
        }
        else if (type == InfoToggleHostile.PIGLIN)
        {
            this.addLineHostile(lineTextH(EntityType.PIGLIN));
        }
        else if (type == InfoToggleHostile.PILLAGER)
        {
            this.addLineHostile(lineTextH(EntityType.PILLAGER));
        }
        else if (type == InfoToggleHostile.PIGLIN_BRUTE)
        {
            this.addLineHostile(lineTextH(EntityType.PIGLIN_BRUTE));
        }
        else if (type == InfoToggleHostile.RAVAGER)
        {
            this.addLineHostile(lineTextH(EntityType.RAVAGER));
        }
        else if (type == InfoToggleHostile.SHULKER)
        {
            this.addLineHostile(lineTextH(EntityType.SHULKER));
        }
        else if (type == InfoToggleHostile.SILVERFISH)
        {
            this.addLineHostile(lineTextH(EntityType.SILVERFISH));
        }
        else if (type == InfoToggleHostile.SKELETON)
        {
            this.addLineHostile(lineTextH(EntityType.SKELETON));
        }
        else if (type == InfoToggleHostile.SLIME)
        {
            this.addLineHostile(lineTextH(EntityType.SLIME));
        }
        else if (type == InfoToggleHostile.SPIDER)
        {
            this.addLineHostile(lineTextH(EntityType.SPIDER));
        }
        else if (type == InfoToggleHostile.STRAY)
        {
            this.addLineHostile(lineTextH(EntityType.STRAY));
        }
        else if (type == InfoToggleHostile.VEX)
        {
            this.addLineHostile(lineTextH(EntityType.VEX));
        }
        else if (type == InfoToggleHostile.VINDICATOR)
        {
            this.addLineHostile(lineTextH(EntityType.VINDICATOR));
        }
        else if (type == InfoToggleHostile.WARDEN)
        {
            this.addLineHostile(lineTextH(EntityType.WARDEN));
        }
        else if (type == InfoToggleHostile.WITCH)
        {
            this.addLineHostile(lineTextH(EntityType.WITCH));
        }
        else if (type == InfoToggleHostile.WITHER)
        {
            this.addLineHostile(lineTextH(EntityType.WITHER));
        }
        else if (type == InfoToggleHostile.WITHER_SKELETON)
        {
            this.addLineHostile(lineTextH(EntityType.WITHER_SKELETON));
        }
        else if (type == InfoToggleHostile.ZOGLIN)
        {
            this.addLineHostile(lineTextH(EntityType.ZOGLIN));
        }
        else if (type == InfoToggleHostile.ZOMBIE)
        {
            this.addLineHostile(lineTextH(EntityType.ZOMBIE));
        }
        else if (type == InfoToggleHostile.ZOMBIE_VILLAGER)
        {
            this.addLineHostile(lineTextH(EntityType.ZOMBIE_VILLAGER));
        }
        else if (type == InfoToggleHostile.ZOMBIFIED_PIGLIN)
        {
            this.addLineHostile(lineTextH(EntityType.ZOMBIFIED_PIGLIN));
        }
    }

    private class StringHolder
    {
        public final String str;

        public StringHolder(String str)
        {
            this.str = str;
        }
    }

    private static class LinePosPassive implements Comparable<LinePosPassive>
    {
        private final int position;
        private final InfoTogglePassive type;

        private LinePosPassive(int position, InfoTogglePassive type)
        {
            this.position = position;
            this.type = type;
        }

        @Override
        public int compareTo(LinePosPassive other)
        {
            if (this.position < 0)
            {
                return other.position >= 0 ? 1 : 0;
            }
            else if (other.position < 0 && this.position >= 0)
            {
                return -1;
            }

            return this.position < other.position ? -1 : (this.position > other.position ? 1 : 0);
        }
    }

    private static class LinePosHostile implements Comparable<LinePosHostile>
    {
        private final int position;
        private final InfoToggleHostile type;

        private LinePosHostile(int position, InfoToggleHostile type)
        {
            this.position = position;
            this.type = type;
        }

        @Override
        public int compareTo(LinePosHostile other)
        {
            if (this.position < 0)
            {
                return other.position >= 0 ? 1 : 0;
            }
            else if (other.position < 0 && this.position >= 0)
            {
                return -1;
            }

            return this.position < other.position ? -1 : (this.position > other.position ? 1 : 0);
        }
    }
}