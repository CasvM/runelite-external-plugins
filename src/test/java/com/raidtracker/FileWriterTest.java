package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.io.FileReadWriter;
import net.runelite.api.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileWriterTest
{
    @Mock @Bind  Client client;
    @Inject FileReadWriter fw;
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
        when(client.getUsername()).thenReturn("Canvasba");
        fw.createFolders();
    }
}
