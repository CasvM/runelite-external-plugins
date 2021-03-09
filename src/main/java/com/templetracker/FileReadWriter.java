package com.templetracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.templetracker.constructors.TempleTracker;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class FileReadWriter
{
	private String username;
	private String dir_TT;

	private void createFolders()
	{
		File dir = new File(RUNELITE_DIR, "temple-tracker");
		IGNORE_RESULT(dir.mkdir());
		dir = new File(dir, username);
		IGNORE_RESULT(dir.mkdir());
		File newTTFile = new File(dir + "\\temple_trek_data.log");

		try {
			IGNORE_RESULT(newTTFile.createNewFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.dir_TT = dir.getAbsolutePath();
	}

	public void updateUsername(final String username) {
		this.username = username;
		createFolders();
	}

	public void writeToFile(TempleTracker templeTracker) {
		try
		{
			log.info("writer started");

			//use json format so serializing and deserializing is easy
			Gson gson = new GsonBuilder().create();

//			JsonParser parser = new JsonParser();

			String fileName = dir_TT + "\\temple_trek_data.log";

			FileWriter fw = new FileWriter(fileName,true); //the true will append the new data

			gson.toJson(templeTracker, fw);

			fw.append("\n");

			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage() + " in writeToFile");
		}
	}

	public void IGNORE_RESULT(boolean b) {}
}
