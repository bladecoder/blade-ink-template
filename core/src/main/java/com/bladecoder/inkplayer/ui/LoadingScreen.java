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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.ui.UI.Screens;
import com.bladecoder.inkplayer.util.RectangleRenderer;

public class LoadingScreen extends ScreenAdapter implements AppScreen {
	private final static float INIT_TIME_SEG = 1f;


	private int pos = 0;
	private int numSquares = 3;

	private float x, y;

	private float squareWidth = 30f;
	private float squareHeight = 30f;
	private float margin = 10f;

	private float initTime = 0;

	private float delta = 0;

	private UI ui;

	private final Viewport viewport = new ScreenViewport();

	@Override
	public void render(float delta) {

		if (!EngineAssetManager.getInstance().isLoading()) {
			ui.setCurrentScreen(Screens.SCENE_SCREEN);
			return;
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Only show the squares when time > INIT_TIME
		if (initTime < INIT_TIME_SEG) {
			initTime += delta;
			return;
		}

		final SpriteBatch batch = ui.getBatch();

		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();

		update(delta);

		for (int i = 0; i < numSquares; i++) {
			final Color color = i == pos ? Color.WHITE : Color.GRAY;
			RectangleRenderer.draw(ui.getBatch(), x + i * (squareWidth + margin), y, squareWidth, squareHeight, color);
		}
		batch.end();
	}

	private void update(float d) {
		delta += d;

		if (delta > 0.4) {
			pos = (pos + 1) % numSquares;
			delta = 0;
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);

		x = (viewport.getWorldWidth() - (squareWidth * numSquares + margin * (numSquares - 1))) / 2;
		y = (viewport.getWorldHeight() - squareHeight) / 2;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(null);
		initTime = 0;
		delta = 0;
	}

	@Override
	public void setUI(UI ui) {
		this.ui = ui;
	}
}
