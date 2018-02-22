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
package com.bladecoder.inkplayer.ui;

import java.util.ResourceBundle;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.bladecoder.inkplayer.StoryManager;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.common.RectangleRenderer;
import com.bladecoder.inkplayer.i18n.I18N;

public class UI {
	private static final String TAG="UI";

	private static final String SKIN_FILENAME = "ui/ui.json";

	private boolean fullscreen = false;

	private AppScreen screen;

	private SpriteBatch batch;
	private Skin skin;

	private StoryManager storyManager;

	private static ResourceBundle i18n;

	public enum Screens {
		INIT_SCREEN, STORY_SCREEN, LOADING_SCREEN, MENU_SCREEN, HELP_SCREEN, CREDIT_SCREEN, LOAD_GAME_SCREEN, SAVE_GAME_SCREEN
	}

	private final AppScreen screens[];

	public UI(StoryManager storyManager) {
		this.storyManager = storyManager;

		batch = new SpriteBatch();

		screens = new AppScreen[Screens.values().length];

		Gdx.input.setCatchMenuKey(true);

		loadAssets();

		screens[Screens.INIT_SCREEN.ordinal()] = getScreenInstance(Screens.INIT_SCREEN.toString(), InitScreen.class);
		screens[Screens.STORY_SCREEN.ordinal()] = getScreenInstance(Screens.STORY_SCREEN.toString(), StoryScreen.class);
		screens[Screens.LOADING_SCREEN.ordinal()] = null;
		screens[Screens.MENU_SCREEN.ordinal()] = getScreenInstance(Screens.MENU_SCREEN.toString(), MenuScreen.class);
		screens[Screens.HELP_SCREEN.ordinal()] = null;
		screens[Screens.CREDIT_SCREEN.ordinal()] = getScreenInstance(Screens.CREDIT_SCREEN.toString(),
				CreditsScreen.class);
		screens[Screens.LOAD_GAME_SCREEN.ordinal()] = null;
		screens[Screens.SAVE_GAME_SCREEN.ordinal()] = null;

		for (AppScreen s : screens)
			if(s != null)
				s.setUI(this);

		setCurrentScreen(Screens.INIT_SCREEN);
	}

	private AppScreen getScreenInstance(String prop, Class<?> defaultClass) {

		try {
			return (AppScreen) ClassReflection.newInstance(defaultClass);
		} catch (Exception e) {
			Gdx.app.error(TAG, "Error instancing screen", e);
		}

		return null;
	}

	public AppScreen getScreen(Screens state) {
		return screens[state.ordinal()];
	}

	public void setScreen(Screens state, AppScreen s) {
		screens[state.ordinal()] = s;
	}

	public SpriteBatch getBatch() {
		return batch;
	}

	public AppScreen getCurrentScreen() {
		return screen;
	}

	public void setCurrentScreen(Screens s) {
		Gdx.app.debug(TAG, "Setting SCREEN: " + s.name());
		setCurrentScreen(screens[s.ordinal()]);
	}

	public void setCurrentScreen(AppScreen s) {

		if (screen != null) {
			screen.hide();
		}

		screen = s;

		screen.show();

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public TextureAtlas getUIAtlas() {
		return skin.getAtlas();
	}

	public Skin getSkin() {
		return skin;
	}

	public void render() {
		// for long processing frames, limit delta to 1/30f to avoid skipping animations
		float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f);

		screen.render(delta);
	}

	private void loadAssets() {
		BladeSkin.addStyleTag(ChoicesUI.ChoicesUIStyle.class);
		BladeSkin.addStyleTag(CreditsScreen.CreditScreenStyle.class);
		BladeSkin.addStyleTag(MenuScreen.MenuScreenStyle.class);
		
		BladeSkin.addStyleTag(StoryScreen.StoryScreenStyle.class);
		BladeSkin.addStyleTag(TextPanel.TextPanelStyle.class);
		
		loadI18NBundle();

		FileHandle skinFile = EngineAssetManager.getInstance().getAsset(SKIN_FILENAME);
		TextureAtlas atlas = new TextureAtlas(EngineAssetManager.getInstance()
				.getResAsset(SKIN_FILENAME.substring(0, SKIN_FILENAME.lastIndexOf('.')) + ".atlas"));
		skin = new BladeSkin(skinFile, atlas);
	}

	public void resize(int width, int height) {
		if (screen != null)
			screen.resize(width, height);
	}

	public void toggleFullScreen() {
		if (!fullscreen) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
			fullscreen = true;
		} else {
			Gdx.graphics.setWindowedMode(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
			fullscreen = false;
		}
	}

	public void dispose() {
		screen.hide();
		batch.dispose();
		skin.dispose();

		RectangleRenderer.dispose();

		// DISPOSE ALL SCREENS
		for (AppScreen s : screens)
			if(s != null)
				s.dispose();

		EngineAssetManager.getInstance().dispose();
	}

	public void resume() {
		if (Gdx.app.getType() != ApplicationType.Desktop) {
			// RESTORE GL CONTEXT
			RectangleRenderer.dispose();
		}

		if (screen != null)
			screen.resume();
	}

	public void pause() {
		if (screen != null)
			screen.pause();
	}

	public StoryManager getStoryManager() {
		return storyManager;
	}

	public void loadI18NBundle() {
		if (EngineAssetManager.getInstance().getAsset("ui/ui.properties").exists())
			i18n = I18N.getBundle("ui/ui", true);
	}

	public String translate(String key) {
		String translated = key;

		try {
			translated = i18n.getString(key);
		} catch (Exception e) {
			Gdx.app.error(TAG, "MISSING TRANSLATION KEY: " + key);
		}

		return translated;
	}
}
