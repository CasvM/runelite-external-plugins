package com.templetracker;

import com.templetracker.menuentryswapper.MenuSwapperPlugin;
import com.templetracker.overlay.TempleTrackerPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunClientAndPlugin
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TempleTrackerPlugin.class, MenuSwapperPlugin.class);
		RuneLite.main(args);
	}
}