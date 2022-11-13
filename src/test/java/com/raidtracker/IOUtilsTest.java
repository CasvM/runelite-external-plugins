package com.raidtracker;

import com.google.common.base.CharMatcher;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.io.FileReadWriter;
import com.raidtracker.io.IOUtils;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumSet;

import static net.runelite.client.util.Text.toJagexName;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IOUtilsTest
{
    @Mock @Bind  Client client;
    @Mock @Bind  RaidTrackerPlugin raidTrackerPlugin;

    @Mock @Bind  ItemManager itemManager;
    @Mock @Bind  ConfigManager manager;
    @Mock @Bind  RaidTrackerConfig config;
    @Inject IOUtils ioUtils;
    @Inject FileReadWriter fileReadWriter;
    @Before
    public void before()
    {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
        when(client.getLocalPlayer()).thenReturn(mock(Player.class));
        when(client.getLocalPlayer().getName()).thenReturn("Canvasba");
        when(client.getUsername()).thenReturn("Canvasba");
        when(client.getWorldType()).thenReturn(EnumSet.allOf(WorldType.class));
    };
    
    @Test
    public void pathTest(){
        when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
        System.out.println(ioUtils.generatePath(1));
        when(client.getWorldType()).thenReturn(EnumSet.allOf(WorldType.class));
        System.out.println(ioUtils.generatePath(1));
        
    };
    @Test
    public void fileCreation()
    {
        System.out.println("Testing file creation");
        when(config.lastusername()).thenReturn("old username");
        fileReadWriter.CheckOrCreate();
        when(config.lastusername()).thenReturn("Canvasba");
        fileReadWriter.CheckOrCreate();
        when(client.getLocalPlayer().getName()).thenReturn("Canvasba2");
        fileReadWriter.CheckOrCreate();
    };
}
