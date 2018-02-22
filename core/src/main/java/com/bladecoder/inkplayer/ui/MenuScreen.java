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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.bladecoder.inkplayer.assets.EngineAssetManager;
import com.bladecoder.inkplayer.common.Config;
import com.bladecoder.inkplayer.common.DPIUtils;
import com.bladecoder.inkplayer.ui.UI.Screens;

public class MenuScreen extends ScreenAdapter implements AppScreen {
	private static final String TAG = "MenuScreen";

	private UI ui;

	private Stage stage;
	private Texture bgTexFile = null;
	private Texture titleTexFile = null;
	private Button credits;
	private Button debug;

	private final Table menuButtonTable = new Table();
	private final Table iconStackTable = new Table();

	private Music music;

	public MenuScreen() {
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);

		float size = DPIUtils.getPrefButtonSize();
		credits.setSize(size, size);
		debug.setSize(size, size);
	}

	@Override
	public void dispose() {

		if (stage != null) {

			stage.dispose();
			stage = null;

			if (bgTexFile != null) {
				bgTexFile.dispose();
				bgTexFile = null;
			}

			if (titleTexFile != null) {
				titleTexFile.dispose();
				titleTexFile = null;
			}

			if (music != null) {
				music.stop();
				music.dispose();
				music = null;
			}
		}
	}

	@Override
	public void show() {
		stage = new Stage(new ScreenViewport());

		final Skin skin = ui.getSkin();

		final MenuScreenStyle style = getStyle();
		final BitmapFont f = skin.get(style.textButtonStyle, TextButtonStyle.class).font;
		float buttonWidth = f.getCapHeight() * 15f;

		// Image background = new Image(style.background);
		Drawable bg = style.background;

		float scale = 1;

		if (bg == null && style.bgFile != null) {
			bgTexFile = new Texture(EngineAssetManager.getInstance().getResAsset(style.bgFile));
			bgTexFile.setFilter(TextureFilter.Linear, TextureFilter.Linear);

			scale = (float) bgTexFile.getHeight() / (float) stage.getViewport().getScreenHeight();

			int width = (int) (stage.getViewport().getScreenWidth() * scale);
			int x0 = (int) ((bgTexFile.getWidth() - width) / 2);

			bg = new TextureRegionDrawable(new TextureRegion(bgTexFile, x0, 0, width, bgTexFile.getHeight()));
		}

		menuButtonTable.clear();
		if (bg != null)
			menuButtonTable.setBackground(bg);

		menuButtonTable.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK)
					if (ui.getStoryManager().getStory() != null)
						ui.setCurrentScreen(Screens.STORY_SCREEN);

				return true;
			}
		});

		menuButtonTable.align(getAlign());
		menuButtonTable.pad(DPIUtils.getMarginSize() * 2);
		menuButtonTable.defaults().pad(DPIUtils.getSpacing()).width(buttonWidth).align(getAlign());
		// menuButtonTable.debug();

		stage.setKeyboardFocus(menuButtonTable);

		if (style.showTitle && style.titleStyle != null) {

			Label title = new Label(Config.getProperty(Config.TITLE_PROP, "Adventure Blade Engine"), skin,
					style.titleStyle);

			title.setAlignment(getAlign());

			menuButtonTable.add(title).padBottom(DPIUtils.getMarginSize() * 2);
			menuButtonTable.row();
		}

		if (style.titleFile != null) {
			titleTexFile = new Texture(EngineAssetManager.getInstance().getResAsset(style.titleFile));
			titleTexFile.setFilter(TextureFilter.Linear, TextureFilter.Linear);

			Image img = new Image(titleTexFile);
			
			float w = (float)titleTexFile.getWidth() / scale;
			float h = (float)titleTexFile.getHeight() / scale;
			
			if(w > stage.getViewport().getScreenWidth() - DPIUtils.getMarginSize() * 4) {
				w = stage.getViewport().getScreenWidth() - DPIUtils.getMarginSize() * 4;
				h = w *  (float)titleTexFile.getHeight() / (float)titleTexFile.getWidth();
			}
			
			menuButtonTable.add(img).width(w).height(h).padBottom(DPIUtils.getMarginSize() * 2);

			menuButtonTable.row();
		}

		if (ui.getStoryManager().savedGameExists() || ui.getStoryManager().getStory() != null) {
			TextButton continueGame = new TextButton(ui.translate("ui.continue"), skin, style.textButtonStyle);
			continueGame.getLabel().setAlignment(getAlign());

			continueGame.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					
					if(ui.getStoryManager().getStory() == null) {
						try {
							ui.getStoryManager().loadGameState();
						} catch (Exception e) {
							Gdx.app.error( TAG, "LOADING GAME STATE.", e);
							Gdx.app.exit();
						}
					}
					
					ui.setCurrentScreen(Screens.STORY_SCREEN);
				}
			});

			menuButtonTable.add(continueGame);
			menuButtonTable.row();
		}

		TextButton newGame = new TextButton(ui.translate("ui.new"), skin, style.textButtonStyle);
		newGame.getLabel().setAlignment(getAlign());
		newGame.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (ui.getStoryManager().savedGameExists()) {
					Dialog d = new Dialog("", skin) {
						protected void result(Object object) {
							if (((Boolean) object).booleanValue()) {
								try {
									ui.getStoryManager().newStory(Config.getProperty(Config.STORY, "story.ink.json"));
								} catch (Exception e) {
									Gdx.app.error( TAG, "IN NEW GAME", e);
									Gdx.app.exit();
								}
								
								ui.setCurrentScreen(Screens.STORY_SCREEN);
							}
						}
					};

					d.pad(DPIUtils.getMarginSize());
					d.getButtonTable().padTop(DPIUtils.getMarginSize());
					d.getButtonTable().defaults().padLeft(DPIUtils.getMarginSize()).padRight(DPIUtils.getMarginSize());

					Label l = new Label(ui.translate("ui.override"), ui.getSkin(), "ui-dialog");
					l.setWrap(true);
					l.setAlignment(Align.center);

					d.getContentTable().add(l).prefWidth(Gdx.graphics.getWidth() * .7f);

					d.button(ui.translate("ui.yes"), true, ui.getSkin().get("ui-dialog", TextButtonStyle.class));
					d.button(ui.translate("ui.no"), false, ui.getSkin().get("ui-dialog", TextButtonStyle.class));
					d.key(Keys.ENTER, true).key(Keys.ESCAPE, false);

					d.show(stage);
				} else {

					try {
						ui.getStoryManager().newStory(Config.getProperty(Config.STORY, "story.ink.json"));
					} catch (Exception e) {
						Gdx.app.error( TAG, "IN NEW GAME", e);
						Gdx.app.exit();
					}
					
					ui.setCurrentScreen(Screens.STORY_SCREEN);
				}
			}
		});

		menuButtonTable.add(newGame);
		menuButtonTable.row();

		TextButton quit = new TextButton(ui.translate("ui.quit"), skin, style.textButtonStyle);
		quit.getLabel().setAlignment(getAlign());
		quit.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});

		// Exit button on only on desktop
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			menuButtonTable.add(quit);
			menuButtonTable.row();
		}

		menuButtonTable.pack();

		stage.addActor(menuButtonTable);

		// BOTTOM-RIGHT BUTTON STACK
		credits = new Button(skin, "credits");
		credits.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				ui.setCurrentScreen(Screens.CREDIT_SCREEN);
			}
		});

		debug = new Button(skin, "debug");
		debug.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				DebugScreen debugScr = new DebugScreen();
				debugScr.setUI(ui);
				ui.setCurrentScreen(debugScr);
			}
		});

		iconStackTable.clear();
		iconStackTable.defaults().pad(DPIUtils.getSpacing()).size(DPIUtils.getPrefButtonSize(),
				DPIUtils.getPrefButtonSize());
		iconStackTable.pad(DPIUtils.getMarginSize() * 2);

		/*
		 * FIXME Debug screen if (EngineLogger.debugMode() &&
		 * world.getCurrentScene() != null) { iconStackTable.add(debug);
		 * iconStackTable.row(); }
		 */

		iconStackTable.add(credits);
		iconStackTable.bottom().right();
		iconStackTable.setFillParent(true);
		iconStackTable.pack();
		stage.addActor(iconStackTable);

		Label version = new Label("v" + Config.getProperty(Config.VERSION_PROP, " unspecified"), skin);
		version.setPosition(DPIUtils.getMarginSize(), DPIUtils.getMarginSize());
		version.addListener(new ClickListener() {
			int count = 0;
			long time = System.currentTimeMillis();

			public void clicked(InputEvent event, float x, float y) {
				if (System.currentTimeMillis() - time < 500) {
					count++;
				} else {
					count = 0;
				}

				time = System.currentTimeMillis();

				if (count == 4) {
					/*
					 * FIXME EngineLogger.toggle();
					 * 
					 * if (World.getInstance().isDisposed()) return;
					 * 
					 * if (EngineLogger.debugMode()) { iconStackTable.row();
					 * iconStackTable.add(debug); } else { Cell<?> cell =
					 * iconStackTable.getCell(debug);
					 * iconStackTable.removeActor(debug); cell.reset(); }
					 */
				}
			}
		});

		stage.addActor(version);

		debug.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				DebugScreen debugScr = new DebugScreen();
				debugScr.setUI(ui);
				ui.setCurrentScreen(debugScr);
			}
		});

		Gdx.input.setInputProcessor(stage);

		if (style.musicFile != null) {
			music = Gdx.audio.newMusic(EngineAssetManager.getInstance().getAsset(style.musicFile));
			music.setLooping(true);
			music.play();
		}
	}

	protected Table getMenuButtonTable() {
		return menuButtonTable;
	}

	protected Table getIconStackTable() {
		return iconStackTable;
	}

	protected UI getUI() {
		return ui;
	}

	protected MenuScreenStyle getStyle() {
		return ui.getSkin().get(MenuScreenStyle.class);
	}

	private int getAlign() {
		if (getStyle().align == null || "center".equals(getStyle().align))
			return Align.center;

		if ("top".equals(getStyle().align))
			return Align.top;

		if ("bottom".equals(getStyle().align))
			return Align.bottom;

		if ("left".equals(getStyle().align))
			return Align.left;

		if ("right".equals(getStyle().align))
			return Align.right;

		return Align.center;
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void setUI(UI ui) {
		this.ui = ui;

		menuButtonTable.setFillParent(true);
		menuButtonTable.center();
	}

	/** The style for the MenuScreen */
	public static class MenuScreenStyle {
		/** Optional. */
		public Drawable background;
		/** if 'bg' not specified try to load the bgFile */
		public String bgFile;
		public String titleFile;
		public String textButtonStyle;
		public String titleStyle;
		public boolean showTitle;
		public String musicFile;
		public String align;

		public MenuScreenStyle() {
		}

		public MenuScreenStyle(MenuScreenStyle style) {
			background = style.background;
			bgFile = style.bgFile;
			titleFile = style.titleFile;
			textButtonStyle = style.textButtonStyle;
			showTitle = style.showTitle;
			titleStyle = style.titleStyle;
			musicFile = style.musicFile;
			align = style.align;
		}
	}
}
