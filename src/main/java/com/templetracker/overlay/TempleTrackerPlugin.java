package com.templetracker.overlay;

import com.google.inject.Provides;
import com.templetracker.FileReadWriter;
import com.templetracker.constructors.Companion;
import com.templetracker.constructors.Encounter;
import com.templetracker.constructors.EncounterName;
import com.templetracker.constructors.StartLocation;
import com.templetracker.constructors.TempleTracker;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;

@Slf4j
@PluginDescriptor(
	name = "Temple Tracker",
	description = "Adds an overlay to the screen showing treks/hour, duration of each trek and average points"
)
public class TempleTrackerPlugin extends Plugin
{
	@Getter
	TempleTracker tracker = new TempleTracker();

	@Inject
	private Client client;

	@Inject
	private TempleTrackerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TempleTrackerOverlayPanel overlayPanel;

	@Inject TreksPerHourOverlayPanel treksPerHourOverlayPanel;

	private final FileReadWriter fw = new FileReadWriter();

	private int previousRegion = 0;

	@Override
	protected void startUp()
	{
	}

	@Override
	protected void shutDown()
	{
		removePanels();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		if (tracker.getRoute() > 0) {
			return;
		}
		//menu option ID's for the different temple trekking routes. 1 is the easy route, 3 is the hard route.
		final int ROUTE_1 = 21561365;
		final int ROUTE_2 = 21561369;
		final int ROUTE_3 = 21561373;

		switch (menuOptionClicked.getWidgetId()) {
			case ROUTE_1:
				tracker.setRoute(1);
				break;
			case ROUTE_2:
				tracker.setRoute(2);
				break;
			case ROUTE_3:
				tracker.setRoute(3);
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned) {
		if (!tracker.isTempleTrekking()) {
			return;
		}

		final int BOG = 13838;
		final int BRIDGE = 13834;

		switch(groundObjectSpawned.getGroundObject().getId())
		{
			case BOG:
				newEncounter(EncounterName.BOG);
				break;
			case BRIDGE:
				newEncounter(EncounterName.BRIDGE);
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned) {
		if (!tracker.isTempleTrekking() || (tracker.getLatestEncounter() != null && tracker.getLatestEncounter().getName() != null)) {
			return;
		}

		final int RIVER_SWING = 13847;
		final int KRAKEN_BOAT = 13864;

		switch (gameObjectSpawned.getGameObject().getId()) {
			case RIVER_SWING:
				newEncounter(EncounterName.RIVER);
				break;
			case KRAKEN_BOAT:
				newEncounter(EncounterName.TENTACLES);
			default:
				break;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned) {
		if (!tracker.isTempleTrekking()
			|| npcSpawned == null || npcSpawned.getNpc() == null || npcSpawned.getNpc().getName() == null
			|| (tracker.getLatestEncounter() != null && tracker.getLatestEncounter().getName() != null))
		{
			return;
		}

		if (tracker.getCompanion() == null && Companion.getCompanion(npcSpawned.getNpc().getName()) != null) {
			tracker.setCompanion(Companion.getCompanion(npcSpawned.getNpc().getName()));
		}

		final String GHAST_NAME = "Ghast";
		final String SHADE_NAME = "Shade";
		final String SHADE_RIYL_NAME = "Riyl shadow";
		final String SHADE_ASYN_NAME = "Asyn shadow";
		final String NAIL_BEAST_NAME = "Nail beast";
		final String SNAIL_NAME = "Giant snail";
		final String SNAKE_NAME = "Swamp snake";
		final String VAMPYRE_NAME = "Vampyre Juvinate";
		final String ABIDOR_NAME = "Abidor Crank";

		switch (npcSpawned.getNpc().getName()) {
			case GHAST_NAME:
				newEncounter(EncounterName.GHASTS);
				break;
			case SHADE_NAME:
			case SHADE_ASYN_NAME:
			case SHADE_RIYL_NAME:
				newEncounter(EncounterName.SHADES);
				break;
			case NAIL_BEAST_NAME:
				newEncounter(EncounterName.NAIL_BEASTS);
				break;
			case SNAIL_NAME:
				newEncounter(EncounterName.SNAILS);
				break;
			case SNAKE_NAME:
				newEncounter(EncounterName.SNAKES);
				break;
			case VAMPYRE_NAME:
				newEncounter(EncounterName.VAMPYRES);
				break;
			case ABIDOR_NAME:
				newEncounter(EncounterName.ABIDOR);
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged) {
		int points = client.getVarbitValue(1955);
		int startCheckValue = client.getVarbitValue(1956);
		int encounter = client.getVarbitValue(1958);
		int escaping = client.getVarbitValue(1954);

		if (points != tracker.getPoints()) {
			if (points > tracker.getPoints() && tracker.isTempleTrekking()) {
				tracker.setPoints(points);
			}
		}

		//1956 0 -> 1 is starting trek
		if (startCheckValue == 1 && tracker.getCheck() == 0) {
			tracker.setCheck(1);
		}

		//1958 -> number = new encounter
		if (encounter != 0 && tracker.getLatestEncounter() != null) {
			tracker.getLatestEncounter().setLocation(encounter);
		}

		//1958 -> 0 = end of the encounter
		else if (encounter == 0 && tracker.getLatestEncounter() != null) {
			tracker.getLatestEncounter().setEndTime(System.currentTimeMillis());
			tracker.addEncounterToList();
		}

		//1954 1 -> 0 is tping or escaping
		if (escaping == 0 && tracker.isTempleTrekking()) {
			templeTrekkingEnded();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGING_IN)
		{
			fw.updateUsername(client.getUsername());
		}

		if (client.getLocalPlayer() == null) {
			return;
		}

		//player is returning to burgh de rott or paterdomus at the start, or finished the trek
		if (tracker.isTempleTrekking() && (client.getLocalPlayer().getWorldLocation().getRegionID() == StartLocation.BURGH_DE_ROTT.getRegionID()
			|| client.getLocalPlayer().getWorldLocation().getRegionID() == StartLocation.PATERDOMUS.getRegionID()))
		{
			if (tracker.getEndTime() < 0 && tracker.getStartLocation() != null && client.getLocalPlayer().getWorldLocation().getRegionID() != tracker.getStartLocation().getRegionID())
			{
				tracker.setEndTime(System.currentTimeMillis());
			}
			templeTrekkingEnded();
		}
		//game state is changed just before location is changed, so use this bodge to set temple trekking true
		if (!tracker.isTempleTrekking() && tracker.getCheck() == 1 &&
			(client.getLocalPlayer().getWorldLocation().getRegionID() == StartLocation.BURGH_DE_ROTT.getRegionID()
			|| client.getLocalPlayer().getWorldLocation().getRegionID() == StartLocation.PATERDOMUS.getRegionID()))
		{
			templeTrekkingStarted();
		}

		int location = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation()).getRegionID();
		if (tracker.getEndTime() > 0
			&& location != StartLocation.BURGH_DE_ROTT.getRegionID()
			&& location != StartLocation.PATERDOMUS.getRegionID()
			&& location != 7769 //home
			&& previousRegion != 7769 //region changes for a bit when tp'ing away from home
			&& location != 13613) //elidinis statue
		{
			removePanels();
		}

		if (location != previousRegion) {
			previousRegion = location;
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked overlayMenuClicked)	{
		OverlayMenuEntry overlayMenuEntry = overlayMenuClicked.getEntry();
		if (overlayMenuEntry.getMenuAction() == MenuAction.RUNELITE_OVERLAY
			&& overlayMenuClicked.getEntry().getOption().equals(TreksPerHourOverlayPanel.TREKS_RESET)
			&& overlayMenuClicked.getOverlay() == treksPerHourOverlayPanel)
		{
			treksPerHourOverlayPanel.reset();
		}
	}

	public void newEncounter(EncounterName name) {
		if (tracker.getLatestEncounter() != null) {
			return;
		}
		Encounter tempEncounter = new Encounter();
		tempEncounter.setName(name);
		tempEncounter.setRoute(tracker.getRoute());

		tracker.setLatestEncounter(tempEncounter);
	}

	private void templeTrekkingStarted() {
		tracker = new TempleTracker();
		tracker.setTempleTrekking(true);

		if (client.getLocalPlayer() != null) {
			if (client.getLocalPlayer().getWorldLocation().getRegionID() == StartLocation.PATERDOMUS.getRegionID()) {
				tracker.setStartLocation(StartLocation.PATERDOMUS);
			}
			else if (client.getLocalPlayer().getWorldLocation().getRegionID() == StartLocation.BURGH_DE_ROTT.getRegionID()) {
				tracker.setStartLocation(StartLocation.BURGH_DE_ROTT);
			}
		}

		if (config.showOverlay())
		{
			addPanels();
		}
	}

	private void templeTrekkingEnded() {
		tracker.setCheck(0);
		tracker.setTempleTrekking(false);
		if (tracker.getEndTime() < 0) {
			overlayManager.remove(overlayPanel);
		}
		else {
			if (config.logData()) {
				fw.writeToFile(tracker);
			}
			treksPerHourOverlayPanel.addTrek(tracker);
		}

	}

	private void addPanels() {
		overlayManager.add(overlayPanel);
		overlayManager.add(treksPerHourOverlayPanel);
	}

	private void removePanels() {
		overlayManager.remove(overlayPanel);
		overlayManager.remove(treksPerHourOverlayPanel);
	}

	@Provides
	TempleTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TempleTrackerConfig.class);
	}
}
