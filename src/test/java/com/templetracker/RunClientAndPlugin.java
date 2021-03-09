package com.templetracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunClientAndPlugin
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TempleTrackerPlugin.class);
		RuneLite.main(args);
	}
}