package com.bladecoder.inkplayer.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.bladecoder.inkplayer.util.DPIUtils;

/**
 * The TextPanel is a table with a label per row
 * 
 * @author rgarcia
 */
public class TextPanel extends ScrollPane implements Serializable {
	private static final float DEFAULT_WAITING_TIME = 1f;
	
	private final String STYLE_PARAM = "style";
	private final String COLOR_PARAM = "color";
	
	private TextPanelStyle style;
	private ObjectMap<String, LabelStyle> labelStyles;
	private Table panel;

	// save all texts in this list
	private final List<Line> history = new ArrayList<Line>();

	public TextPanel(UI ui) {
		super(new Table(ui.getSkin()));
		
		Skin skin = ui.getSkin();

		style = skin.get(TextPanelStyle.class);
		labelStyles = skin.getAll(LabelStyle.class);

		setScrollingDisabled(true, false);

		panel = (Table) getActor();

		if (style.background != null)
			panel.setBackground(style.background);

		// panel.debug();
		panel.defaults().expandX().pad(DPIUtils.getSpacing());
		panel.bottom().left();
		
		setVisible(false);
	}

	public void show() {
		setVisible(true);
		
		resize(getStage().getViewport().getScreenWidth(), getStage().getViewport().getScreenHeight());

		addAction(Actions.sequence(Actions.alpha(0), Actions.alpha(1, 0.7f)));
	}
	
	public void resize(int width, int height) {
		setSize(width * (DPIUtils.getSizeMultiplier() < 1.2 ? 0.8f : 0.6f),
				height * 0.9f);
		setPosition((width - getWidth()) / 2,
				(height - getHeight()) / 2);
		
		panel.clearChildren();
		for(Line l: history)
			addLine(l);
		setScrollPercentY(1);
	}

	public void hide() {
		addAction(Actions.sequence(Actions.alpha(0, 0.7f), Actions.removeActor()));
	}

	public Drawable getBackground() {
		return style.background;
	}

	public void addText(Line line, Runnable runWhenEnds) {
		
		addLine(line);
		history.add(line);
		setScrollPercentY(1);
		
		if(runWhenEnds != null)
			addAction(Actions.sequence(Actions.delay(calcWaitingTime(line.text)), Actions.run(runWhenEnds)));
	}
	
	private void addLine(Line line) {
		float margin = DPIUtils.getSpacing() * 4;
		
		Label l = new Label(line.text, line.params.get(STYLE_PARAM) == null ? style.labelStyle : labelStyles.get(line.params.get(STYLE_PARAM)));
		Table tl = new Table();
		tl.defaults().pad(DPIUtils.getSpacing(), margin, DPIUtils.getSpacing(), margin);
		tl.setBackground(l.getStyle().background);

		if (line.params.get(COLOR_PARAM) != null)
			l.setColor(Color.valueOf(line.params.get(COLOR_PARAM)));

		if (line.params.get(STYLE_PARAM) == null) {
			l.setAlignment(Align.left);
		}

		int align = Align.left;

		float maxLabelSize = getWidth() - margin * 2;
		Cell<Label> cell = tl.add(l);

		panel.add(tl).align(align);

		if (l.getPrefWidth() > maxLabelSize) {
			l.setWrap(true);
			cell.width(maxLabelSize);
		}

		panel.row();
		panel.pack();
		panel.layout();
		layout();
	}
	
	private float calcWaitingTime(String text) {
		return DEFAULT_WAITING_TIME + DEFAULT_WAITING_TIME * text.length() / 20f;
	}

	public List<Line> getTexts() {
		return history;
	}

	public void clearPanel() {
		panel.clear();
		history.clear();
	}

	public void loadTexts(List<Line> l) {
		for (Line t : l)
			addText(t, null);

		history.addAll(l);
	}

	@Override
	public void write(Json json) {
		json.writeValue("texts", getTexts(), ArrayList.class, Line.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Json json, JsonValue jsonData) {

		List<Line> texts = json.readValue("texts", ArrayList.class, Line.class, jsonData);

		if (texts != null) {
			loadTexts(texts);
		}
	}

	static public class TextPanelStyle {
		/** Optional. */
		public Drawable background;
		public LabelStyle labelStyle;

		public TextPanelStyle() {
		}

		public TextPanelStyle(TextPanelStyle style) {
			background = style.background;
			labelStyle = style.labelStyle;
		}
	}
}
