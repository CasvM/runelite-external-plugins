package com.dpscalc;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.dpscalc.beans.ItemStats;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;

@PluginDescriptor(
		name = "DPS Calculator",
		description = "Calculate DPS of gear setup",
		enabledByDefault = false
)
@Setter
@Getter
@Slf4j
public class DpsCalcPlugin extends Plugin {
	private NavigationButton uiNavigationButton;
	private DpsCalcPanel uiPanel;
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private Client client;

	private Player player;
	private Target target;

	@Override
	protected void startUp() throws Exception
	{
		try {
			FileInputStream fis = new FileInputStream(new File(RUNELITE_DIR, "configPlayer"));
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.player = (Player) ois.readObject();
			ois.close();
			fis.close();

			fis = new FileInputStream(new File(RUNELITE_DIR, "configTarget"));
			ois = new ObjectInputStream(fis);
			this.target = (Target) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			log.debug(e.toString());
			this.player = new Player();
			this.target = new Target();
		}
		uiPanel = new DpsCalcPanel(client, this, itemManager, this.player, this.target);
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "icon.png");
		uiNavigationButton = NavigationButton.builder()
				.tooltip("Dps Calculator")
				.icon(icon)
				.priority(6)
				.panel(uiPanel)
				.build();

		clientToolbar.addNavigation(uiNavigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(uiNavigationButton);
		saveConfig();
	}

	public void saveConfig() {
		uiPanel.getPlayerPanel().setPlayerStats();
		uiPanel.getTargetPanel().setTargetStats();
		try {
			FileOutputStream fos = new FileOutputStream(new File(RUNELITE_DIR, "configPlayer"));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(player);
			oos.close();
			fos.close();

			fos = new FileOutputStream(new File(RUNELITE_DIR, "configTarget"));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(target);
			oos.close();
			fos.close();
		} catch (Exception e) {
			log.debug(e.toString());
		}
	}

	String[] calculateDPS() {
		uiPanel.getPlayerPanel().setPlayerStats();
		String[] results = {"--", "--", "--", "--"};
		int maxRoll = 0;
		int targetMaxRoll = 0;
		double maxHit = 0;
		double accuracy = 0;
		double dps = 0;
		double timeToKill = 0;
		ItemStats stats = player.getTotalBonuses();
		int attackSpeed = player.attackSpeed();
		int effectiveStrengthLevel = player.getEffectiveStrengthLevel();
		int meleeStrength = stats.getMelee_strength();
		int effectiveAttackLevel = player.getEffectiveAttackLevel();
		int statsAttackStab = stats.getAttack_stab();
		int num = 0;
		int targetDefenseBonus = 0;
		double targetEffectiveDefenseLevel = target.getDefenseLevel() * Math.pow(0.7, target.getDwh()) * Math.pow(0.95, target.getArclight()) - target.getBgs();
		switch (player.attackType()[1]) {
			case "slash":
				targetDefenseBonus = target.getSlashDefenseBonus();
				statsAttackStab = stats.getAttack_slash();
				break;
			case "crush":
				targetDefenseBonus = target.getCrushDefenseBonus();
				statsAttackStab = stats.getAttack_crush();
				break;
			case "stab":
				targetDefenseBonus = target.getStabDefenseBonus();
				statsAttackStab = stats.getAttack_stab();
				break;
		}
		maxHit = (int) (0.5 + effectiveStrengthLevel * (meleeStrength + 64) / 640.0);
		if (player.getCurrentSet().containsKey(3) && player.getCurrentSet().get(3).getEquipment().getAttack_ranged() > 0) {
			effectiveStrengthLevel = player.getEffectiveRangeStrengthLevel();
			meleeStrength = stats.getRanged_strength();
			effectiveAttackLevel = player.getEffectiveRangeAttackLevel();
			statsAttackStab = stats.getAttack_ranged();
			targetDefenseBonus = target.getRangedDefenseBonus();
			num = 1;
			maxHit = (int) (0.5 + effectiveStrengthLevel * (meleeStrength + 64) / 640.0);
		}
		if (player.getSpellIndex() != 0) {
			effectiveAttackLevel = player.getEffectiveMagicLevel();
			statsAttackStab = stats.getAttack_magic();
			targetDefenseBonus = target.getMagicDefenseBonus();
			targetEffectiveDefenseLevel = target.getMagicLevel();
			num = 2;
			int spellMaxHit = player.getSpellDamage();
			maxHit = (int) (spellMaxHit * (1 + stats.getMagic_damage() / 100.0));
		}
		maxHit = (int) (maxHit * target.getSalve()[num] * target.getSlayerBonus()[num] * target.getDemon() * target.getDragon() *
				player.inquisitor() * twistedBow()[0] * player.tomeOfFire() * target.getKalphite());
		maxRoll = effectiveAttackLevel * (statsAttackStab + 64);
		maxRoll = (int) (maxRoll * target.getSalve()[num] * target.getSlayerBonus()[num] * target.getDemon() * target.getDragon() *
				player.inquisitor() * twistedBow()[1] * target.getKalphite());
		targetMaxRoll = (int) ((targetEffectiveDefenseLevel + 9) * (targetDefenseBonus + 64));
		if (maxRoll > targetMaxRoll) {
			accuracy = 1 - (targetMaxRoll + 2) / (2.0 * (maxRoll + 1));
		} else {
			accuracy = maxRoll / (2.0 * (targetMaxRoll + 1));
		}
		dps = maxHit * accuracy / (attackSpeed * 1.2);
		if (target.getSize() > 3 && player.scythe()) {
			results[0] = (int) maxHit + ", " + (int) (maxHit * 0.5) + ", " + (int) (maxHit * 0.25);
			dps = dps + (dps * 0.5) + (dps * 0.25);
		} else {
			results[0] = String.valueOf((int) maxHit);
		}
		timeToKill = target.getHpLevel() / dps;
		results[1] = String.format("%.3f", accuracy * 100);
		results[2] = String.format("%.3f", dps);
		results[3] = String.format("%.3f", timeToKill);

		return results;
	}

	double[] twistedBow() {
		double[] twistedBow = {1, 1};
		if (player.getCurrentSet().containsKey(3) && player.getCurrentSet().get(3).getName().toLowerCase().equals("twisted bow")) {
			twistedBow[0] = ((250 + (int) ((3 * target.getMagicLevel() - 14) / 100.0) - (int)(Math.pow(0.3 * target.getMagicLevel() - 140, 2)) / 100.0) / 100.0);
			twistedBow[1] = ((140 + (int) ((3 * target.getMagicLevel() - 10) / 100.0) - (int) (Math.pow(0.3 * target.getMagicLevel() - 100, 2) / 100.0)) / 100.0);
			if (twistedBow[1] > 1.4) {
				twistedBow[1] = 1.4;
			}
		}
		return twistedBow;
	}
}
