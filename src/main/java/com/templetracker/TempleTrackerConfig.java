package com.templetracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("templetracker")
public interface TempleTrackerConfig extends Config
{
	@ConfigSection(
		name = "Settings",
		description = "Toggles certain functionalities on or off",
		position = 0
	)
	String toggleSection = "Settings";

	@ConfigSection(
		name = "Menu Entry Swaps",
		description = "Swaps the menu entries of temple trek related menu items",
		position = 1
	)
	String swapperSection = "Menu Entry Swaps";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show Overlay",
		description = "Toggle whether or not to show the overlay during temple trekking or not",
		section = toggleSection
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "logData",
		name = "Log Data",
		description = "Toggle whether or not to log the data of each temple trek, to a file in the .runelite folder.",
		section = toggleSection
	)
	default boolean logData()
	{
		return false;
	}

	@ConfigItem(
		keyName = "swapContinueTrek",
		name = "Prioritize Continue-trek",
		description = "Prioritizes Continue-trek over Look-at",
		section = swapperSection
	)
	default boolean swapContinueTrek()
	{
		return true;
	}

	@ConfigItem(
		keyName = "swapUsePouch",
		name = "Prioritize Use Druid Pouch",
		description = "Prioritizes Use over Fill on the Druid Pouch",
		section = swapperSection
	)
	default boolean swapUsePouch()
	{
		return true;
	}

	@ConfigItem(
		keyName = "swapEscort",
		name = "Prioritize Escort",
		description = "Prioritizes Escort over Talk to on temple trek companions",
		section = swapperSection
	)
	default boolean swapEscort()
	{
		return true;
	}
}
