package com.templetracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.templetracker.constructors.TempleTracker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
			//use json format so serializing and deserializing is easy
			Gson gson = new GsonBuilder().create();

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

	public ArrayList<TempleTracker> readFromFile() {
		try
		{
			//use json format so serializing and deserializing is easy
			Gson gson = new GsonBuilder().create();

			String fileName = dir_TT + "\\temple_trek_data.log";

			JsonParser parser = new JsonParser();

			BufferedReader bufferedreader = new BufferedReader(new FileReader(fileName));
			String line;

			ArrayList<TempleTracker> TTList = new ArrayList<>();

			while ((line = bufferedreader.readLine()) != null && line.length() > 0) {
				try {
					TempleTracker parsed = gson.fromJson(parser.parse(line), TempleTracker.class);
					TTList.add(parsed);
				}
				catch (JsonSyntaxException e) {
					System.out.println("Bad line: " + line);
				}

			}

			bufferedreader.close();
			return TTList;

		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage() + " in writeToFile");
			return new ArrayList<>();
		}
	}


	public void IGNORE_RESULT(boolean b) {}
}
