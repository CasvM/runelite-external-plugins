package com.dpscalc;

import lombok.Getter;

import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DpsCalcPanel extends PluginPanel {
	private final JPanel display = new JPanel();

	@Getter
	private final DpsCalcTargetPanel targetPanel;
	@Getter
	private final DpsCalcPlayerPanel playerPanel;

	@Inject
	public DpsCalcPanel(Client client, DpsCalcPlugin plugin, ItemManager itemManager, Player player, Target target)
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		playerPanel = new DpsCalcPlayerPanel(client, plugin, itemManager, player);
		targetPanel = new DpsCalcTargetPanel(plugin, target);

		MaterialTabGroup tabGroup = new MaterialTabGroup(display);
		MaterialTab playerTab = new MaterialTab("Player", tabGroup, playerPanel);
		MaterialTab targetTab = new MaterialTab("Target", tabGroup, targetPanel);

		tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
		tabGroup.addTab(playerTab);
		tabGroup.addTab(targetTab);
		tabGroup.select(playerTab);

		add(tabGroup, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);
	}
}
