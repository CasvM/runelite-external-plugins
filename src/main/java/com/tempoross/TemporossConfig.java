package com.tempoross;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("tempoross")
public interface TemporossConfig extends Config
{
	@ConfigItem(
		keyName = "highlightFires",
		name = "Highlight Fires",
		description = "Highlight fire",
		position = 0
	)

	default boolean highlightFires()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useFireTimer",
		name = "Enable Fire Timer",
		description = "Shows a timer that indicates when the damage of the fire attack will hit",
		position = 1
	)

	default boolean useFireTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "fireColor",
		name = "Fire Color",
		description = "Color of the Fire highlight tiles",
		position = 2
	)

	default Color fireColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		keyName = "highlightDoubleSpot",
		name = "Highlight Double Fishing Spot",
		description = "Highlights the fishing spot where you can get double fish",
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
		keyName = "useDoubleSpotTimer",
		name = "Enable Double Spot Timer",
		description = "Shows a timer that roughly indicates how long the double fishing spot lasts",
		position = 5
	)

	default boolean useDoubleSpotTimer()
	{
		return true;
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
		keyName = "cookIndicator",
		name = "Show amount of fish to cook",
		description = "Shows a number that indicates how much fish in your inventory you need to cook to get Tempoross' energy to 0 (works mainly in solo's)",
		position = 9
	)

	default boolean cookIndicator()
	{
		return true;
	}

	@Range(
		min = 0,
		max = 100
	)
	@ConfigItem(
		keyName = "targetPercentage",
		name = "Target percentage",
		description = "Target percentage of energy left, so that, the cook indicator shows how much fish you need to fish/cook to reach that percentage.",
		position = 10
	)

	default int targetPercentage() {
		return 0;
	}
}
