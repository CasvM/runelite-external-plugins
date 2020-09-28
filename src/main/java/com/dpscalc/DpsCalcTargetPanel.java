package com.dpscalc;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import com.dpscalc.beans.EquipmentSlotItem;
import com.dpscalc.beans.Monster;
import com.dpscalc.beans.MonsterData;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ComboBoxListRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

@Slf4j
public class DpsCalcTargetPanel extends JPanel {
	private final DpsCalcPlugin plugin;
	private final JLabel resultsPanel = new JLabel();
	private final Target target;

	private final JSpinner hpLevel;
	private final JSpinner attackLevel;
	private final JSpinner strengthLevel;
	private final JSpinner defenseLevel;
	private final JSpinner magicLevel;
	private final JSpinner rangeLevel;

	private final JSpinner stabBonus;
	private final JSpinner slashBonus;
	private final JSpinner crushBonus;
	private final JSpinner magicBonus;
	private final JSpinner rangeBonus;

	private final JCheckBox slayerTask;
	private final JCheckBox dragon;
	private final JCheckBox demon;
	private final JCheckBox undead;
	private final JCheckBox kalphite;
	private final JSpinner dwh;
	private final JSpinner bgs;
	private final JSpinner arclight;
	private final JSpinner size;

	private final JComboBox monsters;

	private String[] dps = {"--", "--", "--", "--"};

	DpsCalcTargetPanel(DpsCalcPlugin plugin, Target target) {
		this.plugin = plugin;
		this.target = target;

		setLayout(new BorderLayout(7, 7));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel targetStatsInputPanel = new JPanel();
		targetStatsInputPanel.setLayout(new GridLayout(10, 2, 7, 7));
		hpLevel = addSpinnerComponent("HP Level", target.getHpLevel(), 1, 999, 1, targetStatsInputPanel);
		stabBonus = addSpinnerComponent("Stab Bonus", target.getStabDefenseBonus(), -999, 999, 1, targetStatsInputPanel);
		attackLevel = addSpinnerComponent("Attack Level", target.getAttackLevel(), 0, 999, 1, targetStatsInputPanel);
		slashBonus = addSpinnerComponent("Slash Bonus", target.getStabDefenseBonus(), -999, 999, 1, targetStatsInputPanel);
		strengthLevel = addSpinnerComponent("Strength Level", target.getStrengthLevel(), 0, 999, 1, targetStatsInputPanel);
		crushBonus = addSpinnerComponent("Crush Bonus", target.getCrushDefenseBonus(), -999, 999, 1, targetStatsInputPanel);
		defenseLevel = addSpinnerComponent("Defense Level", target.getDefenseLevel(), 0, 999, 1, targetStatsInputPanel);
		magicBonus = addSpinnerComponent("Magic Bonus", target.getMagicDefenseBonus(), -999, 999, 1, targetStatsInputPanel);
		magicLevel = addSpinnerComponent("Magic Level", target.getMagicLevel(), 0, 999, 1, targetStatsInputPanel);
		rangeBonus = addSpinnerComponent("Range Bonus", target.getRangedDefenseBonus(), -999, 999, 1, targetStatsInputPanel);
		rangeLevel = addSpinnerComponent("Range Level", target.getRangeLevel(), 0, 999, 1, targetStatsInputPanel);
		undead = addCheckBoxComponent("Undead", targetStatsInputPanel, target.isUndead());
		dwh = addSpinnerComponent("Warhammer", target.getDwh(), 0, 999, 1, targetStatsInputPanel);
		slayerTask = addCheckBoxComponent("Slayer Task", targetStatsInputPanel, target.isSlayerTask());
		bgs = addSpinnerComponent("BGS", target.getBgs(), 0, 999, 1, targetStatsInputPanel);
		dragon = addCheckBoxComponent("Dragon", targetStatsInputPanel, target.getDragon() != 1.0);
		arclight = addSpinnerComponent("Arclight" , target.getArclight(), 0, 999, 1, targetStatsInputPanel);
		demon = addCheckBoxComponent("Demon", targetStatsInputPanel, target.getDemon() != 1.0);
		size = addSpinnerComponent("Size", target.getSize(), 1, 10, 1, targetStatsInputPanel);
		kalphite = addCheckBoxComponent("Kalphite", targetStatsInputPanel, target.getKalphite() != 1.0);

		InputStream monsterDataFile = DpsCalcPlugin.class.getResourceAsStream("monsters-complete.json");
		MonsterData monsterData = new Gson().fromJson(new InputStreamReader(monsterDataFile), MonsterData.class);
		monsters = new JComboBox();
		monsters.setRenderer(new ComboBoxListRenderer());
		monsters.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		AutoCompletion.enable(monsters);
		for (Monster monster: monsterData.getMonsters()) {
			monsters.addItem(monster.getName() + " ( Lvl. " + monster.getCombat_level() + ")");
		}
		monsters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Monster selectedMonster = monsterData.getMonsters()[monsters.getSelectedIndex()];
				target.setMonsterIndex(monsters.getSelectedIndex());

				hpLevel.setValue(selectedMonster.getHitpoints());
				attackLevel.setValue(selectedMonster.getAttack_level());
				strengthLevel.setValue(selectedMonster.getStrength_level());
				defenseLevel.setValue(selectedMonster.getDefence_level());
				magicLevel.setValue(selectedMonster.getMagic_level());
				rangeLevel.setValue(selectedMonster.getRanged_level());

				stabBonus.setValue(selectedMonster.getDefence_stab());
				slashBonus.setValue(selectedMonster.getDefence_slash());
				crushBonus.setValue(selectedMonster.getDefence_crush());
				magicBonus.setValue(selectedMonster.getDefence_magic());
				rangeBonus.setValue(selectedMonster.getDefence_ranged());

				size.setValue(selectedMonster.getSize());
			}
		});
		monsters.setSelectedIndex(target.getMonsterIndex());

		JPanel resultsContainer = new JPanel();
		resultsContainer.setLayout(new BorderLayout(7, 7));
		String results = "<html>" +
				"Max Hit: " + dps[0] + "<br>" +
				"Accuracy: " + dps[1] + "<br>" +
				"DPS: " + dps[2] + "<br>" +
				"Exp. secs to kill: " + dps[3] +
				"</html>";
		resultsPanel.setText(results);
		resultsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		resultsPanel.setOpaque(true);
		resultsPanel.setBorder(new EmptyBorder(7, 7, 7, 7));
		resultsPanel.setForeground(Color.WHITE);

		JButton calculateButton = new JButton("Calculate");
		calculateButton.setFocusPainted(false);
		calculateButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		calculateButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e) {
				setTargetStats();
				dps = plugin.calculateDPS();
				String results = "<html>" +
						"Max Hit: " + dps[0] + "<br>" +
						"Accuracy: " + dps[1] + "%<br>" +
						"DPS: " + dps[2] + "<br>" +
						"Exp. secs to kill: " + dps[3] +
						"</html>";
				resultsPanel.setText(results);
				repaint();
				revalidate();
			}
		});

		resultsContainer.add(resultsPanel, BorderLayout.NORTH);
		resultsContainer.add(calculateButton, BorderLayout.SOUTH);

		add(monsters, BorderLayout.NORTH);
		add(targetStatsInputPanel, BorderLayout.CENTER);
		add(resultsContainer, BorderLayout.SOUTH);
	}

	private JCheckBox addCheckBoxComponent(String label, JPanel panel, boolean init) {
		JCheckBox checkBox = new JCheckBox(label);

		checkBox.setBorderPainted(false);
		checkBox.setFocusPainted(false);
		checkBox.setBackground(ColorScheme.LIGHT_GRAY_COLOR);
		checkBox.setFont(FontManager.getRunescapeSmallFont());
		checkBox.setForeground(Color.WHITE);
		checkBox.setSelected(init);

		panel.add(checkBox);

		return checkBox;
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

	void setTargetStats() {
		HashMap<Integer, EquipmentSlotItem> gear = plugin.getPlayer().getCurrentSet();
		target.setHpLevel((int) hpLevel.getValue());
		target.setAttackLevel((int) attackLevel.getValue());
		target.setStrengthLevel((int) strengthLevel.getValue());
		target.setDefenseLevel((int) defenseLevel.getValue());
		target.setMagicLevel((int) magicLevel.getValue());
		target.setRangeLevel((int) rangeLevel.getValue());
		target.setStabDefenseBonus((int) stabBonus.getValue());
		target.setSlashDefenseBonus((int) slashBonus.getValue());
		target.setCrushDefenseBonus((int) crushBonus.getValue());
		target.setMagicDefenseBonus((int) magicBonus.getValue());
		target.setRangedDefenseBonus((int) rangeBonus.getValue());
		target.setBgs((int) bgs.getValue());
		target.setDwh((int) dwh.getValue());
		target.setArclight((int) arclight.getValue());
		target.setSize((int) size.getValue());
		if (dragon.isSelected() && gear.containsKey(3)) {
			switch (gear.get(3).getName().toLowerCase()) {
				case("dragon hunter lance"):
					target.setDragon(1.2);
					break;
				case("dragon hunter crossbow"):
					target.setDragon(1.3);
					break;
				default:
					target.setDragon(1.0);
					break;
			}
		} else {
			target.setDragon(1.0);
		}
		if (demon.isSelected() && gear.containsKey(3)) {
			target.setDemon(gear.get(3).getName().toLowerCase().contains("arclight") ? 1.7 : 1.0);
		} else {
			target.setDemon(1.0);
		}
		if (undead.isSelected() && gear.containsKey(2)) {
			target.setUndead(undead.isSelected());
			switch (gear.get(2).getName().toLowerCase()) {
				case("salve amulet"):
					target.setSalve(new double[] {7.0/6, 1.0, 1.0});
					break;
				case("salve amulet (e)"):
					target.setSalve(new double[] {1.2, 1.0, 1.0});
					break;
				case("salve amulet(i)"):
					target.setSalve(new double[] {7.0/6, 7.0/6, 1.15});
					break;
				case("salve amulet(ei)"):
					target.setSalve(new double[] {1.2, 1.2, 1.2});
					break;
				default:
					target.setSalve(new double[] {1.0, 1.0, 1.0});
					break;
			}
		} else {
			target.setSalve(new double[] {1.0, 1.0, 1.0});
			target.setUndead(false);
		}
		if (slayerTask.isSelected() && gear.containsKey(0)) {
			target.setSlayerTask(slayerTask.isSelected());
			switch (gear.get(0).getName().toLowerCase()) {
				case("blask mask"):
				case("slayer helmet"):
					target.setSlayerBonus(new double[] {7.0/6, 1.0, 1.0});
					break;
				case("black mask (i)"):
				case("slayer helmet (i)"):
					target.setSlayerBonus(new double[] {7.0/6, 1.15, 1.15});
					break;
				default:
					target.setSlayerBonus(new double[] {1.0, 1.0, 1.0});
					break;
			}
		} else {
			target.setSlayerBonus(new double[] {1.0, 1.0, 1.0});
			target.setSlayerTask(false);
		}
		if (kalphite.isSelected() && gear.containsKey(3)) {
			target.setKalphite(gear.get(3).getName().toLowerCase().contains("keris") ? 1 + 1.0/3 : 1.0);
		} else {
			target.setKalphite(1.0);
		}
	}
}
