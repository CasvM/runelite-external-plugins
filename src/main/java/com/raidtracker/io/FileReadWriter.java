package com.raidtracker.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.raidtracker.RaidTrackerConfig;
import com.raidtracker.RaidTrackerPlugin;
import com.raidtracker.data.RaidTracker;
import com.raidtracker.data.OldRaidTracker;
import com.raidtracker.data.UniqueDrop;
import com.sun.jna.platform.FileUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.util.Text;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static net.runelite.client.RuneLite.RUNELITE_DIR;
import static net.runelite.client.util.Text.toJagexName;

@SuppressWarnings("deprecation")
@Slf4j
public class FileReadWriter {
    private static final ArrayList<String> usernames = new ArrayList<>();
    private static ConfigManager configManager;
    @Getter
    private String username = "Canvasba";
    public static final String PLUGIN_DIR = "raid-data-tracker";
    @Inject
    private Client client;
    @Inject
    private IOUtils ioUtils;
    
    @Inject
    private RaidTrackerConfig config;
    
    public void writeToFile(RaidTracker raidTracker)
    {
        CheckOrCreate();
        log.info("[RAID DATA TRACKER] [STARTING WRITER]");
        ArrayList<RaidTracker> saved = readFromFile(raidTracker.getInRaidType());
        boolean newrt = true;
        int index = 0;
        for (RaidTracker srt : saved)
        {
            if (srt.getKillCountID().equalsIgnoreCase(raidTracker.getKillCountID()))
            {
                newrt = false;
                saved.set(index, raidTracker);
            }
            index ++;
        }
        if (newrt)
        {
            saved.add(raidTracker);
        }
        updateRTList(saved, raidTracker.getInRaidType());
    }

    public String getJSONString(RaidTracker raidTracker, Gson gson, JsonParser parser)
    {
        return gson.toJson(raidTracker);
    }
    
    public ArrayList<RaidTracker> readFromFile(String alternateFile, int raidType)
    {
        try {
            Gson gson = new GsonBuilder().create();
            JsonParser parser = new JsonParser();
            String filePath = ioUtils.generatePath(raidType);
            File f = new File(filePath);
            if (!f.exists())
            {
                f.createNewFile();
            };
            
            BufferedReader bufferedreader = new BufferedReader(new FileReader(filePath));
            String line;

            ArrayList<RaidTracker> RTList = new ArrayList<>();
            while ((line = bufferedreader.readLine()) != null && line.length() > 0) {
                try {
                    RaidTracker parsed = gson.fromJson(parser.parse(line), RaidTracker.class);
                    RTList.add(parsed);
                }
                catch (JsonSyntaxException e) {
                    log.info("Bad line: " + line);
                }
            }

            bufferedreader.close();
            return RTList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<RaidTracker> readFromFile(int type) {
        return readFromFile("", type);

    }

    public void CheckOrCreate()
    {
        String lastusername = config.lastusername();
        if (!lastusername.equalsIgnoreCase(""))
        {
            ioUtils.checkUsernames(lastusername, client.getLocalPlayer().getName());
        } else
        {
            config.setlastusername(client.getLocalPlayer().getName());
            ioUtils.ensurePath(config.lastusername());
        };
    
        if (oldExists())
        {
            try
            {
                log.debug("[RAID DATA TRACKER] [IMPORTING DATA]");
                migrate();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    };

    public void updateUsername(final String username) {
        this.username = username;
        CheckOrCreate();
    }

    public void updateRTList(ArrayList<RaidTracker> RTList, int type) {
        try {
            Gson gson = new GsonBuilder().create();

            JsonParser parser = new JsonParser();
            FileWriter fw = new FileWriter(ioUtils.generatePath(type), false); //the true will append the new data
            for (RaidTracker RT : RTList)
            {
                gson.toJson(parser.parse(getJSONString(RT, gson, parser)), fw);
                fw.append("\n");
            }
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean delete(int type) {
        String filePath = ioUtils.generatePath(type);
        File newFile = new File(filePath);
        boolean isDeleted = newFile.delete();
        try
        {
            IGNORE_RESULT(newFile.createNewFile());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return isDeleted;
    }

    public void IGNORE_RESULT(boolean b) {}
    
    //importer
    public String getPath(String n)
    {
        return RUNELITE_DIR + File.separator + "raid-data tracker" + File.separator + toJagexName(n);
    }
    
    @SuppressWarnings("deprecation")
    public boolean oldExists()
    {
        //noinspection deprecation
        return (Files.isDirectory(Paths.get(getPath(client.getUsername()))));
    }
    
    public String getName(String s)
    {
        String profileKey = configManager == null ? "rsprofile._pM2FnYG" : configManager.getRSProfileKey();
        return (usernames.contains(toJagexName(s)) ? profileKey : toJagexName(s));
    }
    
    @SuppressWarnings("deprecation")
    public void migrate() throws IOException
    {
        @SuppressWarnings("deprecation") String basePath = getPath(client.getUsername());
        String[] dirs = {"cox", "tob"};
        for (String dir : dirs)
        {
            Path thisPath = Paths.get(basePath + File.separator + dir);
            if (Files.exists(thisPath) && Files.isDirectory(thisPath))
            {
                
                String filePath = basePath + File.separator + dir + File.separator + "raid_tracker_data.log";
                Path path = Paths.get(filePath);
                if (Files.exists(path))
                {
                    log.info(dir.toUpperCase() + " Exists, Reading File");
                    int start = (int) new Date().getTime();
                    Gson gson = new Gson();String line;
                    BufferedReader bufferedreader = new BufferedReader(new FileReader(filePath));
                    ArrayList<OldRaidTracker> oldList = new ArrayList<>();
                    ArrayList<RaidTracker> newList = new ArrayList<>();
                    while ((line = bufferedreader.readLine()) != null && line.length() > 0)
                    {
                        try {
                            oldList.add(gson.fromJson(line, OldRaidTracker.class));
                        }
                        catch (JsonSyntaxException e) {
                            log.info("Bad line: " + line);
                        }
                    }
                    int end = (int) new Date().getTime();
                    int diff = end - start;
                    log.info("Loaded " + oldList.size() + " Items in " + diff + "ms");
                    log.info("Starting Migration");
                    bufferedreader.close();
                    ArrayList<String> keys = new ArrayList<>();
                    for (OldRaidTracker oldRaidTracker : oldList)
                    {
                        String comparer = oldRaidTracker.getKillCountID();
                        if (!keys.contains(comparer)) keys.add(comparer);
                    }
                    log.info("Found " + keys.size() + " Unique Raids");
                    int startm = (int) new Date().getTime();
                    final int[] index = {-1};
                    for (String key : keys)
                    {
                        RaidTracker newTracker = new RaidTracker();
                        ArrayList<UniqueDrop> uniqueDrops = new ArrayList<>();
                        oldList.stream()
                                .filter(o -> o.getKillCountID().equalsIgnoreCase(key))
                                .forEach(oldTracker ->
                                {
                                    if (oldTracker.isSpecialLootInOwnName())
                                    {
                                        if (!usernames.contains(toJagexName(oldTracker.getSpecialLootReceiver())))
                                        {
                                            usernames.add(toJagexName(oldTracker.getSpecialLootReceiver()));
                                        }
                                    }
                                    // times.
                                    int[] times;
                                    newTracker.setChestOpened(newTracker.isChestOpened() || oldTracker.isChestOpened());
                                    newTracker.setRaidComplete(newTracker.isRaidComplete() || oldTracker.isRaidComplete());
                                    newTracker.setLoggedIn(newTracker.isLoggedIn() || oldTracker.isLoggedIn());
                                    newTracker.setChallengeMode(newTracker.isChallengeMode() || oldTracker.isChallengeMode());
                                    newTracker.setInRaid(true);
                                    newTracker.setKillCountID(oldTracker.getKillCountID());
                                    newTracker.setTeamSize(Math.max(oldTracker.getTeamSize(), newTracker.getTeamSize()));
                                    newTracker.setRaidTime(newTracker.getRaidTime() == -1 ? oldTracker.getRaidTime() : newTracker.getRaidTime());
                                    newTracker.setCompletionCount(newTracker.getCompletionCount() == -1 ? oldTracker.getCompletionCount() : newTracker.getCompletionCount());
                                    newTracker.setDate(oldTracker.getDate());
                                    newTracker.setMvp(newTracker.getMvp().equalsIgnoreCase("") ? oldTracker.getMvp() : newTracker.getMvp());
                                    newTracker.setMvpInOwnName(newTracker.isMvpInOwnName() || oldTracker.isMvpInOwnName());
                                    newTracker.setInRaidType(oldTracker.isInRaidChambers() ? 0 : 1);
                                    newTracker.setTotalPoints(newTracker.getTotalPoints() == -1 ? oldTracker.getTotalPoints() : newTracker.getTotalPoints());
                                    newTracker.setPersonalPoints((newTracker.getPersonalPoints() == -1 ? oldTracker.getPersonalPoints() : newTracker.getPersonalPoints()));
                                    newTracker.setPercentage(newTracker.getPercentage() == -1 ? oldTracker.getPercentage() : newTracker.getPercentage());
                                    if (newTracker.getLootList().size() < oldTracker.getLootList().size())
                                    {
                                        newTracker.setLootList(oldTracker.getLootList());
                                    }
                                    if (oldTracker.isInRaidChambers())
                                    {
                                        times = new int[]{
                                                oldTracker.getUpperTime(),
                                                oldTracker.getMiddleTime(),
                                                oldTracker.getLowerTime(),
                                                oldTracker.getShamansTime(),
                                                oldTracker.getVasaTime(),
                                                oldTracker.getVanguardsTime(),
                                                oldTracker.getMysticsTime(),
                                                oldTracker.getTektonTime(),
                                                oldTracker.getVespulaTime(),
                                                oldTracker.getIceDemonTime(),
                                                oldTracker.getThievingTime(),
                                                oldTracker.getTightropeTime(),
                                                oldTracker.getCrabsTime()
                                        };
                                    } else
                                    {
                                        times = new int[]{
                                                oldTracker.getMaidenTime(),
                                                oldTracker.getBloatTime(),
                                                oldTracker.getNyloTime(),
                                                oldTracker.getSotetsegTime(),
                                                oldTracker.getXarpusTime(),
                                                oldTracker.getVerzikTime()
                                        };
                                    }
                                    for (int i = 0; i < times.length; i++)
                                    {
                                        newTracker.roomTimes[i] = (times[i] > -1 && times[i] < newTracker.roomTimes[i])  || newTracker.roomTimes[i] == -1 ? times[i] : newTracker.roomTimes[i];
                                    }
                                    if (!oldTracker.getSpecialLoot().equalsIgnoreCase(""))
                                    {
                                        newTracker.getUniques().add(new UniqueDrop(
                                                getName(oldTracker.getSpecialLootReceiver()),
                                                oldTracker.getSpecialLoot(),
                                                oldTracker.getSpecialLootValue(),
                                                oldTracker.isFreeForAll(),
                                                oldTracker.getTeamSize()
                                        ));
                                    }
                                    if (!oldTracker.getPetReceiver().equalsIgnoreCase(""))
                                    {
                                        String pet  = newTracker.getInRaidType() == 0 ? "Olmlet" : "Lil' Zik";
                                        newTracker.getPets().add(new UniqueDrop(toJagexName(oldTracker.getPetReceiver()),pet));
                                    }
                                    if (!oldTracker.getKitReceiver().equalsIgnoreCase(""))
                                    {
                                        newTracker.getNTradables().add(new UniqueDrop(getName(oldTracker.getKitReceiver()),"Twisted Kit"));
                                    }
                                    if (!oldTracker.getDustReceiver().equalsIgnoreCase(""))
                                    {
                                        newTracker.getNTradables().add(new UniqueDrop(getName(oldTracker.getDustReceiver()),"Metamorphic Dust"));
                                    }
                                    if (newTracker.getTobPlayers()[0].equalsIgnoreCase(""))
                                    {
                                        newTracker.setTobPlayers(new String[]{oldTracker.getTobPlayer1(), oldTracker.getTobPlayer2(), oldTracker.getTobPlayer3(), oldTracker.getTobPlayer4(), oldTracker.getTobPlayer5()});
                                    }
                                    int deaths = Arrays.stream(newTracker.getTobDeaths()).sum();
                                    if (deaths == 0)
                                    {
                                        newTracker.setTobDeaths(new int[]{oldTracker.getTobPlayer1DeathCount(), oldTracker.getTobPlayer2DeathCount(), oldTracker.getTobPlayer3DeathCount(), oldTracker.getTobPlayer4DeathCount(), oldTracker.getTobPlayer5DeathCount()});
                                    }
                                    if (index[0] == -1)
                                    {
                                        index[0] = newTracker.getInRaidType();
                                    }
                                });
                        newTracker.setUniqueID(UUID.randomUUID().toString());
                        newList.add(newTracker);
                        
                    }
                    int endm = (int) new Date().getTime();
                    diff = endm - startm;
                    log.info("Finished migrating in " + diff + "ms");
                    if (newList.size() > 0)
                    {
                        updateRTList(newList, index[0]);
                    }
                    Files.move(path, Paths.get(filePath + ".bak"));
                }
            }
        }
    }
}
