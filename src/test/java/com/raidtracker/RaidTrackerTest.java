package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.data.RaidTracker;
import com.raidtracker.io.FileReadWriter;
import com.raidtracker.data.RaidState;
import com.raidtracker.utils.RaidStateTracker;
import junit.framework.TestCase;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigClient;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.http.api.item.ItemPrice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RaidTrackerTest extends TestCase
{
	@Mock
	@Bind
	Client client;

	@Mock
	@Bind
	EventBus eventBus;

	@Mock
	@Bind
	ScheduledExecutorService executor;

	@Mock
	@Bind
	RuneLiteConfig runeliteConfig;

	@Bind
	@Named("sessionfile")
	File sessionfile = RuneLite.DEFAULT_SESSION_FILE;
	
	@Mock
	@Bind
	ConfigClient configClient;

	@Mock
	@Bind
	ConfigManager manager;

	@Mock
	@Bind
	private ItemManager itemManager;

	@Mock
	@Bind
	private RaidTrackerConfig config;

	@Inject
	RaidTrackerPlugin RaidTrackerPlugin;

	@Mock
	@Bind
	RaidTracker raidTracker;

	@Mock
	@Bind
	RaidStateTracker RaidStateTracker;
	
	@Inject FileReadWriter fw;
	
	
	@SuppressWarnings("deprecation")
	@Before
	public void before()
	{
		MockitoAnnotations.initMocks(this);
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
		RaidTrackerPlugin.startUp();
		when(RaidStateTracker.isInRaid()).thenReturn(true);
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		//noinspection deprecation
		when(client.getUsername()).thenReturn("Canvasba");
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 0));
		
		when(client.getLocalPlayer()).thenReturn(mock(Player.class));
		when(client.getLocalPlayer().getName()).thenReturn("Canvasba");
		when(client.getUsername()).thenReturn("Canvasba");
		when(client.getWorldType()).thenReturn(EnumSet.allOf(WorldType.class));
		when(config.lastusername()).thenReturn("Canvasba");
		fw.updateUsername("Canvasba");
	}
	
	@After
	public void after()
	{
		RaidTrackerPlugin.shutDown();
	}
	@Test
	public void TestRaidComplete()
	{
		RaidTracker raidTracker = new RaidTracker();
		ChatMessage message  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "Your completed Chambers of Xeric Challenge Mode count is: 357", "", 0);
		RaidTrackerPlugin.checkChatMessage(message, raidTracker);
		assertTrue(raidTracker.isRaidComplete());
	}


	@Test
	public void ChambersTest()
	{
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Canvasba");
		when(client.getVarbitValue(Varbits.TOTAL_POINTS)).thenReturn(50000);
		when(client.getVarbitValue(Varbits.PERSONAL_POINTS)).thenReturn(1000000);
		RaidTracker raidTracker = new RaidTracker();
		List<ItemPrice> ItemList = new ArrayList<>();
		ItemPrice KodaiItem = new ItemPrice();
		KodaiItem.setId(0);
		KodaiItem.setName("Kodai insignia");
		KodaiItem.setPrice(505050);

		ItemPrice TbowItem = new ItemPrice();
		TbowItem.setId(1);
		TbowItem.setName("Twisted Bow");
		TbowItem.setPrice(999999);

		ItemList.add(KodaiItem);
		ItemList.add(TbowItem);
		RaidTrackerPlugin.setFw(fw);
		raidTracker.setInRaidType(0);
		//raidTracker.setRaidComplete(true);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "<col=ef20ff>Congratulations - your raid is complete!</col><br>Team size: <col=ff0000>4 players</col> Duration:</col> <col=ff0000>28:33</col> Personal best: </col><col=ff0000>22:50</col>", "", 0), raidTracker);


		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 0));
		when(itemManager.search(anyString()))
			.thenAnswer(invocation -> ItemList.stream().filter(e -> e.getName().equalsIgnoreCase(invocation.getArgument(0, String.class))).collect(Collectors.toList()));
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "Your completed Chambers of Xeric Challenge Mode count is: 57.", "", 0), raidTracker);

		raidTracker.setTeamSize(5);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Player 1 - Kodai insignia", "", 0),raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Player 2 - Twisted Bow", "", 0),raidTracker);

	}
	@Test
	public void TobTest()
	{
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Canvasba");
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setRaidComplete(true);
		List<ItemPrice> tobList = new ArrayList<>();
		ItemPrice AvernicItem = new ItemPrice();
		AvernicItem.setId(0);
		AvernicItem.setName("Avernic defender hilt");
		AvernicItem.setPrice(505050);

		ItemPrice ScytheItem = new ItemPrice();
		ScytheItem.setId(1);
		ScytheItem.setName("Scythe of vitur (uncharged)");
		ScytheItem.setPrice(999999);

		tobList.add(AvernicItem);
		tobList.add(ScytheItem);

		RaidTrackerPlugin.setFw(fw);
		raidTracker.setInRaidType(1);
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 1));
		when(itemManager.search(anyString()))
			.thenAnswer(invocation -> tobList.stream().filter(e -> e.getName().equalsIgnoreCase(invocation.getArgument(0, String.class))).collect(Collectors.toList()));
		raidTracker.setTeamSize(5);

		RaidTrackerPlugin.checkChatMessage( new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Lil\\u0027 Zik", "", 0), raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Avernic defender hilt", "", 0),raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Scythe of vitur (uncharged)", "", 0),raidTracker);
		fw.writeToFile(raidTracker);
	}
	@Test
	public void ToaTest()
	{
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("Canvasba");
		RaidTracker raidTracker = new RaidTracker();
		raidTracker.setRaidComplete(true);
		List<ItemPrice> toalist = new ArrayList<>();
		ItemPrice fangItem = new ItemPrice();
		fangItem.setId(0);
		fangItem.setName("Osmumtuns Fang");
		fangItem.setPrice(505050);

		ItemPrice staffItem = new ItemPrice();
		staffItem.setId(1);
		staffItem.setName("Tumekens Shadow");
		staffItem.setPrice(999999);

		toalist.add(staffItem);
		toalist.add(fangItem);
		RaidTrackerPlugin.setFw(fw);
		raidTracker.setInRaidType(2);
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(false, 2));
		when(itemManager.search(anyString()))
				.thenAnswer(invocation -> toalist.stream().filter(e -> e.getName().equalsIgnoreCase(invocation.getArgument(0, String.class))).collect(Collectors.toList()));
		raidTracker.setTeamSize(5);
		RaidTrackerPlugin.checkChatMessage( new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Tumeken\\u0027s guardian", "", 0), raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Tumekens Shadow", "", 0),raidTracker);
		RaidTrackerPlugin.checkChatMessage(new ChatMessage(null, ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Canvasba found something special: Osmumtuns Fang", "", 0),raidTracker);

	}
	
	@Test
	public void TestToaTimes()
	{
		RaidTracker raidTracker = new RaidTracker();
		when(RaidStateTracker.getCurrentState()).thenReturn(new RaidState(true, 2));
		String[] toaRooms = {
				"Path of Crondis", "Zebak", "Path of Apmeken", "Ba-Ba", "Path of Het", "Akkha", "Path of Scabaras", "Kephri", "The Wardens"
		};
		int index = 0;
		for (String room : toaRooms)
		{
			int seconds = new Random().nextInt(1000);
			String timeString = seconds / 60 + ":" + (seconds % 60 < 10 ? "0" : "") + seconds % 60;
			String s = "Challenge complete: ";
			s+= room + " ";
			s+= "Duration: ";
			s+= timeString;
			ChatMessage message  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", s, "", 0);

			RaidTrackerPlugin.checkChatMessage(message, raidTracker);
			assertEquals(seconds, raidTracker.getRoomTimes()[index]);
			index ++;
		}
		// full raid.
		String message = "Challenge complete: The Wardens. Duration: <col=ef1020>3:53</col><br>Tombs of Amascut: Entry Mode challenge completion time: <col=ef1020>17:22</col>. Personal best: 16:40";
		ChatMessage m  = new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", message, "", 0);
		RaidTrackerPlugin.checkChatMessage(m, raidTracker);
	}
}