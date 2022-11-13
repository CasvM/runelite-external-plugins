package com.raidtracker.io;

import com.google.inject.Inject;
import com.raidtracker.RaidTrackerConfig;
import com.raidtracker.RaidTrackerPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.util.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.raidtracker.RaidTrackerPlugin.PLUGIN_DIR;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class IOUtils
{
    @Inject
    Client client;
    
    @Inject
    private RaidTrackerConfig config;
    
    @Inject
    private RaidTrackerPlugin raidTrackerPlugin;
    
    private static final String[] raidTypes = {"Chambers of Xeric", "Theatre of Blood", "Tombs of Amascot"};
    public void ensurePath(String path)
    {
        try
        {
           Files.createDirectories(Paths.get(RUNELITE_DIR + File.separator + PLUGIN_DIR + File.separator + path));
        } catch (IOException e)
        {
            log.error(e.getLocalizedMessage());
        };
    };
    
    public static void mergeTwoDirectories(File dir1, File dir2)
    {
        String targetDirPath = dir1.getAbsolutePath();
        File[] files = dir2.listFiles();
        boolean delete = true;
        for (File file : files)
        {
            Path target = Paths.get(targetDirPath + File.separator + file.getName());
            if (!Files.exists(target))
            {
                file.renameTo(new File(String.valueOf(target)));
            } else
            {
                delete = false;
                log.info(String.format("Unable to move file %s a file with that name already exists in target directory", file.getName()));
            };
        };
        
        if (delete)
        {
            try
            {
                Files.deleteIfExists(dir2.toPath());
                log.info("Deleted old raid-data-tracker directory");
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            };
        };
    };
    
    public void checkUsernames(String oldValue, String newValue)
    {
        Path oldPath = Paths.get(String.format("%s%s%s%s%s", RUNELITE_DIR, File.separator, PLUGIN_DIR,File.separator, oldValue));
        Path newPath = Paths.get(String.format("%s%s%s%s%s", RUNELITE_DIR, File.separator, PLUGIN_DIR,File.separator, newValue));
        if (oldValue.equalsIgnoreCase(newValue) || !Files.exists(oldPath) || oldValue.equalsIgnoreCase(""))
        {
            ensurePath(newValue);
            config.setlastusername(client.getLocalPlayer().getName());
            return;
        };
        if (Files.exists(newPath))
        {
            File oldFolder =  new File(String.valueOf(oldPath));
            File newFolder =  new File(String.valueOf(newPath));
            mergeTwoDirectories(newFolder,oldFolder);
        } else
        {
            oldPath.toFile().renameTo(newPath.toFile());
        }
        config.setlastusername(client.getLocalPlayer().getName());
    };
    
    public String generatePath(int i)
    {
        String s = File.separator;
        RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
        return String.format("%s%s%s%s%s%s%s%s.json",
                RUNELITE_DIR.getAbsolutePath(),
                s,
                PLUGIN_DIR,
                s,
                client.getLocalPlayer().getName(),
                s,
                raidTypes[i],
                profileType.equals(RuneScapeProfileType.STANDARD) ? "" : " - " + Text.titleCase(RuneScapeProfileType.getCurrent(client))
        );
    };
};
