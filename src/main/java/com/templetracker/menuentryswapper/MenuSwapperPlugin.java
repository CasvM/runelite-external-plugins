package com.templetracker.menuentryswapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Provides;
import com.templetracker.constructors.Companion;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Temple Trekking Swapper",
	description = "Adds the possibility to swap to continue-trek, escort (companion), and use on the druid pouch"
)
public class MenuSwapperPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private MenuSwapperConfig config;

	@Provides
	MenuSwapperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MenuSwapperConfig.class);
	}

	@Override
	public void startUp() {}

	@Override
	public void shutDown() {}

	private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

	@Subscribe(priority = 10)
	public void onClientTick(ClientTick clientTick)
	{
		// The menu is not rebuilt when it is open, so don't swap or else it will
		// repeatedly swap entries
		if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen())
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();

		// Build option map for quick lookup in findIndex
		int idx = 0;
		optionIndexes.clear();
		for (MenuEntry entry : menuEntries)
		{
			String option = Text.removeTags(entry.getOption()).toLowerCase();
			optionIndexes.put(option, idx++);
		}

		// Perform swaps
		idx = 0;
		for (MenuEntry entry : menuEntries)
		{
			swapMenuEntry(entry, menuEntries, idx++);
		}
	}

	private void swapMenuEntry(MenuEntry menuEntry, MenuEntry[] menuEntries, int index)
	{
		final String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
		final String target = Text.removeTags(menuEntry.getTarget()).toLowerCase();

		if (config.swapContinueTrek() && option.equals("look-at") && target.equals("path")) {
			swap("continue-trek", option, target, menuEntries, index);
		}
		else if (config.swapUsePouch() && option.equals("fill") && target.equals("druid pouch")) {
			swap("use", option, target, menuEntries, index);
		}
		else if (config.swapEscort() && option.equals("talk-to") && Companion.getCompanion(target) != null) {
			swap("escort", option, target, menuEntries, index);
		}
	}

	private void swap(String priority, String option, String target, MenuEntry[] entries, int index) {
		int thisIndex = findIndex(entries, index, option, target);
		int optionIdx = findIndex(entries, thisIndex, priority, target);

		if (thisIndex >= 0 && optionIdx >= 0)
		{
			swap(optionIdx, thisIndex, entries);
		}
	}

	private void swap(int index1, int index2, MenuEntry[] entries) {
		MenuEntry entry = entries[index1];
		entries[index1] = entries[index2];
		entries[index2] = entry;

		client.setMenuEntries(entries);
	}

	private int findIndex(MenuEntry[] entries, int limit, String option, String target) {
		List<Integer> indexes = optionIndexes.get(option);

		// We want the last index which matches the target, as that is what is top-most
		// on the menu
		for (int i = indexes.size() - 1; i >= 0; --i)
		{
			int idx = indexes.get(i);
			MenuEntry entry = entries[idx];
			String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();

			// Limit to the last index which is prior to the current entry
			if (idx <= limit && entryTarget.equals(target))
			{
				return idx;
			}
		}
		return -1;
	}

}
