/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.bladecoder.inkplayer.assets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ResolutionFileResolver.Resolution;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.bladecoder.inkplayer.InkApp;
import com.bladecoder.inkplayer.util.Config;


public class EngineAssetManager extends AssetManager {

	public static final String DESKTOP_PREFS_DIR = "InkPlayer";
	public static final String NOT_DESKTOP_PREFS_DIR = "data/";

	public static final String FONT_DIR = "ui/fonts/";
	public static final String FONT_EXT = ".ttf";
	
	public static final String INK_EXT = ".ink.json";

	private static EngineAssetManager instance = null;

	private float scale = 1;

	private EngineResolutionFileResolver resResolver;

	protected EngineAssetManager() {
		this(new InternalFileHandleResolver());
		// getLogger().setLevel(Application.LOG_DEBUG);
	}

	protected EngineAssetManager(FileHandleResolver resolver) {
		super(resolver);

		resResolver = new EngineResolutionFileResolver(resolver);
		setLoader(Texture.class, new TextureLoader(resResolver));
		setLoader(TextureAtlas.class, new TextureAtlasLoader(resResolver));
		setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

		Texture.setAssetManager(this);
	}

	public float getScale() {
		return scale;
	}

	public void setScale(int worldWidth, int worldHeight) {
		Resolution r[] = getResolutions(resResolver.getBaseResolver(), worldWidth, worldHeight);

		if (r == null || r.length == 0) {
			Gdx.app.error( InkApp.LOG_TAG, "No resolutions defined. Maybe your 'assets' folder doesn't exists or it's empty");
			return;
		}

		resResolver.setResolutions(r);
		resResolver.selectResolution();
		scale = resResolver.getResolution().portraitWidth / (float) worldWidth;

		Gdx.app.debug( InkApp.LOG_TAG, "Setting ASSETS SCALE: " + scale);
	}

	public static EngineAssetManager getInstance() {
		if (instance == null) {
			instance = new EngineAssetManager();
		}

		return instance;
	}

	public void forceResolution(String suffix) {
		resResolver.setFixedResolution(suffix);

		Gdx.app.debug( InkApp.LOG_TAG, "FORCING ASSETS RESOLUTION SCALE: " + suffix);
	}

	public Resolution getResolution() {
		return resResolver.getResolution();
	}

	public boolean isLoading() {
		return !update();
	}

	/**
	 * Returns a file in the asset directory SEARCHING in the resolution
	 * directories
	 */
	public FileHandle getResAsset(String filename) {
		return resResolver.resolve(filename);
	}

	/**
	 * Returns a file in the asset directory without searching in the resolution
	 * directories
	 */
	public FileHandle getAsset(String filename) {
		return resResolver.baseResolve(filename);
	}

	public void dispose() {
		super.dispose();
		instance = null;
	}

	public boolean assetExists(String filename) {
		return resResolver.exists(filename);
	}

	private Resolution[] getResolutions(FileHandleResolver resolver, int worldWidth, int worldHeight) {
		ArrayList<Resolution> rl = new ArrayList<Resolution>();

		String list[] = null;
		
		String configRes = Config.getProperty(Config.RESOLUTIONS, null);
		
		if(configRes != null) {
			list = configRes.split(",");
		} else {
			list = listAssetFiles("ui");
		}
		
		if(list == null)
			list = new String[]{"1"};
		
		for (String name : list) {
			
			try {
				float scale = Float.parseFloat(name);

				Gdx.app.debug( InkApp.LOG_TAG, "RES FOUND: " + scale);

				Resolution r = new Resolution((int) (worldWidth * scale), (int) (worldHeight * scale), name);

				rl.add(r);
			} catch (NumberFormatException e) {

			}
		}

		Collections.sort(rl, new Comparator<Resolution>() {
			public int compare(Resolution a, Resolution b) {
				return a.portraitWidth - b.portraitWidth;
			}
		});

		return rl.toArray(new Resolution[rl.size()]);
	}

	public String[] listAssetFiles(String base) {
		FileHandleResolver resolver = resResolver.getBaseResolver();

		String list[] = null;

		if (Gdx.app.getType() != ApplicationType.Desktop) {

			FileHandle[] l = resolver.resolve(base).list();
			list = new String[l.length];

			for (int i = 0; i < l.length; i++)
				list[i] = l[i].name();

		} else {
			// FOR DESKTOP
			URL u = EngineAssetManager.class.getResource("/" + resolver.resolve(base).path());

			if (u != null && u.getProtocol().equals("jar")) {
				list = getFilesFromJar("/" + base);
			} else {
				String n = resolver.resolve(base).path();

				if (u != null)
					n = u.getFile();

				FileHandle f = null;

				try {
					f = Gdx.files.absolute(URLDecoder.decode(n, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Gdx.app.error( InkApp.LOG_TAG, "Error decoding URL", e);
					return new String[0];
				}

				FileHandle[] l = f.list();
				list = new String[l.length];

				for (int i = 0; i < l.length; i++)
					list[i] = l[i].name();
			}
		}

		return list;
	}

	/**
	 * Returns the resolutions from a jar file.
	 */
	private String[] getFilesFromJar(String base) {
		URL dirURL = EngineAssetManager.class.getResource(base);

		Set<String> result = new HashSet<String>(); // avoid duplicates in case
													// it is a subdirectory

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
			JarFile jar;

			try {
				jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			} catch (Exception e) {
				Gdx.app.error( InkApp.LOG_TAG, "Locating jar file", e);
				return result.toArray(new String[result.size()]);
			}

			Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
															// in jar

			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();

				int start = name.indexOf('/');
				int end = name.lastIndexOf('/');

				if (start == end)
					continue;

				String entry = name.substring(start + 1, end);

				result.add(entry);
			}

			try {
				jar.close();
			} catch (IOException e) {
				Gdx.app.error( InkApp.LOG_TAG, "Closing jar file", e);
				return result.toArray(new String[result.size()]);
			}

		}

		return result.toArray(new String[result.size()]);
	}

	public FileHandle getUserFile(String filename) {
		FileHandle file = null;

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.Applet) {
			String dir = Config.getProperty(Config.TITLE_PROP, DESKTOP_PREFS_DIR);
			dir.replace(" ", "");

			StringBuilder sb = new StringBuilder();
			sb.append(".").append(dir).append("/").append(filename);
			
			if (System.getProperty("os.name").toLowerCase().contains("mac")
					&& System.getenv("HOME").contains("Containers")) {

				file = Gdx.files.absolute(System.getenv("HOME") + "/" + sb.toString());
			} else {

				file = Gdx.files.external(sb.toString());
			}
		} else {
			file = Gdx.files.local(NOT_DESKTOP_PREFS_DIR + filename);
		}

		return file;
	}

	public FileHandle getUserFolder() {
		FileHandle file = null;

		if (Gdx.app.getType() == ApplicationType.Desktop || Gdx.app.getType() == ApplicationType.Applet) {
			String dir = Config.getProperty(Config.TITLE_PROP, DESKTOP_PREFS_DIR);
			dir.replace(" ", "");

			StringBuilder sb = new StringBuilder(".");
			
			if (System.getProperty("os.name").toLowerCase().contains("mac")
					&& System.getenv("HOME").contains("Containers")) {

				file = Gdx.files.absolute(System.getenv("HOME") + "/" + sb.append(dir).toString());
			} else {

				file = Gdx.files.external(sb.append(dir).toString());
			}
			
		} else {
			file = Gdx.files.local(NOT_DESKTOP_PREFS_DIR);
		}

		return file;
	}
}
