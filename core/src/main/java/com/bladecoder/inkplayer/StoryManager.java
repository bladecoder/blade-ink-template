package com.bladecoder.inkplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.JsonValue;
import com.bladecoder.ink.runtime.Choice;
import com.bladecoder.ink.runtime.InkList;
import com.bladecoder.ink.runtime.ListDefinition;
import com.bladecoder.ink.runtime.Story;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.common.FileUtils;
import com.bladecoder.inkplayer.i18n.I18N;


public class StoryManager implements Serializable {
	private static final String TAG="StoryManager";
	
	public static final String GAMESTATE_EXT = ".gamestate.v1";
	private static final String GAMESTATE_FILENAME = "default" + GAMESTATE_EXT;
	
	public final static char NAME_VALUE_TAG_SEPARATOR = ':';
	public final static char NAME_VALUE_PARAM_SEPARATOR = '=';
	private final static String PARAM_SEPARATOR = ",";
	public final static char COMMAND_MARK = '>';

	private static ResourceBundle i18n;

	private Story story = null;
	private ExternalFunctions externalFunctions;

	private String storyName;

	private StoryListener l;
	
	// save all spit texts
	private List<Line> record = new ArrayList<Line>();

	public StoryManager() {
		externalFunctions = new ExternalFunctions();
	}

	public void newStory(InputStream is) throws Exception {

		String json = getJsonString(is);
		story = new Story(json);

		externalFunctions.bindExternalFunctions(this);
	}

	public void newStory(String storyName) throws Exception {
		FileHandle asset = EngineAssetManager.getInstance().getAsset(storyName);

		try {
			long initTime = System.currentTimeMillis();
			newStory(asset.read());
			Gdx.app.debug( TAG, "INK STORY LOADING TIME (ms): " + (System.currentTimeMillis() - initTime));

			this.storyName = storyName;

			loadI18NBundle();
			record.clear();
		} catch (Exception e) {
			Gdx.app.error( TAG, "Cannot load Ink Story: " + storyName + " " + e.getMessage());
		}
	}

	private void loadI18NBundle() {
		if (storyName != null && EngineAssetManager.getInstance().getAsset(storyName + "-ink.properties").exists())
			i18n = I18N.getBundle(storyName + "-ink", true);
	}
	
	public List<Line> getRecod() {
		return record;
	}

	public String translateLine(String line) {
		if (line.charAt(0) == I18N.PREFIX) {
			String key = line.substring(1);

			// In ink, several keys can be included in the same line.
			String[] keys = key.split("@");

			String translated = "";

			for (String k : keys) {
				try {
					translated += i18n.getString(k);
				} catch (Exception e) {
					Gdx.app.error( TAG, "MISSING TRANSLATION KEY: " + key);
					return key;
				}
			}

			return translated;
		}

		return line;
	}

	public String getVariable(String name) {
		return story.getVariablesState().get(name).toString();
	}

	public boolean compareVariable(String name, String value) {
		if (story.getVariablesState().get(name) instanceof InkList) {
			return ((InkList) story.getVariablesState().get(name)).ContainsItemNamed(value);
		} else {
			return story.getVariablesState().get(name).toString().equals(value);
		}
	}

	public void setVariable(String name, String value) throws Exception {
		if (story.getVariablesState().get(name) instanceof InkList) {

			InkList rawList = (InkList) story.getVariablesState().get(name);

			if (rawList.getOrigins() == null) {
				List<String> names = rawList.getOriginNames();
				if (names != null) {
					ArrayList<ListDefinition> origins = new ArrayList<ListDefinition>();
					for (String n : names) {
						ListDefinition def = story.getListDefinitions().getDefinition(n);
						if (!origins.contains(def))
							origins.add(def);
					}
					rawList.setOrigins(origins);
				}
			}

			rawList.addItem(value);
		} else
			story.getVariablesState().set(name, value);
	}
	
	public void next() {
		String line = null;

		HashMap<String, String> currentLineParams = new HashMap<String, String>(0);

		if (story.canContinue()) {
			try {
				line = story.Continue();
				currentLineParams.clear();

				// Remove trailing '\n'
				if (!line.isEmpty())
					line = line.substring(0, line.length() - 1);

				if (!line.isEmpty()) {
					Gdx.app.debug( TAG, "INK LINE: " + line);

					processParams(story.getCurrentTags(), currentLineParams);

					// PROCESS COMMANDS
					if (line.charAt(0) == COMMAND_MARK) {
						processCommand(currentLineParams, line);
					} else {
						processTextLine(currentLineParams, line);
					}
				} else {
					Gdx.app.debug( TAG, "INK EMPTY LINE!");
				}
			} catch (Exception e) {
				Gdx.app.error( TAG, e.getMessage(), e);
			}

			if (story.getCurrentErrors() != null && !story.getCurrentErrors().isEmpty()) {
				Gdx.app.error( TAG, story.getCurrentErrors().get(0));
			}

		} else if(hasChoices()) {
			if(l != null)
				l.choices(getChoices());
		} else {
			if(l != null)
				l.end();
		}
	}

	private void processParams(List<String> input, HashMap<String, String> output) {

		for (String t : input) {
			String key;
			String value;

			int i = t.indexOf(NAME_VALUE_TAG_SEPARATOR);

			// support ':' and '=' as param separator
			if (i == -1)
				i = t.indexOf(NAME_VALUE_PARAM_SEPARATOR);

			if (i != -1) {
				key = t.substring(0, i).trim();
				value = t.substring(i + 1, t.length()).trim();
			} else {
				key = t.trim();
				value = null;
			}

			Gdx.app.debug( TAG, "PARAM: " + key + " value: " + value);

			output.put(key, value);
		}
	}

	private void processCommand(HashMap<String, String> params, String line) {
		String commandName = null;
		String commandParams[] = null;

		int i = line.indexOf(NAME_VALUE_TAG_SEPARATOR);

		if (i == -1) {
			commandName = line.substring(1).trim();
		} else {
			commandName = line.substring(1, i).trim();
			commandParams = line.substring(i + 1).split(PARAM_SEPARATOR);

			processParams(Arrays.asList(commandParams), params);
		}

		if ("debug".equals(commandName)) {
			Gdx.app.debug( TAG, params.get("text"));
		} else {
			if(l != null)
				l.command(commandName, params);
		}
	}

	private void processTextLine(HashMap<String, String> params, String line) {

		// Get actor name from Line. Actor is separated by '>' character.
		// ej. "Johnny> Hello punks!"
		if (!params.containsKey("actor")) {
			int idx = line.indexOf(COMMAND_MARK);

			if (idx != -1) {
				params.put("actor", line.substring(0, idx).trim());
				line = line.substring(idx + 1).trim();
			}
		}

		String tline =  translateLine(line);
		
		Line line2 = new Line(tline, params);
		record.add(line2);
		
		if(l != null) {
			l.line(line2);
		}
	}

	public Story getStory() {
		return story;
	}

	public void run(String path) throws Exception {
		if (story == null) {
			Gdx.app.error( TAG, "Ink Story not loaded!");
			return;
		}

		story.choosePathString(path);
	}

	public boolean hasChoices() {
		return (story != null && story.getCurrentChoices().size() > 0);
	}

	public List<String> getChoices() {

		List<Choice> options = story.getCurrentChoices();
		List<String> choices = new ArrayList<String>(options.size());

		for (Choice o : options) {
			String line = o.getText();

			int idx = line.indexOf(StoryManager.COMMAND_MARK);

			if (idx != -1) {
				line = line.substring(idx + 1).trim();
			}

			choices.add(translateLine(line));
		}

		return choices;
	}

	private String getJsonString(InputStream is) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			// Replace the BOM mark
			if (line != null)
				line = line.replace('\uFEFF', ' ');

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public void selectChoice(int i) {
		try {
			story.chooseChoiceIndex(i);
		} catch (Exception e) {
			Gdx.app.error( TAG, e.getMessage(), e);
		}
	}
	
	public void setStoryListener(StoryListener sl) {
		this.l = sl;
	}
	
	public boolean savedGameExists() {
		return savedGameExists(GAMESTATE_FILENAME);
	}

	public boolean savedGameExists(String filename) {
		return EngineAssetManager.getInstance().getUserFile(filename).exists()
				|| FileUtils.exists(EngineAssetManager.getInstance().getAsset("tests/" + filename));
	}

	public void loadGameState() throws IOException {
		long initTime = System.currentTimeMillis();
		loadGameState(GAMESTATE_FILENAME);
		Gdx.app.debug(TAG, "GAME STATE LOADING TIME (ms): " + (System.currentTimeMillis() - initTime));
	}

	public void loadGameState(String filename) throws IOException {
		FileHandle savedFile = null;

		if (EngineAssetManager.getInstance().getUserFile(filename).exists())
			savedFile = EngineAssetManager.getInstance().getUserFile(filename);
		else
			savedFile = EngineAssetManager.getInstance().getAsset("tests/" + filename);

		loadGameState(savedFile);
	}

	public void loadGameState(FileHandle savedFile) throws IOException {
		Gdx.app.debug(TAG, "LOADING GAME STATE");

		if (savedFile.exists()) {
			JsonValue root = new JsonReader().parse(savedFile.reader("UTF-8"));

			Json json = new Json();
			json.setIgnoreUnknownFields(true);

			read(json, root);
		} else {
			throw new IOException("LOADGAMESTATE: no saved game exists");
		}
	}

	public void saveGameState() throws IOException {
		saveGameState(GAMESTATE_FILENAME);
	}

	public void removeGameState(String filename) throws IOException {
		EngineAssetManager.getInstance().getUserFile(filename).delete();
	}

	public void saveGameState(String filename) throws IOException {
		Gdx.app.debug(TAG, "SAVING GAME STATE");

		Json json = new Json();
		json.setOutputType(OutputType.javascript);

		String s = null;

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG)
			s = json.prettyPrint(this);
		else
			s = json.toJson(this);

		Writer w = EngineAssetManager.getInstance().getUserFile(filename).writer(false, "UTF-8");

		try {
			w.write(s);
			w.flush();
		} catch (IOException e) {
			throw new IOException("ERROR SAVING GAME", e);
		} finally {
			w.close();
		}
	}	

	@Override
	public void write(Json json) {
		// SAVE STORY
		json.writeValue("storyName", storyName);

		if (story != null) {
			json.writeValue("record", record, ArrayList.class, Line.class);
			
			try {
				json.writeValue("story", story.getState().toJson());
			} catch (Exception e) {
				Gdx.app.error( TAG, e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {
		
		// READ STORY
		String storyName = json.readValue("storyName", String.class, jsonData);
		String storyString = json.readValue("story", String.class, jsonData);
		if (storyString != null) {
			try {
				newStory(storyName);

				long initTime = System.currentTimeMillis();
				story.getState().loadJson(storyString);
				Gdx.app.debug( TAG, "INK SAVED STATE LOADING TIME (ms): " + (System.currentTimeMillis() - initTime));
			} catch (Exception e) {
				Gdx.app.error( TAG, e.getMessage(), e);
			}
			
			record = json.readValue("record", ArrayList.class, Line.class, jsonData);
		}
	}
}
