package com.raidtracker;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.data.RaidTracker;
import com.raidtracker.data.RaidTrackerItem;
import com.raidtracker.utils.RaidStateTracker;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigClient;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

import com.raidtracker.io.IOUtils;
@RunWith(MockitoJUnitRunner.class)
public class GenericTests
{
    @Inject
    public Gson gson;
    
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
    
    @Bind
    @Named("config")
    File config = RuneLite.DEFAULT_CONFIG_FILE;
    
    
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
    private RaidTrackerConfig raidTrackerConfig;
    
    @Inject
    RaidTrackerPlugin RaidTrackerPlugin;
    @Inject
    IOUtils ioUtils;
    
    @Mock
    @Bind
    RaidStateTracker RaidStateTracker;
    @Before
    public void before()
    {
       
        Gson gson = new Gson();
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
        RaidTrackerPlugin.startUp();
        String message = "Challenge complete: The Wardens. Duration: <col=ef1020>3:53</col><br>Tombs of Amascut: Entry Mode challenge completion time: <col=ef1020>17:22</col>. Personal best: 16:40";
        System.out.println(message.split("<br>")[0]);
        System.out.println(message.split("<br>")[1]);
        
    }
    
    @After
    public void after()
    {
        RaidTrackerPlugin.shutDown();
    }
    @Test
    public void TestJsonToStringWithTracker()
    {
        RaidTracker raidTracker = new RaidTracker();
        ArrayList<RaidTrackerItem> lootList = new ArrayList<>();
        lootList.add(new RaidTrackerItem("item", 1, 1, 1));
        lootList.add(new RaidTrackerItem("item", 1, 1, 1));
        lootList.add(new RaidTrackerItem("item", 1, 1, 1));
        raidTracker.setLootList(lootList);
        System.out.println("here: " + gson.toJson(raidTracker.getLootList()));
    }
    
    @Test
    public void ioTests()
    {
        ioUtils.ensurePath("test/test/test");
    }
}
