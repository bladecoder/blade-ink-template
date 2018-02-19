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

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bladecoder.inkplayer.StoryListener;
import com.bladecoder.inkplayer.ui.UI.Screens;
import com.bladecoder.inkplayer.util.DPIUtils;

public class StoryScreen implements AppScreen {
	private UI ui;

	private Stage stage;

	private Button menuButton;
	private ChoicesUI choicesUI;
	private TextPanel textPanel;
	
	private float tmpMoveByAmountY;

	private final Viewport viewport;
	
	private final StoryListener storyListener = new StoryListener() {
		
		@Override
		public void line(String text, HashMap<String, String> params) {
			Line line = new Line(text, params);
			
			textPanel.addText(line);
		}
		 
		@Override
		public void command(String name, HashMap<String, String> params) {
			
		}

		@Override
		public void choices(List<String> choices) {
			// Show options UI
			choicesUI.show(choices);		
			choicesUI.clearActions();

			tmpMoveByAmountY = choicesUI.getHeight() - textPanel.getY() + DPIUtils.getMarginSize();

			choicesUI.setVisible(true);
			choicesUI.setY(-choicesUI.getHeight());
			choicesUI.addAction(Actions.moveBy(0, choicesUI.getHeight(), 0.5f, Interpolation.fade));
			textPanel.addAction(Actions.moveBy(0, tmpMoveByAmountY, 0.5f, Interpolation.fade));
		}

		@Override
		public void end() {
			ui.setCurrentScreen(Screens.CREDIT_SCREEN);
		}
	};

	private final InputProcessor inputProcessor = new InputAdapter() {

		@Override
		public boolean keyUp(int keycode) {
			switch (keycode) {
			case Input.Keys.ESCAPE:
			case Input.Keys.BACK:
			case Input.Keys.MENU:
				showMenu();
				break;
			case Input.Keys.D:
				if (UIUtils.ctrl())
					// FIXME EngineLogger.toggle();
				break;
			case Input.Keys.SPACE:

				break;
			}

			return true;
		}

		@Override
		public boolean keyTyped(char character) {
			switch (character) {
			case 'f':
				// ui.toggleFullScreen();
				break;
			}
			
			return true;
		}
	};

	public StoryScreen() {
		viewport = new ScreenViewport();
	}

	public UI getUI() {
		return ui;
	}

	private void update(float delta) {
		stage.act(delta);
	}


	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		// STAGE
		stage.draw();
	}



	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);

		float size = DPIUtils.getPrefButtonSize();
		float margin = DPIUtils.getMarginSize();

		menuButton.setSize(size, size);
		menuButton.setPosition(stage.getViewport().getScreenWidth() - menuButton.getWidth() - margin,
				stage.getViewport().getScreenHeight() - menuButton.getHeight() - margin);

	}

	public void dispose() {
		stage.dispose();
	}

	private void showMenu() {
		pause();
		ui.setCurrentScreen(Screens.MENU_SCREEN);
	}

	public Stage getStage() {
		return stage;
	}

	public void selectChoice(int i) {
		//textPanel.addAction(Actions.moveBy(0, -tmpMoveByAmountY, 0.5f, Interpolation.fade));
		textPanel.setY(textPanel.getY()- tmpMoveByAmountY);
		ui.getStoryManager().selectChoice(i);
	}

	@Override
	public void show() {
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor( stage );
		multiplexer.addProcessor( inputProcessor ); 
		Gdx.input.setInputProcessor( multiplexer );
		
		
		textPanel.show();
		ui.getStoryManager().continueMaximally();
	}

	@Override
	public void hide() {

	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void setUI(final UI ui) {
		this.ui = ui;

		//recorder = ui.getRecorder();
		//testerBot = ui.getTesterBot();

		menuButton = new Button(ui.getSkin(), "menu");
		choicesUI = new ChoicesUI(this);
		textPanel = new TextPanel(ui);

		menuButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				ui.setCurrentScreen(Screens.MENU_SCREEN);
			}
		});

		stage = new Stage(viewport);
		stage.addActor(choicesUI);
		stage.addActor(menuButton);
		stage.addActor(textPanel);
		
		ui.getStoryManager().setStoryListener(storyListener);
	}
}
