package com.raidtracker;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.raidtracker.io.IOUtils;
import net.runelite.api.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IOUtilsTest
{
    @Mock @Bind  Client client;
    @Mock @Bind  RaidTrackerConfig raidTrackerConfig;
    @Inject
    IOUtils ioUtils;
    @Before
    public void before()
    {
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
    }
    
    @Test
    public void ChangeUsernameTest()
    {
        ioUtils.checkUsernames("old", "new");
    }
}
