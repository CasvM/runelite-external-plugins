package com.templetracker.menuentryswapper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("templetrekswapper")
public interface MenuSwapperConfig extends Config
{
	@ConfigItem(
		keyName = "swapContinueTrek",
		name = "Prioritize Continue-trek",
		description = "Prioritizes Continue-trek over Look-at"
	)
	default boolean swapContinueTrek()
	{
		return true;
	}

	@ConfigItem(
		keyName = "swapUsePouch",
		name = "Prioritize Use Druid Pouch",
		description = "Prioritizes Use over Fill on the Druid Pouch"
	)
	default boolean swapUsePouch()
	{
		return true;
	}

	@ConfigItem(
		keyName = "swapEscort",
		name = "Prioritize Escort",
		description = "Prioritizes Escort over Talk to on temple trek companions"
	)
	default boolean swapEscort()
	{
		return true;
	}
}
