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
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
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

	@Getter
	private final Map<GameObject, DrawObject> gameObjects = new HashMap<>();

	@Getter
	private final Map<NPC, Instant> npcs = new HashMap<>();

	private final Map<GameObject, DrawObject> totemMap = new HashMap<>();

	private TemporossInfoBox infoBox;
	private TemporossInfoBox fishToCook;

	boolean waveIsIncoming;

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

		int region = client.getLocalPlayer().getWorldLocation().getRegionID();

		if (region != TEMPOROSS_REGION)
		{
			npcs.clear();
			gameObjects.clear();
			totemMap.clear();
			removeFishToCookInfoBox();
		}

	if (region == UNKAH_BOAT_REGION || region == UNKAH_REWARD_POOL_REGION)
		{
			if (infoBox == null)
			{
				infoBox = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "tome_of_water.png"), this, client.getVarbitValue(11936));
				infoBoxManager.addInfoBox(infoBox);
			}

			updateRewardInfoBox();

		}
		else
		{
			removeRewardInfoBox();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		updateRewardInfoBox();

		//11895 > 0 means tethered, 0 untethered. The exact value depends on what totem you're tethered to
		if (client.getVarbitValue(11895) > 0 && waveIsIncoming)
		{
			updateTotemTimers(config.tetheredColor());
		}
		else if (client.getVarbitValue(11895) == 0 && waveIsIncoming)
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
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		//updating every game tick so that the value stays correct even though items aren't changing in the inventory
		updateFishToCook();
	}

	public void updateFishToCook()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

		if (inventory == null || !config.cookIndicator())
		{
			return;
		}

		int uncookedFish = findAmountById(inventory.getItems(), 25564);
		int cookedFish = findAmountById(inventory.getItems(), 25565);
		int crystalFish = findAmountById(inventory.getItems(), 25566);

		int guaranteedDamage = crystalFish * 10 + cookedFish * 15 + uncookedFish * 10;

		if (fishToCook == null && guaranteedDamage == 0)
		{
			//return if there are no fish and there isn't already a round going
			return;
		}

		if (fishToCook == null)
		{
			addFishInfoBox("");
		}
		int bossHealth = 270;

		Widget energyWidget = client.getWidget(437, 37);
		Widget stormWidget = client.getWidget(437, 57);

		if (energyWidget != null && stormWidget != null)
		{
			//check for storm to be higher than 0 so that the infobox doesn't react in between phases
			if (Integer.parseInt(stormWidget.getText().split(": ")[1].split("%")[0]) > 0)
			{
				bossHealth = (int) Math.round(270 * Integer.parseInt(energyWidget.getText().split(": ")[1].split("%")[0]) / 100.0);
			}
		}

		int amountToFish = Math.max((int) Math.ceil((bossHealth - (guaranteedDamage + 5 * uncookedFish)) / 15.0), 0);

		int amountToCook = amountToFish == 0 ? (int) Math.ceil((bossHealth - guaranteedDamage) / 5.0) : amountToFish + uncookedFish;

		amountToCook = Math.max(amountToCook, 0);

		//change the infobox image when it first becomes possible to kill the boss
		if ((guaranteedDamage + uncookedFish * 5) > bossHealth && fishToCook.getRewardCount() < 0)
		{
			removeFishToCookInfoBox();
			fishToCook = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "harpoonfish.png"), this, amountToCook);
			fishToCook.setAlternateText(amountToFish + "/" + amountToCook);
			infoBoxManager.addInfoBox(fishToCook);
		}
		else if ((guaranteedDamage + uncookedFish * 5 < bossHealth) && fishToCook.getRewardCount() >= 0)
		{
			//edge case, some fish is dropped, or lost by a wave, so return to the not possible image
			addFishInfoBox(amountToFish + "/" + amountToCook);
		}
		else
		{
			fishToCook.setAlternateText(amountToFish + "/" + amountToCook);
		}

		String toolTip =
			"Minimum amount of fish to catch: " +
			amountToFish +
			"</br>" +
			"Minimum amount of fish to cook: " +
			amountToCook;

		fishToCook.setToolTipText(toolTip);
	}

	public void addTotemTimers()
	{
		if (config.useWaveTimer())
		{
			totemMap.forEach((object, drawObject) ->
			{
				drawObject.setStartTime(Instant.now());
				if (client.getVarbitValue(11895) > 0)
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

	public void updateRewardInfoBox()
	{
		if (infoBox != null)
		{
			int rewardPoints = client.getVarbitValue(11936);
			infoBox.setRewardCount(rewardPoints);
			infoBox.setToolTipText(rewardPoints  + " Reward Point" + (rewardPoints == 1 ? "" : "s"));
		}
	}

	public void removeRewardInfoBox()
	{
		infoBoxManager.removeInfoBox(infoBox);
		infoBox = null;
	}

	public void addFishInfoBox(String alternateText)
	{
		infoBoxManager.removeInfoBox(fishToCook);
		fishToCook = new TemporossInfoBox(ImageUtil.loadImageResource(getClass(), "not_possible.png"), this, -1);
		infoBoxManager.addInfoBox(fishToCook);
	}

	public void removeFishToCookInfoBox()
	{
		infoBoxManager.removeInfoBox(fishToCook);
		fishToCook = null;
	}

	public int findAmountById(Item[] inventory, int id)
	{
		return (int) Arrays.stream(inventory).filter(item -> item.getId() == id).count();
	}

}
