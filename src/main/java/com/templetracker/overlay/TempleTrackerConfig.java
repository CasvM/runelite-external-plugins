package com.templetracker.overlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("templetracker")
public interface TempleTrackerConfig extends Config
{
	@ConfigItem(
		keyName = "showOverlay",
		name = "Show Overlay",
		description = "Toggle whether or not to show the overlay during temple trekking or not"
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "logData",
		name = "Log Data",
		description = "Toggle whether or not to log the data of each temple trek, to a file in the .runelite folder."
	)
	default boolean logData()
	{
		return false;
	}
}
