package com.tempoross;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.NullObjectID;
import net.runelite.api.ObjectID;
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
import net.runelite.client.game.ItemManager;
import net.runelite.client.Notifier;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@PluginDescriptor(
	name = "Tempoross",
	description = "Useful information and tracking for the Tempoross skilling boss"
)
public class TemporossPlugin extends Plugin
{
	private static final String WAVE_INCOMING_MESSAGE = "a colossal wave closes in...";
	private static final String WAVE_END_SAFE = "as the wave washes over you";
	private static final String WAVE_END_DANGEROUS = "the wave slams into you";
	private static final String TEMPOROSS_VULNERABLE_MESSAGE = "tempoross is vulnerable";

	private static final int VARB_IS_TETHERED = 11895;
	private static final int VARB_REWARD_POOL_NUMBER = 11936;

	private static final int TEMPOROSS_REGION = 12078;
	private static final int UNKAH_REWARD_POOL_REGION = 12588;
	private static final int UNKAH_BOAT_REGION = 12332;

	private static final int DAMAGE_PER_UNCOOKED = 10;
	private static final int DAMAGE_PER_COOKED = 15;
	private static final int DAMAGE_PER_CRYSTAL = 10;

	private static final int REWARD_POOL_IMAGE_ID = ItemID.TOME_OF_WATER;
	private static final int DAMAGE_IMAGE_ID = ItemID.DRAGON_HARPOON;
	private static final int FISH_IMAGE_ID = ItemID.HARPOONFISH;
	private static final BufferedImage PHASE_IMAGE = ImageUtil.loadImageResource(TemporossPlugin.class, "phases.png");

	private static final int FIRE_ID = 37582;

	private static final int FIRE_SPREAD_MILLIS = 24000;
	private static final int FIRE_SPAWN_MILLIS = 9600;
	private static final int FIRE_SPREADING_SPAWN_MILLIS = 1200;
	private static final int WAVE_IMPACT_MILLIS = 7800;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private TemporossConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private TemporossOverlay temporossOverlay;

	private final Set<Integer> TEMPOROSS_GAMEOBJECTS = ImmutableSet.of(
		FIRE_ID, NullObjectID.NULL_41006, NullObjectID.NULL_41007, NullObjectID.NULL_41352,
		NullObjectID.NULL_41353, NullObjectID.NULL_41354, NullObjectID.NULL_41355, ObjectID.DAMAGED_MAST_40996,
		ObjectID.DAMAGED_MAST_40997, ObjectID.DAMAGED_TOTEM_POLE, ObjectID.DAMAGED_TOTEM_POLE_41011);

	//Jagex changed the fire from 41005 (in objectID) to 37582 (not in ObjectID or nullobjectID),
	//that's why int instead of an objectid is used.

	//41006 = shadow before fire is burning
	//41007 = shadow just before fire is jumping over to a next spot
	//41354/41355 = a totem to grapple on to
	//41352/41353 = a mast to grapple on to
	//41010/41011 = a totem that is broken
	//40996/40997 = a broken mast

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
	private boolean nearRewardPool;

	private int previousRegion;

	private int phase = 1;

	@Provides
	TemporossConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TemporossConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(temporossOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(temporossOverlay);
		reset();
		removeRewardInfoBox();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		if (!TEMPOROSS_GAMEOBJECTS.contains(gameObjectSpawned.getGameObject().getId()))
		{
			return;
		}

		int duration;
		switch (gameObjectSpawned.getGameObject().getId())
		{
			case FIRE_ID:
				duration = FIRE_SPREAD_MILLIS;
				break;
			case NullObjectID.NULL_41006:
				duration = FIRE_SPAWN_MILLIS;
				break;
			case NullObjectID.NULL_41007:
				duration = FIRE_SPREADING_SPAWN_MILLIS;
				break;
			default:
				//if it is not one of the three above, it is a totem/mast and should be added to the totem map, with 7800ms duration
				if (config.useWaveTimer())
				{
					totemMap.put(gameObjectSpawned.getGameObject(),
						new DrawObject(gameObjectSpawned.getTile(), gameObjectSpawned.getGameObject(),
							Instant.now(), WAVE_IMPACT_MILLIS, config.waveTimerColor()));
				}
				return;
		}

		if (config.highlightFires())
		{
			gameObjects.put(gameObjectSpawned.getGameObject(), new DrawObject(gameObjectSpawned.getTile(), gameObjectSpawned.getGameObject(), Instant.now(), duration, config.fireColor()));
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		gameObjects.remove(gameObjectDespawned.getGameObject());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (NpcID.FISHING_SPOT_10569 == npcSpawned.getNpc().getId())
		{
			if (config.highlightDoubleSpot())
			{
				npcs.put(npcSpawned.getNpc(), Instant.now());
			}

			if (config.doubleSpotNotification())
			{
				notifier.notify("A double Harpoonfish spot has appeared.");
			}
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

		int region = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();

		if (region != TEMPOROSS_REGION && previousRegion == TEMPOROSS_REGION)
		{
			reset();
		}
		else if (region == TEMPOROSS_REGION && previousRegion != TEMPOROSS_REGION)
		{
			setup();
		}

		nearRewardPool = (region == UNKAH_BOAT_REGION || region == UNKAH_REWARD_POOL_REGION);

		if (nearRewardPool)
		{
			addRewardInfoBox();
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
		if (nearRewardPool)
		{
			addRewardInfoBox();
		}

		// The varb is a bitfield that refers to what totem/mast the player is tethered to,
		// with each bit corresponding to a different object.
		if (waveIsIncoming && config.useWaveTimer())
		{
			addTotemTimers(false);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = chatMessage.getMessage().toLowerCase();
		if (message.contains(WAVE_INCOMING_MESSAGE))
		{
			waveIsIncoming = true;
			if (config.useWaveTimer())
			{
				addTotemTimers(true);
			}
		}
		else if (message.contains(WAVE_END_SAFE) || message.contains(WAVE_END_DANGEROUS))
		{
			waveIsIncoming = false;
			removeTotemTimers();
		}
		else if (message.contains(TEMPOROSS_VULNERABLE_MESSAGE) && config.phaseIndicator())
		{
			addPhaseInfoBox(++phase);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId() ||
			(!config.fishIndicator() && !config.damageIndicator()) ||
			(fishInfoBox == null && damageInfoBox == null))
		{
			return;
		}

		ItemContainer inventory = event.getItemContainer();

		int uncookedFish = inventory.count(ItemID.RAW_HARPOONFISH);
		int cookedFish = inventory.count(ItemID.HARPOONFISH);
		int crystalFish = inventory.count(ItemID.CRYSTALLISED_HARPOONFISH);

		if (config.fishIndicator())
		{
			addFishInfoBox(
				(uncookedFish + crystalFish) + "/" + cookedFish,
				"Uncooked Fish: " + (uncookedFish + crystalFish) + "</br>Cooked Fish: " + cookedFish
			);
		}

		if (config.damageIndicator())
		{
			int damage = uncookedFish * DAMAGE_PER_UNCOOKED
				+ cookedFish * DAMAGE_PER_COOKED
				+ crystalFish * DAMAGE_PER_CRYSTAL;

			addDamageInfoBox(damage);
		}
	}

	public void addTotemTimers(boolean setStart)
	{
		Color color = client.getVarbitValue(VARB_IS_TETHERED) > 0 ? config.tetheredColor() : config.waveTimerColor();
		totemMap.forEach((object, drawObject) ->
		{
			if (setStart)
			{
				drawObject.setStartTime(Instant.now());
			}
			drawObject.setColor(color);
			gameObjects.put(object, drawObject);
		});
	}

	public void removeTotemTimers()
	{
		totemMap.forEach(gameObjects::remove);
	}

	private TemporossInfoBox createInfobox(BufferedImage image, String text, String tooltip)
	{
		TemporossInfoBox infoBox = new TemporossInfoBox(image, this);
		infoBox.setText(text);
		infoBox.setTooltip(tooltip);
		return infoBox;
	}

	public void addRewardInfoBox()
	{
		int rewardPoints = client.getVarbitValue(VARB_REWARD_POOL_NUMBER);
		infoBoxManager.removeInfoBox(rewardInfoBox);
		rewardInfoBox = createInfobox(itemManager.getImage(REWARD_POOL_IMAGE_ID),
			Integer.toString(rewardPoints),
			rewardPoints + " Reward Point" + (rewardPoints == 1 ? "" : "s"));

		infoBoxManager.addInfoBox(rewardInfoBox);
	}

	public void removeRewardInfoBox()
	{
		infoBoxManager.removeInfoBox(rewardInfoBox);
		rewardInfoBox = null;
	}

	public void addFishInfoBox(String text, String tooltip)
	{
		infoBoxManager.removeInfoBox(fishInfoBox);
		fishInfoBox = createInfobox(itemManager.getImage(FISH_IMAGE_ID), text, tooltip);
		infoBoxManager.addInfoBox(fishInfoBox);
	}

	public void removeFishInfoBox()
	{
		infoBoxManager.removeInfoBox(fishInfoBox);
		fishInfoBox = null;
	}

	public void addDamageInfoBox(int damage)
	{
		infoBoxManager.removeInfoBox(damageInfoBox);
		damageInfoBox = createInfobox(itemManager.getImage(DAMAGE_IMAGE_ID), Integer.toString(damage), "Damage: " + damage);
		infoBoxManager.addInfoBox(damageInfoBox);
	}

	public void removeDamageInfoBox()
	{
		infoBoxManager.removeInfoBox(damageInfoBox);
		damageInfoBox = null;
	}

	public void addPhaseInfoBox(int phase)
	{
		infoBoxManager.removeInfoBox(phaseInfoBox);
		phaseInfoBox = createInfobox(PHASE_IMAGE, Integer.toString(phase), "Phase " + phase);
		infoBoxManager.addInfoBox(phaseInfoBox);
	}

	public void removePhaseInfoBox()
	{
		infoBoxManager.removeInfoBox(phaseInfoBox);
		phaseInfoBox = null;
	}

	public void reset()
	{
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
			addDamageInfoBox(0);
		}

		if (config.fishIndicator())
		{
			addFishInfoBox("0/0", "Uncooked Fish: " + 0 + "</br>" + "Cooked Fish: " + 0);
		}

		if (config.phaseIndicator())
		{
			addPhaseInfoBox(phase);
		}
	}
}
