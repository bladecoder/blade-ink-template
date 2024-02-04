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

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.bladecoder.inkplayer.common.DPIUtils;

public class ChoicesUI extends ScrollPane {
	public static final String DIALOG_END_COMMAND = "dialog_end";

	private final ChoicesUIStyle style;

	// private Recorder recorder;

	private final Table panel;

	private final Button up;
	private final Button down;

	private final StoryScreen sc;

	public ChoicesUI(StoryScreen sc) {
		super(new Table(sc.getUI().getSkin()), sc.getUI().getSkin());

		UI ui = sc.getUI();
		this.sc = sc;
		setFadeScrollBars(true);
		setOverscroll(false, false);

		up = new Button(ui.getSkin(), "dialog-up");
		down = new Button(ui.getSkin(), "dialog-down");

		panel = (Table) getActor();
		style = ui.getSkin().get(ChoicesUIStyle.class);
		// this.recorder = ui.getRecorder();

		if (style.background != null)
			panel.setBackground(style.background);

		panel.top().left();
		panel.pad(DPIUtils.getMarginSize());

		setVisible(false);
		panel.defaults().expandX().fillX().top().left().padBottom(DPIUtils.getSpacing());

		addListener(new EventListener() {

			@Override
			public boolean handle(Event event) {
				if (isScrollY()) {

					if (getScrollPercentY() > 0f && !up.isVisible()) {
						up.setVisible(true);
					} else if (getScrollPercentY() == 0f && up.isVisible()) {
						up.setVisible(false);
					}

					if (getScrollPercentY() < 1f && !down.isVisible()) {
						down.setVisible(true);
					} else if (getScrollPercentY() == 1f && down.isVisible()) {
						down.setVisible(false);
					}
				}

				return false;
			}
		});

		up.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setScrollY(getScrollY() - DPIUtils.getPrefButtonSize());
			}
		});

		down.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				setScrollY(getScrollY() + DPIUtils.getPrefButtonSize());
			}
		});
	}

	public void show(List<String> choices) {
		setVisible(true);

		if (choices.isEmpty()) {
            setVisible(false);
            return;

        } else if (style.autoselect && choices.size() == 1) {
			// If only has one option, autoselect it
			select(0);
            setVisible(false);
			return;
		}

		panel.clear();
        setScrollPercentY(0);

		for (int i = 0; i < choices.size(); i++) {
			String str = choices.get(i);

			TextButton ob = new TextButton(str, style.textButtonStyle);
			ob.setUserObject(i);
			panel.row();
			panel.add(ob);
			ob.getLabel().setWrap(true);
			ob.getLabel().setAlignment(Align.left);

			ob.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					int i = (Integer) event.getListenerActor().getUserObject();

					select(i);
				}
			});
		}

        setWidth(getStage().getViewport().getScreenWidth());
        setHeight(Math.min(panel.getPrefHeight(), getStage().getViewport().getScreenHeight() / 2f));

        float size = DPIUtils.getPrefButtonSize() * .8f;
        float margin = DPIUtils.getSpacing();

        getStage().addActor(up);
        up.setSize(size, size);
        up.setPosition(getX() + getWidth() - size - margin, getY() + getHeight() - margin - size);

        getStage().addActor(down);
        down.setSize(size, size);
        down.setPosition(getX() + getWidth() - size - margin, getY() + margin);

        layout();

        setUpDownVisibility();

        if (down.isVisible()) {
            down.addAction(
                Actions.repeat(3, Actions.sequence(Actions.moveBy(0, 15, .08f), Actions.moveBy(0, -15, .08f))));
        }

        getStage().setScrollFocus(this);
	}

    public void setUpDownVisibility() {
        if (isScrollY() && getMaxY() > DPIUtils.getMarginSize()) {

            if (getScrollPercentY() > 0f && !up.isVisible()) {
                up.setVisible(true);
            } else if (getScrollPercentY() == 0f && up.isVisible()) {
                up.setVisible(false);
            }

            if (getScrollPercentY() < 1f && !down.isVisible()) {
                down.setVisible(true);
            } else if (getScrollPercentY() == 1f && down.isVisible()) {
                down.setVisible(false);
            }
        } else {
            up.setVisible(false);
            down.setVisible(false);
        }
    }

	public void resize(int width, int height) {
		setWidth(width);
		setHeight(Math.min(panel.getHeight(), height / 2f));

		float size = DPIUtils.getPrefButtonSize() * .8f;
		float margin = DPIUtils.getSpacing();

		up.setSize(size, size);
		up.setPosition(getX() + getWidth() - size - margin, getY() + getHeight() - margin - size);

		down.setSize(size, size);
		down.setPosition(getX() + getWidth() - size - margin, getY() + margin);

	}

	private void select(final int i) {
		// RECORD
		/*
		 * if (recorder.isRecording()) { recorder.add(i); }
		 */

		up.remove();
		down.remove();

		addAction(Actions.sequence(Actions.fadeOut(1f), Actions.run(new Runnable() {

			@Override
			public void run() {
				setVisible(false);
				setColor(Color.WHITE);
				sc.selectChoice(i);
			}
		})));

	}

	/** The style for the DialogUI */
	static public class ChoicesUIStyle {
		/** Optional. */
		public Drawable background;

		public TextButtonStyle textButtonStyle;

		// If only one option is visible, auto select it.
		public boolean autoselect = true;

		public ChoicesUIStyle() {
		}

		public ChoicesUIStyle(ChoicesUIStyle style) {
			background = style.background;
			textButtonStyle = style.textButtonStyle;
			autoselect = style.autoselect;
		}
	}
}
