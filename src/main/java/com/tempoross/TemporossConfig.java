package com.tempoross;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import java.awt.Color;

@ConfigGroup("tempoross")
public interface TemporossConfig extends Config
{
	@ConfigItem(
		keyName = "highlightFires",
		name = "Highlight Fires",
		description = "Draws a square around the fires, and shows a timer when a fire spawns, or when a fire is going to spread",
		position = 0
	)
	default boolean highlightFires()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fireColor",
		name = "Fire Color",
		description = "Color of the Fire highlight tiles",
		position = 1
	)
	default Color fireColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		keyName = "fireNotification",
		name = "Storm Cloud Notification",
		description = "Notify when a storm clouds appear",
		position = 2
	)
	default boolean fireNotification()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightDoubleSpot",
		name = "Highlight Double Fishing Spot",
		description = "Highlights the fishing spot where you can get double fish as well as a timer when it approximately depletes",
		position = 3
	)
	default boolean highlightDoubleSpot()
	{
		return true;
	}

	@ConfigItem(
		keyName = "doubleSpotColor",
		name = "Double Spot Color",
		description = "Color of the Double Spot highlight tiles",
		position = 4
	)
	default Color doubleSpotColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
			keyName = "doubleSpotNotification",
			name = "Double Spot Notification",
			description = "Notify when a double spot appears",
			position = 5
	)
	default boolean doubleSpotNotification()
	{
		return false;
	}

	@ConfigItem(
		keyName = "useWaveTimer",
		name = "Enable Wave Timer",
		description = "Shows a timer that indicates when the wave damage will hit on a totem pole",
		position = 6
	)
	default boolean useWaveTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "waveTimerColor",
		name = "Wave Timer Color",
		description = "Color of the Wave Timer when untethered",
		position = 7
	)
	default Color waveTimerColor()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "tetheredColor",
		name = "Tethered Color",
		description = "Color of the Wave Timer when tethered",
		position = 8
	)
	default Color tetheredColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "poleBrokenColor",
		name = "Broken Pole Color",
		description = "Color of the Wave Timer when the pole/mast is broken",
		position = 9
	)
	default Color poleBrokenColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "waveNotification",
		name = "Wave Incoming Notification",
		description = "Notify when a wave is incoming",
		position = 10
	)
	default boolean waveNotification()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fishIndicator",
		name = "Show fish amount",
		description = "Shows the amount of cooked, and uncooked fish in your inventory, and how much damage that does to the boss",
		position = 11
	)
	default boolean fishIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "damageIndicator",
		name = "Show damage",
		description = "Shows the amount of damage you can do to the boss with the fish in your inventory",
		position = 12
	)
	default boolean damageIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "phaseIndicator",
		name = "Show phases",
		description = "Shows which phase of tempoross you're on",
		position = 13
	)
	default boolean phaseIndicator()
	{
		return true;
	}

	@ConfigItem(
		keyName = "vulnerableNotification",
		name = "Vulnerable Notification",
		description = "Notify when Tempoross is vulnerable",
		position = 14
	)
	default boolean vulnerableNotification()
	{
		return false;
	}

	@ConfigItem(
			keyName = "stormIntensityNotification",
			name = "Storm Intensity Notification",
			description = "Notify when The storm intensity is above 90%",
			position = 15
	)
	default boolean stormIntensityNotification()
	{
		return false;
	}
}
