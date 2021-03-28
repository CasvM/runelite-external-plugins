package com.tempoross;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.Color;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Tempoross"
)
public class TemporossPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private TemporossConfig config;

	@Inject
	private TemporossOverlay temporossOverlay;

	private final Set<Integer> TEMPOROSS_GAMEOBJECTS = ImmutableSet.of(41005, 41006, 41007, 41354, 41355, 41352, 41353, 40996, 40997, 41010, 41011);
	//41005 = fire burning
	//41006 = shadow before fire is burning
	//41007 = shadow just before fire is jumping over to a next spot
	//41354/41355 = a totem to grapple on to
	//41352/41353 = a mast to grapple on to
	//41010/41011 = a totem that is broken
	//40996/40997 = a broken mast

	private final Set<Integer> TEMPOROSS_NPCS = ImmutableSet.of(10569); //double fishing spot

	static private final int TEMPOROSS_ISTETHERED = 11895;
	static private final int TEMPOROSS_REWARDPOOL = 11936;

	@Getter
	private final Map<GameObject, DrawObject> gameObjects = new HashMap<>();

	@Getter
	private final Map<NPC, Instant> npcs = new HashMap<>();

	private final Map<GameObject, DrawObject> totemMap = new HashMap<>();

	private TemporossInfoBox rewardInfoBox;
	private TemporossInfoBox fishInfoBox;
	private TemporossInfoBox damageInfoBox;
	private TemporossInfoBox phaseInfoBox;

	private boolean waveIsIncoming;

	private int previousRegion;

	private int phase = 1;

	@Override
	protected void startUp()
	{
		overlayManager.add(temporossOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(temporossOverlay);
	}

	@Provides
	TemporossConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TemporossConfig.class);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (TEMPOROSS_GAMEOBJECTS.contains(gameObjectSpawned.getGameObject().getId()))
		{
			int duration = 7800;

			switch (gameObjectSpawned.getGameObject().getId())
			{
				//duration in millis for different fire objects to have something happen to them:
				//41005 -> spreads fire
				//41006 -> spawns fire from shadow
				//41007 -> spawns fire from shadow
				case 41005:
					duration = 24000;
					break;
				case 41006:
					duration = 9600;
					break;
				case 41007:
					duration = 1200;
					break;
				default:
					//if it is not one of the three above, it is a totem/mast and should be added to the totem map, with 7800ms duration
					totemMap.put(gameObjectSpawned.getGameObject(), new DrawObject(gameObjectSpawned.getTile(), gameObjectSpawned.getGameObject(), Instant.now(), duration, config.waveTimerColor()));
			}

			if (duration != 7800)
			{
				gameObjects.put(gameObjectSpawned.getGameObject(), new DrawObject(gameObjectSpawned.getTile(), gameObjectSpawned.getGameObject(), Instant.now(), duration, config.fireColor()));
			}

		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		GameObject object = gameObjectDespawned.getGameObject();
		gameObjects.remove(object);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (TEMPOROSS_NPCS.contains(npcSpawned.getNpc().getId()))
		{
			npcs.put(npcSpawned.getNpc(), Instant.now());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		npcs.remove(npcDespawned.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		int TEMPOROSS_REGION = 12078;
		int UNKAH_REWARD_POOL_REGION = 12588;
		int UNKAH_BOAT_REGION = 12332;

		int region = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();;

		log.info(region + " | " + previousRegion);

		if (region != TEMPOROSS_REGION && previousRegion == TEMPOROSS_REGION)
		{
			reset();
		}

		if (region == TEMPOROSS_REGION && previousRegion != TEMPOROSS_REGION)
		{
			setup();
		}

		if (region == UNKAH_BOAT_REGION || region == UNKAH_REWARD_POOL_REGION)
		{
			if (rewardInfoBox == null)
			{
				addRewardInfoBox();
			}

			updateRewardInfoBox();
		}
		else
		{
			removeRewardInfoBox();
		}

		previousRegion = region;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		updateRewardInfoBox();

		//TEMPOROSS_ISTETHERED > 0 means tethered, 0 untethered. The exact value depends on what totem you're tethered to
		if (client.getVarbitValue(TEMPOROSS_ISTETHERED) > 0 && waveIsIncoming)
		{
			updateTotemTimers(config.tetheredColor());
		}
		else if (client.getVarbitValue(TEMPOROSS_ISTETHERED) == 0 && waveIsIncoming)
		{
			updateTotemTimers(config.waveTimerColor());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (chatMessage.getMessage().toLowerCase().contains("a colossal wave closes in..."))
			{
				waveIsIncoming = true;
				addTotemTimers();
			}
			if (chatMessage.getMessage().toLowerCase().contains("as the wave washes over you") ||
				chatMessage.getMessage().toLowerCase().contains("the wave slams into you"))
			{
				waveIsIncoming = false;
				removeTotemTimers();
			}

			if (chatMessage.getMessage().toLowerCase().contains("tempoross is vulnerable"))
			{
				phaseInfoBox.setRewardCount(++phase);
				phaseInfoBox.setToolTipText("Phase " + phase);
			}

		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

		if (inventory == null || (!config.fishIndicator() && !config.damageIndicator()))
		{
			return;
		}

		int uncookedFish = findAmountById(inventory.getItems(), 25564);
		int cookedFish = findAmountById(inventory.getItems(), 25565);
		int crystalFish = findAmountById(inventory.getItems(), 25566);

		int damage = crystalFish * 10 + cookedFish * 15 + uncookedFish * 10;

		if (fishInfoBox == null && damageInfoBox == null)
		{
			//return if the player isn't fighting tempoross
			return;
		}

		String toolTip =
			"Uncooked Fish: " +
				(uncookedFish + crystalFish) +
				"</br>" +
				"Cooked Fish: " +
				cookedFish;

		String infoBoxText = (uncookedFish + crystalFish) +	"/" + cookedFish;

		if (config.fishIndicator())
		{
			fishInfoBox.setToolTipText(toolTip);
			fishInfoBox.setAlternateText(infoBoxText);
		}

		toolTip = "Damage: " + damage;
		infoBoxText = Integer.toString(damage);


		if (config.damageIndicator())
		{
			damageInfoBox.setToolTipText(toolTip);
			damageInfoBox.setAlternateText(infoBoxText);
		}


	}

	public void addTotemTimers()
	{
		if (config.useWaveTimer())
		{
			totemMap.forEach((object, drawObject) ->
			{
				drawObject.setStartTime(Instant.now());
				if (client.getVarbitValue(TEMPOROSS_ISTETHERED) > 0)
				{
					drawObject.setColor(config.tetheredColor());
				}
				else
				{
					drawObject.setColor(config.waveTimerColor());
				}
				gameObjects.put(object, drawObject);
			});
		}
	}

	public void updateTotemTimers(Color color)
	{
		if (config.useWaveTimer())
		{
			totemMap.forEach((object, drawObject) ->
			{
				drawObject.setColor(color);
				gameObjects.put(object, drawObject);
			});
		}
	}

	public void removeTotemTimers()
	{
		totemMap.forEach(gameObjects::remove);
	}

	public void addRewardInfoBox()
	{
		int rewardPoints = client.getVarbitValue(TEMPOROSS_REWARDPOOL);
		infoBoxManager.removeInfoBox(rewardInfoBox);
		rewardInfoBox = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "tome_of_water.png"), this, rewardPoints);
		rewardInfoBox.setToolTipText(rewardPoints  + " Reward Point" + (rewardPoints == 1 ? "" : "s"));
		infoBoxManager.addInfoBox(rewardInfoBox);
	}

	public void updateRewardInfoBox()
	{
		if (rewardInfoBox != null)
		{
			int rewardPoints = client.getVarbitValue(TEMPOROSS_REWARDPOOL);
			rewardInfoBox.setRewardCount(rewardPoints);
			rewardInfoBox.setToolTipText(rewardPoints  + " Reward Point" + (rewardPoints == 1 ? "" : "s"));
		}
	}

	public void removeRewardInfoBox()
	{
		infoBoxManager.removeInfoBox(rewardInfoBox);
		rewardInfoBox = null;
	}

	public void addFishInfoBox(String alternateText, String toolTipText)
	{
		infoBoxManager.removeInfoBox(fishInfoBox);
		fishInfoBox = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "harpoonfish.png"), this, -1);
		fishInfoBox.setAlternateText(alternateText);
		fishInfoBox.setToolTipText(toolTipText);
		infoBoxManager.addInfoBox(fishInfoBox);
	}

	public void removeFishInfoBox()
	{
		infoBoxManager.removeInfoBox(fishInfoBox);
		fishInfoBox = null;
	}

	public void addDamageInfoBox(String alternateText, String toolTipText)
	{
		infoBoxManager.removeInfoBox(damageInfoBox);
		damageInfoBox = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "damage.png"), this, -1);
		damageInfoBox.setAlternateText(alternateText);
		damageInfoBox.setToolTipText(toolTipText);
		infoBoxManager.addInfoBox(damageInfoBox);
	}

	public void removeDamageInfoBox()
	{
		infoBoxManager.removeInfoBox(damageInfoBox);
		damageInfoBox = null;
	}

	public void addPhaseInfoBox()
	{
		infoBoxManager.removeInfoBox(phaseInfoBox);
		phaseInfoBox = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "phases.png"), this, phase);
		phaseInfoBox.setToolTipText("Phase " + phase);
		infoBoxManager.addInfoBox(phaseInfoBox);
	}

	public void removePhaseInfoBox()
	{
		infoBoxManager.removeInfoBox(phaseInfoBox);
		phaseInfoBox = null;
	}

	public int findAmountById(Item[] inventory, int id)
	{
		return (int) Arrays.stream(inventory).filter(item -> item.getId() == id).count();
	}

	public void reset()
	{
		log.info("reset");
		removeFishInfoBox();
		removeDamageInfoBox();
		removePhaseInfoBox();
		npcs.clear();
		totemMap.clear();
		gameObjects.clear();
		waveIsIncoming = false;
		phase = 1;
	}

	public void setup()
	{
		if (config.damageIndicator())
		{
			addDamageInfoBox("0", "Damage: 0");
		}

		if (config.fishIndicator())
		{
			String toolTip = "Uncooked Fish: " + 0 + "</br>" + "Cooked Fish: " + 0;
			addFishInfoBox("0/0", toolTip);
		}

		if (config.phaseIndicator())
		{
			addPhaseInfoBox();
		}
	}
}
