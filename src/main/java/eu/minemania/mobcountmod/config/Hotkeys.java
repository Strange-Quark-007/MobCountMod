package eu.minemania.mobcountmod.config;

import fi.dy.masa.malilib.config.options.ConfigHotkey;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Default hotkeys configuration.
 */
public class Hotkeys
{
    public static final ConfigHotkey PASSIVE = new ConfigHotkey("togglePassive", "P", "mcm.description.hotkey.passive");
    public static final ConfigHotkey HOSTILE = new ConfigHotkey("toggleHostile", "O", "mcm.description.hotkey.hostile");
    public static final ConfigHotkey OPEN_GUI_SETTINGS = new ConfigHotkey("openGuiSettings", "P,C", "mcm.description.hotkey.open_gui_settings");
    public static final ConfigHotkey TOGGLE_BOTH = new ConfigHotkey("toggleBoth", "", "mcm.description.hotkey.both");
    public static final ConfigHotkey EMPTY_MOBCOUNTS = new ConfigHotkey("emptyMobcounts", "", "mcm.description.hotkey.empty_mobcounts");

    public static final List<ConfigHotkey> HOTKEY_LIST = ImmutableList.of(
            EMPTY_MOBCOUNTS,
            HOSTILE,
            OPEN_GUI_SETTINGS,
            PASSIVE,
            TOGGLE_BOTH
    );
}