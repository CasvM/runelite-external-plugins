package com.raidtracker.io;

import com.google.inject.Inject;
import com.raidtracker.RaidTrackerConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class IOUtils
{
    @Inject
    Client client;
    
    @Inject
    private RaidTrackerConfig config;
    public void ensurePath(String path)
    {
        try
        {
            Files.createDirectories(Paths.get(RUNELITE_DIR + File.separator + path));
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
        Path oldPath = Paths.get(String.format("%s%s%s", RUNELITE_DIR, File.separator, oldValue));
        Path newPath = Paths.get(String.format("%s%s%s", RUNELITE_DIR, File.separator, newValue));
        if (!Files.exists(oldPath))
        {
            ensurePath(newValue);
            return;
        };
        if (Files.exists(newPath))
        {
            File oldFolder =  new File(String.valueOf(oldPath));
            File newFolder =  new File(String.valueOf(newPath));
            mergeTwoDirectories(newFolder,oldFolder);
        };
    };
};
