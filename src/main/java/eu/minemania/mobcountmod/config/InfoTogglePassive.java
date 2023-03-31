package eu.minemania.mobcountmod.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import eu.minemania.mobcountmod.MobCountMod;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBoolean;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.StringUtils;

public enum InfoTogglePassive implements IConfigInteger, IHotkeyTogglable
{
    RADIUS_COUNTER("infoRadiusCounter", true, 99, "", "radius counter"),
    ALLAY("infoAllay", true, 38, "", "allays"),
    AXOLOTL("infoAxolotl", true, 33, "", "axolotl"),
    BAT("infoBat", true, 10, "", "bats"),
    BEE("infoBee", true, 23, "", "bees"),
    CAT("infoCat", true, 11, "", "cats"),
    CHICKEN("infoChicken", true, 1, "", "chickens"),
    COW("infoCow", true, 4, "", "cows"),
    DOLPHIN("infoDolphin", true, 17, "", "dolphins"),
    DONKEY("infoDonkey", true, 24, "", "donkeys"),
    FISH("infoFish", true, 15, "", "fishes"),
    FOX("infoFox", true, 18, "", "foxes"),
    FROG("infoFrog", true, 36, "", "frogs"),
    GLOWSQUID("infoGlowSquid", true, 34, "", "glow squids"),
    GOAT("infoGoat", true, 35, "", "goats"),
    HORSE("infoHorse", true, 5, "", "horses"),
    IRONGOLEM("infoIronGolem", true, 12, "", "iron golems"),
    LLAMA("infoLlama", true, 25, "", "llamas"),
    MOOSHROOM("infoMooshroom", true, 26, "", "mooshrooms"),
    MULE("infoMule", true, 27, "", "mules"),
    OCELOT("infoOcelot", true, 8, "", "ocelots"),
    PANDA("infoPanda", true, 19, "", "pandas"),
    PARROT("infoParrot", true, 9, "", "parrots"),
    PIG("infoPig", true, 2, "", "pigs"),
    PLAYER("infoPlayer", true, 14, "", "players"),
    POLARBEAR("infoPolarBear", true, 20, "", "polarbears"),
    RABBIT("infoRabbit", true, 6, "", "rabbits"),
    SHEEP("infoSheep", true, 3, "", "sheep"),
    SKELETON_HORSE("infoSkeletonHorse", true, 28, "", "skeleton horses"),
    SNOW_GOLEM("infoSnowGolem", true, 13, "", "snow golems"),
    SQUID("infoSquid", true, 21, "", "squids"),
    STRIDER("infoStrider", true, 32, "", "Striders"),
    TADPOLE("infoTadpole", true, 37, "", "tadpoles"),
    TRADER_LLAMA("infoTraderLlama", true, 29, "", "trader llamas"),
    TURTLE("infoTurtle", true, 22, "", "turtles"),
    VILLAGER("infoVillager", true, 16, "", "villagers"),
    WANDERING_TRADER("infoWanderingTrader", true, 30, "", "wandering traders"),
    WOLF("infoWolf", true, 7, "", "wolves"),
    ZOMBIE_HORSE("infoZombieHorse", true, 31, "", "zombie horses");

    private final String name;
    private final IKeybind keybind;
    private final boolean defaultValueBoolean;
    private final int defaultLinePosition;
    private boolean valueBoolean;
    private int linePosition;
    private Object[] commentArgs;

    InfoTogglePassive(String name, boolean defaultValue, int linePosition, String defaultHotkey, Object... commentArgs)
    {
        this(name, defaultValue, linePosition, defaultHotkey, KeybindSettings.DEFAULT, commentArgs);
    }

    InfoTogglePassive(String name, boolean defaultValue, int linePosition, String defaultHotkey, KeybindSettings settings, Object... commentArgs)
    {
        this.name = name;
        this.valueBoolean = defaultValue;
        this.defaultValueBoolean = defaultValue;
        this.keybind = KeybindMulti.fromStorageString(defaultHotkey, settings);
        this.keybind.setCallback(new KeyCallbackToggleBoolean(this));
        this.linePosition = linePosition;
        this.defaultLinePosition = linePosition;
        this.commentArgs = commentArgs;
    }

    @Override
    public ConfigType getType()
    {
        return ConfigType.HOTKEY;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getComment()
    {
        return StringUtils.translate("mcm.description.config.infotoggle", getCommentArgs());
    }

    public Object[] getCommentArgs()
    {
        if (this.commentArgs != null)
        {
            return this.commentArgs;
        }
        return new Object[0];
    }

    @Override
    public void setValueFromJsonElement(JsonElement element)
    {
        try
        {
            if (element.isJsonPrimitive())
            {
                this.valueBoolean = element.getAsBoolean();
            }
            else
            {
                MobCountMod.logger.warn("Failed to read config value for '{}' from the JSON config", this.getName());
            }
        }
        catch (Exception e)
        {
            MobCountMod.logger.warn("Failed to read config value for '{}' from the JSON config", this.getName(), e);
        }
    }

    @Override
    public JsonElement getAsJsonElement()
    {
        return new JsonPrimitive(this.valueBoolean);
    }

    @Override
    public boolean isModified()
    {
        return this.valueBoolean != this.defaultValueBoolean;
    }

    @Override
    public void resetToDefault()
    {
        this.valueBoolean = this.defaultValueBoolean;
    }

    @Override
    public String getDefaultStringValue()
    {
        return String.valueOf(this.defaultValueBoolean);
    }

    @Override
    public void setValueFromString(String value)
    {
        try
        {
            this.valueBoolean = Boolean.parseBoolean(value);
        }
        catch (Exception e)
        {
            MobCountMod.logger.warn("Failed to red config value for '{}' from the JSON config", this.getName(), e);
        }
    }

    @Override
    public boolean isModified(String newValue)
    {
        return String.valueOf(this.defaultValueBoolean).equals(newValue) == false;
    }

    @Override
    public String getStringValue()
    {
        return String.valueOf(this.valueBoolean);
    }

    @Override
    public boolean getBooleanValue()
    {
        return this.valueBoolean;
    }

    @Override
    public boolean getDefaultBooleanValue()
    {
        return this.defaultValueBoolean;
    }

    @Override
    public void setBooleanValue(boolean value)
    {
        this.valueBoolean = value;
    }

    @Override
    public IKeybind getKeybind()
    {
        return this.keybind;
    }

    @Override
    public int getIntegerValue()
    {
        return this.linePosition;
    }

    @Override
    public int getDefaultIntegerValue()
    {
        return this.defaultLinePosition;
    }

    @Override
    public void setIntegerValue(int value)
    {
        this.linePosition = value;
    }

    @Override
    public int getMinIntegerValue()
    {
        return 0;
    }

    @Override
    public int getMaxIntegerValue()
    {
        return InfoTogglePassive.values().length - 1;
    }
}
