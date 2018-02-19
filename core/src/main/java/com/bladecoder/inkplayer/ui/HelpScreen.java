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

import java.text.MessageFormat;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.i18n.I18N;
import com.bladecoder.inkplayer.ui.UI.Screens;

public class HelpScreen extends ScreenAdapter implements AppScreen {
	private final static String HELP_FILENAME = "ui/help";

	private Texture tex;

	private UI ui;

	private String localeFilename;
	private final Viewport viewport;
	
	public HelpScreen() {
		viewport= new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getWidth() * 9f/16f);
	}

	private final InputProcessor inputProcessor = new InputAdapter() {
		@Override
		public boolean keyUp(int keycode) {
			switch (keycode) {
			case Input.Keys.ESCAPE:
			case Input.Keys.BACK:
				ui.setCurrentScreen(Screens.MENU_SCREEN);
				break;
			}

			// FIXME: This ALWAYS return true, even when we haven't dealt with
			// the key. Is that expected?
			return true;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			ui.setCurrentScreen(Screens.MENU_SCREEN);
			return true;
		}
	};

	@Override
	public void render(float delta) {
		SpriteBatch batch = ui.getBatch();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		batch.draw(tex, 0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		
		float aspect = width / height;
		float bgAspect = (float)tex.getWidth() / (float)tex.getHeight();
		
		if(aspect < bgAspect)
			viewport.setWorldSize(width, (int)(width / bgAspect));
		else
			viewport.setWorldSize((int)(height * bgAspect), height);
		
		viewport.update(width, height, true);
	}

	@Override
	public void dispose() {
		if(tex != null) {
			tex.dispose();
			tex = null;
		}
	}

	@Override
	public void show() {
		final Locale locale = I18N.getCurrentLocale();
		String filename = HELP_FILENAME;

		localeFilename = MessageFormat.format("{0}_{1}.png", filename, locale.getLanguage());

		if (!EngineAssetManager.getInstance().assetExists(localeFilename))
			localeFilename = MessageFormat.format("{0}.png", filename);

		tex = new Texture(EngineAssetManager.getInstance().getResAsset(localeFilename));
		tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void setUI(UI ui) {
		this.ui = ui;
	}
}
