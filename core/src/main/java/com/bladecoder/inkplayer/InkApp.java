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
package com.bladecoder.inkplayer;

import java.text.MessageFormat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.common.Config;
import com.bladecoder.inkplayer.ui.UI;

public class InkApp implements ApplicationListener {
	private static final String TAG="InkApp";

	/* Run the specified path at init */
	private String initPath;
	
	private String gameState;
	private String recordName;
	private String forceRes;
	private boolean debug = false;
	private boolean restart = false;
	
	private StoryManager storyManager;
	private UI ui;

	public static UI getAppUI() {
		return ((InkApp) Gdx.app.getApplicationListener()).getUI();
	}

	public void setInitPath(String s) {
		initPath = s;
	}

	public void loadGameState(String s) {
		gameState = s;
	}

	public void setPlayMode(String recordName) {
		this.recordName = recordName;
	}

	public void setDebugMode() {
		debug = true;
	}

	public void setRestart() {
		restart = true;
	}

	public void forceResolution(String forceRes) {
		this.forceRes = forceRes;
	}

	public UI getUI() {
		return ui;
	}

	@Override
	public void create() {
		EngineAssetManager.getInstance().setScale(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		storyManager = new StoryManager();
		ui = new UI(storyManager);
		
		if (!debug)
			debug = Config.getProperty(Config.DEBUG_PROP, debug);

		if (debug)
			Gdx.app.setLogLevel(Application.LOG_DEBUG);

		Gdx.app.debug(TAG, "GAME CREATE");

		if (forceRes == null)
			forceRes = Config.getProperty(Config.FORCE_RES_PROP, forceRes);

		if (forceRes != null) {
			EngineAssetManager.getInstance().forceResolution(forceRes);
		}

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			/*
			if (chapter == null)
				chapter = Config.getProperty(Config.CHAPTER_PROP, chapter);

			if (testScene == null) {
				testScene = Config.getProperty(Config.TEST_SCENE_PROP, testScene);
			}

			if (testScene != null || chapter != null) {
				try {
					World.getInstance().loadChapter(chapter, testScene, true);
				} catch (Exception e) {
					dispose();
					Gdx.app.debug.error("EXITING: " + e.getMessage());
					Gdx.app.exit();
				}

				ui.setCurrentScreen(UI.Screens.SCENE_SCREEN);
			}

			if (gameState == null)
				gameState = Config.getProperty(Config.LOAD_GAMESTATE_PROP, gameState);

			if (gameState != null) {
				try {
					World.getInstance().loadGameState(gameState);
				} catch (IOException e) {
					Gdx.app.debug.error(e.getMessage());
				}
			}

			if (restart) {
				try {
					World.getInstance().loadChapter(null);
					
					ui.setCurrentScreen(UI.Screens.SCENE_SCREEN);
				} catch (Exception e) {
					Gdx.app.debug.error("ERROR LOADING GAME", e);
					dispose();
					Gdx.app.exit();
				}
			}

			if (recordName == null)
				recordName = Config.getProperty(Config.PLAY_RECORD_PROP, recordName);

			if (recordName != null) {
				ui.getRecorder().setFilename(recordName);
				ui.getRecorder().load();
				ui.getRecorder().setPlaying(true);
				
				ui.setCurrentScreen(UI.Screens.SCENE_SCREEN);
			}
			*/
		}
		
		// Capture back key
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void dispose() {
		Gdx.app.debug(TAG, "GAME DISPOSE");
		//World.getInstance().dispose();
		ui.dispose();
	}

	@Override
	public void render() {
		ui.render();
	}

	@Override
	public void resize(int width, int height) {
		Gdx.app.debug(TAG, MessageFormat.format("GAME RESIZE {0}x{1}", width, height));
		
		if(ui != null)
			ui.resize(width, height);
	}

	@Override
	public void pause() {
		/*
		boolean bot = ui.getTesterBot().isEnabled();
		boolean r = ui.getRecorder().isPlaying();

		if (!World.getInstance().isDisposed() && 
				((!bot && !r) || Gdx.app.debug.lastError != null)) {
			Gdx.app.debug(LOG_TAG, "GAME PAUSE");
			ui.pause();
			try {
				World.getInstance().saveGameState();
			} catch (IOException e) {
				Gdx.app.debug.error(e.getMessage());
			}
		} else {
			Gdx.app.debug(LOG_TAG, "NOT PAUSING WHEN BOT IS RUNNING OR PLAYING RECORDED GAME");
		}
		*/
	}

	@Override
	public void resume() {
		Gdx.app.debug(TAG, "GAME RESUME");
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ui.resume();
	}

}
