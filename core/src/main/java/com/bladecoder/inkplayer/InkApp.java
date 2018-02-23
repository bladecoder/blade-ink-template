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

import java.io.IOException;
import java.text.MessageFormat;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.common.Config;
import com.bladecoder.inkplayer.ui.UI;

public class InkApp implements ApplicationListener {
	private static final String TAG = "InkApp";

	/* Run the specified path at init */
	private String initPath;

	private String forceRes;
	private boolean debug = false;
	private boolean restart = false;
	private String gameState;
	private String recordName;

	private StoryManager storyManager;
	private UI ui;

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

			if (initPath == null) {
				initPath = Config.getProperty(Config.INITPATH_PROP, initPath);
			}

			if (initPath != null) {
				try {
					StoryListener sl = storyManager.getStoryListener();
					storyManager.setStoryListener(null);
					storyManager.newStory(Config.getProperty(Config.STORY, "story.ink.json"));
					storyManager.run(initPath);
					storyManager.setStoryListener(sl);
					storyManager.next();
				} catch (Exception e) {
					dispose();
					Gdx.app.error(TAG, "EXITING: " + e.getMessage());
					Gdx.app.exit();
				}

				ui.setCurrentScreen(UI.Screens.STORY_SCREEN);
			}
			
			if(restart) {
				// TODO
			}
			
			if(gameState != null) {
				// TODO
			}
			
			if(recordName != null) {
				// TODO
			}
		}

		// Capture back key
		Gdx.input.setCatchBackKey(true);

		Gdx.graphics.setContinuousRendering(false);
		Gdx.graphics.requestRendering();
	}

	@Override
	public void dispose() {
		Gdx.app.debug(TAG, "GAME DISPOSE");
		ui.dispose();
	}

	@Override
	public void render() {
		ui.render();
	}

	@Override
	public void resize(int width, int height) {
		Gdx.app.debug(TAG, MessageFormat.format("GAME RESIZE {0}x{1}", width, height));

		if (ui != null)
			ui.resize(width, height);
	}

	@Override
	public void pause() {
		Gdx.app.debug(TAG, "GAME PAUSE");
		ui.pause();

		if (ui.getStoryManager().getStory() != null) {
			try {
				ui.getStoryManager().saveGameState();
			} catch (IOException e) {
				Gdx.app.error(TAG, e.getMessage());
			}
		}
	}

	@Override
	public void resume() {
		Gdx.app.debug(TAG, "GAME RESUME");
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ui.resume();
	}

}
