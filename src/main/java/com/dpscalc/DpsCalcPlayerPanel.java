package com.dpscalc;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import com.dpscalc.beans.EquipmentSlot;
import com.dpscalc.beans.EquipmentSlotData;
import com.dpscalc.beans.EquipmentSlotItem;
import com.dpscalc.beans.Stances;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

@Slf4j
public class DpsCalcPlayerPanel extends JPanel {
	private final int MAX_DISPLAY_ITEMS = 100;
	private final ItemManager itemManager;
	private final DpsCalcPlugin plugin;
	private final Player player;
	private final Client client;
	private final HashMap<Integer, EquipmentSlotData> equipmentSlotData;
	private EquipmentSlot currentSlot;

	private final MaterialTabGroup equipmentSlotTabGroup;
	private final HashMap<Integer, MaterialTab> equipmentTabs = new HashMap<>();
	private final IconTextField searchBar = new IconTextField();
	private final JPanel equipmentSlotItemsPanel = new JPanel();
	private final GridBagConstraints constraints = new GridBagConstraints();

	private final JSpinner attackLevel;
	private final JSpinner strengthLevel;
	private final JSpinner magicLevel;
	private final JSpinner rangeLevel;
	private final JButton getStats;
	private final String[] meleePrayerStrings = {"None", "5%", "10%", "15%", "Chivalry", "Piety"};
	private final String[] rangePrayerStrings = {"None", "5%", "10%", "15%", "Rigour"};
	private final String[] magicPrayerStrings = {"None", "5%", "10%", "15%", "Augury"};
	private final String[] potionStrings = {"None", "Normal", "Super", "Overload"};
	private final JComboBox attackPrayer;
	private final JComboBox strengthPrayer;
	private final JComboBox rangePrayer;
	private final JComboBox magicPrayer;
	private final JComboBox potion;
	private final JComboBox combatStance;
	private final JComboBox spells;
	private final String[] unarmed = {"punch/crush/accurate", "kick/crush/aggressive", "block/defensive/crush"};
	private final JComboBox equipmentSetsPanel;

	DpsCalcPlayerPanel(Client client, DpsCalcPlugin plugin, ItemManager itemManager, Player player) {
		setLayout(new BorderLayout(5, 5));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		ToolTipManager.sharedInstance().setDismissDelay(99999999);

		this.plugin = plugin;
		this.itemManager = itemManager;
		this.player = player;
		this.client = client;

		equipmentSlotData = loadData();

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

		equipmentSlotTabGroup = new MaterialTabGroup();
		equipmentSlotTabGroup.setLayout(new GridLayout(5, 1, 7, 7));
		equipmentSlotTabGroup.setBorder(new EmptyBorder(0, 0, 10, 0));

		equipmentSlotItemsPanel.setLayout(new GridBagLayout());
		equipmentSlotItemsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel playerStatsInputPanel = new JPanel();
		playerStatsInputPanel.setLayout(new GridLayout(5, 2, 7, 7));
		playerStatsInputPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		attackLevel = addSpinnerComponent("Attack Level", player.getAttackLevel(), 1, 99, 1, playerStatsInputPanel);
		attackPrayer = addDropDownComponent("Prayer", meleePrayerStrings, playerStatsInputPanel, player.getAttackPrayerIndex());
		strengthLevel = addSpinnerComponent("Strength Level", player.getStrengthLevel(), 1, 99, 1, playerStatsInputPanel);
		strengthPrayer = addDropDownComponent("Prayer", meleePrayerStrings, playerStatsInputPanel, player.getStrengthPrayerIndex());
		rangeLevel = addSpinnerComponent("Range Level", player.getRangeLevel(), 1, 99, 1, playerStatsInputPanel);
		rangePrayer = addDropDownComponent("Prayer", rangePrayerStrings, playerStatsInputPanel, player.getRangePrayerIndex());
		magicLevel = addSpinnerComponent("Magic Level", player.getMagicLevel(), 1, 99, 1, playerStatsInputPanel);
		magicPrayer = addDropDownComponent("Prayer", magicPrayerStrings, playerStatsInputPanel, player.getMagicPrayerIndex());
		getStats = addButtonComponent(" ", "Import Stats", playerStatsInputPanel);
		getStats.setToolTipText("Click to use your character's stats.");
		potion = addDropDownComponent("Potion", potionStrings, playerStatsInputPanel, player.getPotionIndex());
		getStats.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (client.getLocalPlayer() != null) {
					attackLevel.setValue(client.getRealSkillLevel(Skill.ATTACK));
					strengthLevel.setValue(client.getRealSkillLevel(Skill.STRENGTH));
					magicLevel.setValue(client.getRealSkillLevel(Skill.MAGIC));
					rangeLevel.setValue(client.getRealSkillLevel(Skill.RANGED));
				} else {
					JOptionPane.showMessageDialog(null, "You must be logged in for this to work.", "Import Stats", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.addKeyListener(e -> searchItems());
		searchBar.addClearListener(() -> renderItems(this.currentSlot));

		equipmentSetsPanel = addDropDownComponent("Sets", player.getEquipmentSets().keySet().toArray(new String[player.getEquipmentSets().size()]), container,
				new ArrayList<>(player.getEquipmentSets().keySet()).indexOf(player.getSelectedSetName()));
		AutoCompletion.enable(equipmentSetsPanel);
		equipmentSetsPanel.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player.setSelectedSetName((String) equipmentSetsPanel.getSelectedItem());
				player.setCurrentSet(new HashMap<>(player.getSelectedSet()));
				setAllEquipmentSlots(player.getSelectedSet());
			}
		});

		container.add(Box.createVerticalStrut(10));
		container.add(equipmentSlotTabGroup);
		container.add(playerStatsInputPanel);
		combatStance = addDropDownComponent("Stance", player.getCombatStance().toArray(new String[] {}), container, player.getCombatStanceIndex());
		container.add(Box.createVerticalStrut(10));
		spells = addDropDownComponent("Spell", player.getSpells().keySet().toArray(new String[player.getSpells().size()]), container, player.getSpellIndex());
		AutoCompletion.enable(spells);
		container.add(Box.createVerticalStrut(10));
		container.add(searchBar);
		container.add(equipmentSlotItemsPanel);
		addEquipmentSlots();

		add(container, BorderLayout.CENTER);
	}

	private HashMap<Integer, EquipmentSlotData> loadData() {
		HashMap<Integer, EquipmentSlotData> data = new HashMap<>();

		for (EquipmentSlot slot: EquipmentSlot.values()) {
			InputStream equipmentSlotDataFile = DpsCalcPlugin.class.getResourceAsStream(slot.getDataFile());
			data.put(slot.getSlot().getSlotIdx(), new Gson().fromJson(new InputStreamReader(equipmentSlotDataFile), EquipmentSlotData.class));
		}
		return data;
	}

	private JSpinner addSpinnerComponent(String label, int init, int min, int max, int step, JPanel panel) {
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		final JLabel uiLabel = new JLabel(label);
		final SpinnerNumberModel model = new SpinnerNumberModel(init, min, max, step);
		final JSpinner uiInput = new JSpinner(model);

		uiInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JComponent editor = uiInput.getEditor();
		JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)editor;
		spinnerEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);

		uiLabel.setFont(FontManager.getRunescapeSmallFont());
		uiLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
		uiLabel.setForeground(Color.WHITE);

		container.add(uiLabel, BorderLayout.NORTH);
		container.add(uiInput, BorderLayout.CENTER);

		panel.add(container);

		return uiInput;
	}

	private JComboBox addDropDownComponent(String label, String[] options, JPanel panel, int init) {
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		final JLabel uiLabel = new JLabel(label);
		final JComboBox uiInput = new JComboBox(options);

		uiInput.setRenderer(new ComboBoxListRenderer());
		uiInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		uiInput.setSelectedIndex(init);

		uiLabel.setFont(FontManager.getRunescapeSmallFont());
		uiLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
		uiLabel.setForeground(Color.WHITE);

		container.add(uiLabel, BorderLayout.NORTH);
		container.add(uiInput, BorderLayout.CENTER);

		panel.add(container);

		return uiInput;
	}

	private JButton addButtonComponent(String label, String buttonLabel, JPanel panel) {
		final JPanel container = new JPanel();
		container.setLayout(new BorderLayout());

		final JLabel uiLabel = new JLabel(label);
		JButton uiInput = new JButton(buttonLabel);
		uiInput.setFocusPainted(false);
		uiInput.setFont(FontManager.getRunescapeSmallFont());
		uiInput.setBorder(new EmptyBorder(3, 0, 0, 0));

		uiLabel.setFont(FontManager.getRunescapeSmallFont());
		uiLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
		uiLabel.setForeground(Color.WHITE);

		container.add(uiLabel, BorderLayout.NORTH);
		container.add(uiInput, BorderLayout.CENTER);

		panel.add(container);

		return uiInput;
	}

	private MaterialTab addTabComponent(EquipmentSlot equipmentSlot, ImageIcon icon, MaterialTabGroup tabGroup) {
		MaterialTab tab = new MaterialTab(icon, tabGroup, null);
		tab.setOnSelectEvent(() ->
		{
			this.currentSlot = equipmentSlot;
			renderItems(this.currentSlot);
			return true;
		});
		tabGroup.addTab(tab);

		return tab;
	}

	void addItemPanel(EquipmentSlotItem item) {
		ImageIcon itemIcon;
		DpsCalcPlayerItemPanel panel;
		if (item == null) {
			itemIcon = new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), this.currentSlot.getImageFile()));
			panel = new DpsCalcPlayerItemPanel(this, itemIcon, "None", currentSlot, null);
		} else {
			itemIcon = new ImageIcon(itemManager.getImage(item.getId()));
			panel = new DpsCalcPlayerItemPanel(this, itemIcon, item.getName(), this.currentSlot, item);
		}
		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
		marginWrapper.add(panel, BorderLayout.NORTH);
		equipmentSlotItemsPanel.add(marginWrapper, constraints);
		constraints.gridy++;
	}

	private void addEquipmentSlots()
	{
		// delete set
		MaterialTab deleteSetTab = new MaterialTab("<html>" + "Delete Set" + "</html>", equipmentSlotTabGroup, null);
		deleteSetTab.setToolTipText("<html>" + "Click to delete the currently selected set." + "</html>");
		deleteSetTab.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String setName = (String) equipmentSetsPanel.getSelectedItem();
				int result = JOptionPane.showConfirmDialog(null, "Delete " + setName + "?",
						"Delete Set", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					if (!player.getSelectedSetName().toLowerCase().equals("new")) {
						int idx = equipmentSetsPanel.getSelectedIndex();
						player.getEquipmentSets().remove(player.getSelectedSetName());
						player.setSelectedSetName("New");
						player.setCurrentSet(player.getSelectedSet());
						equipmentSetsPanel.setSelectedIndex(0);
						equipmentSetsPanel.removeItemAt(idx);
						setAllEquipmentSlots(player.getSelectedSet());
					} else {
						JOptionPane.showMessageDialog(null, "This set cannot be deleted.", "Delete Set", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});
		equipmentSlotTabGroup.addTab(deleteSetTab);
		// helmet
		equipmentTabs.put(0, addTabComponent(EquipmentSlot.HEAD,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.HEAD.getImageFile())),
				this.equipmentSlotTabGroup));
		// save set
		MaterialTab saveSetTab = new MaterialTab("<html>" + "Save Set" + "</html>", equipmentSlotTabGroup, null);
		saveSetTab.setToolTipText("<html>" + "Click to save your current gear setup.<br>Enter the same name to override a setup." + "</html>");
		saveSetTab.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				String setName = JOptionPane.showInputDialog(null, "Set Name", "Save Set", JOptionPane.QUESTION_MESSAGE);
				if (setName.toLowerCase().equals("new")) {
					JOptionPane.showMessageDialog(null,"Please choose a different name.", "Save Set", JOptionPane.WARNING_MESSAGE);
				} else {
					if (!player.getEquipmentSets().containsKey(setName)) equipmentSetsPanel.addItem(setName);
					player.getEquipmentSets().put(setName, player.getCurrentSet());
					player.setSelectedSetName(setName);
					equipmentSetsPanel.setSelectedItem(setName);
					JOptionPane.showMessageDialog(null,setName + " saved.", "Save Set", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		equipmentSlotTabGroup.addTab(saveSetTab);
		// cape
		equipmentTabs.put(1, addTabComponent(EquipmentSlot.CAPE,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.CAPE.getImageFile())),
				this.equipmentSlotTabGroup));
		// amulet
		equipmentTabs.put(2, addTabComponent(EquipmentSlot.AMULET,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.AMULET.getImageFile())),
				this.equipmentSlotTabGroup));
		// ammo
		equipmentTabs.put(13, addTabComponent(EquipmentSlot.AMMO,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.AMMO.getImageFile())),
				this.equipmentSlotTabGroup));
		// weapon
		equipmentTabs.put(3, addTabComponent(EquipmentSlot.WEAPON,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.WEAPON.getImageFile())),
				this.equipmentSlotTabGroup));
		// body
		equipmentTabs.put(4, addTabComponent(EquipmentSlot.BODY,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.BODY.getImageFile())),
				this.equipmentSlotTabGroup));
		// shield
		equipmentTabs.put(5, addTabComponent(EquipmentSlot.SHIELD,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.SHIELD.getImageFile())),
				this.equipmentSlotTabGroup));
		// pull gear
		MaterialTab importGearTab = new MaterialTab("<html>" + "Import Gear" + "</html>", equipmentSlotTabGroup, null);
		importGearTab.setToolTipText("Click to use your character's equipped gear");
		importGearTab.setOnSelectEvent(() -> {
			if (client.getLocalPlayer() != null) {
				Item[] equippedGear = client.getItemContainer(InventoryID.EQUIPMENT).getItems();
				for (EquipmentSlot slot: EquipmentSlot.values()) {
					int slotID = slot.getSlot().getSlotIdx();
					if (slotID > equippedGear.length || equippedGear[slotID].getId() > -1) {
						for (EquipmentSlotItem item : equipmentSlotData.get(slotID).getEquipmentSlotItems()) {
							if (item.getId() == equippedGear[slotID].getId()) {
								player.getCurrentSet().put(slotID, item);
								break;
							}
						}
					} else {
						player.getCurrentSet().remove(slotID);
					}
				}
				setAllEquipmentSlots(player.getCurrentSet());
			} else {
				JOptionPane.showMessageDialog(null, "You must be logged in for this to work.", "Import Gear", JOptionPane.WARNING_MESSAGE);
			}
			return true;
		});
		equipmentSlotTabGroup.addTab(importGearTab);
		// legs
		equipmentTabs.put(7, addTabComponent(EquipmentSlot.LEGS,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.LEGS.getImageFile())),
				this.equipmentSlotTabGroup));
		// attack bonuses
		MaterialTab attackBonusTab = new MaterialTab("<html>" + "Attack Bonuses" + "</html>", equipmentSlotTabGroup, null);
		attackBonusTab.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e) {
				String tooltip = "<html>" +
						"Stab: " + player.getTotalBonuses().getAttack_stab() + "<br>" +
						"Slash: " + player.getTotalBonuses().getAttack_slash() + "<br>" +
						"Crush: " + player.getTotalBonuses().getAttack_crush() + "<br>" +
						"Magic: " + player.getTotalBonuses().getAttack_magic() + "<br>" +
						"Range: " + player.getTotalBonuses().getAttack_ranged() + "<br><br>" +
						"Melee Strength: " + player.getTotalBonuses().getMelee_strength() + "<br>" +
						"Magic Damage: " + player.getTotalBonuses().getMagic_damage() + "<br>" +
						"Ranged Strength: " + player.getTotalBonuses().getRanged_strength() +
						"</html>";
				attackBonusTab.setToolTipText(tooltip);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				attackBonusTab.unselect();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				attackBonusTab.unselect();
			}
		});
		equipmentSlotTabGroup.addTab(attackBonusTab);
		// gloves
		equipmentTabs.put(9, addTabComponent(EquipmentSlot.GLOVES,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.GLOVES.getImageFile())),
				this.equipmentSlotTabGroup));
		// boots
		equipmentTabs.put(10, addTabComponent(EquipmentSlot.BOOTS,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.BOOTS.getImageFile())),
				this.equipmentSlotTabGroup));
		//  ring
		equipmentTabs.put(12, addTabComponent(EquipmentSlot.RING,
				new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), EquipmentSlot.RING.getImageFile())),
				this.equipmentSlotTabGroup));
		setAllEquipmentSlots(player.getCurrentSet());
	}

	private void renderItems(EquipmentSlot slot) {
		int count = 0;
		constraints.gridy = 0;
		equipmentSlotItemsPanel.removeAll();
		addItemPanel(null);
		for (EquipmentSlotItem item : equipmentSlotData.get(slot.getSlot().getSlotIdx()).getEquipmentSlotItems()) {
			if (count++ > MAX_DISPLAY_ITEMS) {
				break;
			}
			addItemPanel(item);
		}
		revalidate();
		repaint();
	}

	private void searchItems() {
		constraints.gridy = 0;
		String query = searchBar.getText().toLowerCase();
		equipmentSlotItemsPanel.removeAll();
		int count = 0;
		for (EquipmentSlotItem item : equipmentSlotData.get(this.currentSlot.getSlot().getSlotIdx()).getEquipmentSlotItems()) {
			if (!item.getName().toLowerCase().contains(query)) {
				continue;
			}
			if (count++ > MAX_DISPLAY_ITEMS) {
				break;
			}
			addItemPanel(item);
		}
		revalidate();
		repaint();
	}

	void clearEquipmentSlot(EquipmentSlot slot) {
		int slotID = slot.getSlot().getSlotIdx();
		equipmentTabs.get(slotID).setIcon(new ImageIcon(ImageUtil.getResourceStreamFromClass(getClass(), slot.getImageFile())));
		if (slotID == 3) {
			combatStance.removeAllItems();
			for (String entry : unarmed) {
				combatStance.addItem(entry);
			}
			player.setCombatStanceIndex(0);
			combatStance.setSelectedIndex(0);
		}
		player.getCurrentSet().remove(slotID);
	}

	void setEquipmentSlot(ImageIcon icon, EquipmentSlot slot, EquipmentSlotItem equipmentSlotItem) {
		int slotID = slot.getSlot().getSlotIdx();
		equipmentTabs.get(slotID).setIcon(icon);
		if (slotID == 3) {
			combatStance.removeAllItems();
			player.getCombatStance().clear();
			for (Stances entry : equipmentSlotItem.getWeapon().getStances()) {
				combatStance.addItem(entry.getCombat_style() + "/" + entry.getAttack_type() + "/" + entry.getAttack_style());
				player.getCombatStance().add(entry.getCombat_style() + "/" + entry.getAttack_type() + "/" + entry.getAttack_style());
			}
			combatStance.setSelectedIndex(player.getCombatStanceIndex());
		}
		player.getCurrentSet().put(slotID, equipmentSlotItem);
	}

	private void setAllEquipmentSlots(HashMap<Integer, EquipmentSlotItem> set) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			try {
				ImageIcon icon = new ImageIcon(itemManager.getImage(set.get(slot.getSlot().getSlotIdx()).getId()));
				setEquipmentSlot(icon, slot, set.get(slot.getSlot().getSlotIdx()));
			} catch (Exception ex) {
				clearEquipmentSlot(slot);
			}
		}
	}

	void setPlayerStats() {
		player.setAttackLevel((int) attackLevel.getValue());
		player.setStrengthLevel((int) strengthLevel.getValue());
		player.setMagicLevel((int) magicLevel.getValue());
		player.setRangeLevel((int) rangeLevel.getValue());
		player.setAttackPrayerIndex(attackPrayer.getSelectedIndex());
		player.setStrengthPrayerIndex(strengthPrayer.getSelectedIndex());
		player.setRangePrayerIndex(rangePrayer.getSelectedIndex());
		player.setMagicPrayerIndex(magicPrayer.getSelectedIndex());
		player.setPotionIndex(potion.getSelectedIndex());
		player.setCombatStanceIndex(combatStance.getSelectedIndex());
		player.setSpellIndex(spells.getSelectedIndex());
		HashMap<Integer, EquipmentSlotItem> set = player.getCurrentSet();
		try {
			set.get(0).getName().toLowerCase().contains("void");
			set.get(4).getName().toLowerCase().contains("void");
			set.get(7).getName().toLowerCase().contains("void");
			set.get(9).getName().toLowerCase().contains("void");
			player.setVoidSet(true);
		} catch (Exception e) {
			player.setVoidSet(false);
		}
	}
}
