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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bladecoder.inkplayer.Line;
import com.bladecoder.inkplayer.StoryListener;
import com.bladecoder.inkplayer.StoryManager;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.common.Config;
import com.bladecoder.inkplayer.common.DPIUtils;
import com.bladecoder.inkplayer.ui.UI.Screens;

public class StoryScreen implements AppScreen {
	private static final String TAG="StoryScreen";
	
	private static final float CHOICES_SHOW_TIME = 1.5f;
	
	private UI ui;
	private StoryManager storyManager;

	private Stage stage;

	private Button menuButton;
	private ChoicesUI choicesUI;
	private TextPanel textPanel;
	
	private Image background;
	
	private float tmpMoveByAmountY;

	private final Viewport viewport;
	
	private final StoryListener storyListener = new StoryListener() {
		
		@Override
		public void line(Line line) {
			textPanel.addText(line, new Runnable() {

				@Override
				public void run() {
					storyManager.next();
				}
				
			});
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
			
			if(tmpMoveByAmountY < 0) {
				tmpMoveByAmountY = 0;
			} else {
				textPanel.addAction(Actions.moveBy(0, tmpMoveByAmountY, CHOICES_SHOW_TIME, Interpolation.fade));
			}
			
			choicesUI.setVisible(true);
			choicesUI.setY(-choicesUI.getHeight());
			choicesUI.addAction(Actions.moveBy(0, choicesUI.getHeight(), CHOICES_SHOW_TIME, Interpolation.fade));
			
		}

		@Override
		public void end() {
			String theEnd = "THE END";
			
			Line line = new Line(theEnd, new HashMap<String, String>(0));
			
			textPanel.addText(line, new Runnable() {

				@Override
				public void run() {
					textPanel.addAction(Actions.sequence(Actions.delay(4), Actions.fadeOut(2), Actions.run(new Runnable() {
						@Override
						public void run() {
							ui.setCurrentScreen(Screens.CREDIT_SCREEN);
							textPanel.setColor(Color.WHITE);
						}
					})));				
				}
				
			});
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
		
		background.setSize(width, height);

		choicesUI.resize(width, height);
		textPanel.resize(width, height);
		textPanel.setY(textPanel.getY() + tmpMoveByAmountY);
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

	public void selectChoice(final int i) {
		textPanel.addAction(Actions.sequence(Actions.moveBy(0, -tmpMoveByAmountY, CHOICES_SHOW_TIME, Interpolation.fade), 
				Actions.run(new Runnable() {
					
					@Override
					public void run() {
						storyManager.selectChoice(i);
						storyManager.next();
						tmpMoveByAmountY = 0;
					}
				})));
	}
	
	
	public void newGame() {
		try {
			resetUI();
			storyManager.newStory(Config.getProperty(Config.STORY, "story.ink.json"));
			storyManager.next();
		} catch (Exception e) {
			Gdx.app.error( TAG, "IN NEW GAME", e);
			Gdx.app.exit();
		}
	}
	
	private void resetUI() {
		choicesUI.setVisible(false);
		textPanel.clearPanel();
		tmpMoveByAmountY = 0;
	}

	@Override
	public void show() {
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor( stage );
		multiplexer.addProcessor( inputProcessor ); 
		Gdx.input.setInputProcessor( multiplexer );
		stage.setScrollFocus(textPanel);
		
		textPanel.show();
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
		StoryScreenStyle style = ui.getSkin().get(StoryScreenStyle.class);
		storyManager = ui.getStoryManager();
		
		stage = new Stage(viewport);

		//recorder = ui.getRecorder();
		//testerBot = ui.getTesterBot();

		menuButton = new Button(ui.getSkin(), "menu");
		choicesUI = new ChoicesUI(this);
		textPanel = new TextPanel(ui);
		
		if(style.bgFile != null) {
			Texture tex = new Texture(EngineAssetManager.getInstance().getResAsset(style.bgFile));
			tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			
			background = new Image(tex);
			stage.addActor(background);
		}

		menuButton.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				ui.setCurrentScreen(Screens.MENU_SCREEN);
			}
		});

		stage.addActor(textPanel);
		stage.addActor(choicesUI);
		stage.addActor(menuButton);
		
		storyManager.setStoryListener(storyListener);
	}
	
	/** The style for the MenuScreen */
	public static class StoryScreenStyle {
		/** Optional. */
		public Drawable background;
		/** if 'background' is not specified try to load the bgFile */
		public String bgFile;

		public StoryScreenStyle() {
		}

		public StoryScreenStyle(StoryScreenStyle style) {
			background = style.background;
			bgFile = style.bgFile;
		}
	}
}
