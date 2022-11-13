package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.io.FileReadWriter;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.vars.AccountType;
import net.runelite.client.config.RuneScapeProfile;
import net.runelite.client.config.RuneScapeProfileType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileWriterTest
{

    @Inject FileReadWriter fw;
    @Mock @Bind  Client client;
    @Mock @Bind  RaidTrackerConfig config;
    @Before
    public void before()
    {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void folderCreation()
    {
        //noinspection deprecation
        when(client.getLocalPlayer()).thenReturn(mock(Player.class));
        when(client.getLocalPlayer().getName()).thenReturn("Canvasba");
        when(client.getWorldType()).thenReturn(EnumSet.allOf(WorldType.class));
    }
}
