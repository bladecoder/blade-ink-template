package com.bladecoder.inkplayer.ui;

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
import com.badlogic.gdx.utils.ObjectMap;
import com.bladecoder.inkplayer.Line;
import com.bladecoder.inkplayer.StoryManager;
import com.bladecoder.inkplayer.common.DPIUtils;

/**
 * The TextPanel is a table with a label per row
 * 
 * @author rgarcia
 */
public class TextPanel extends ScrollPane {
	private static final float DEFAULT_WAITING_TIME = 1f;

	private final String STYLE_PARAM = "style";
	private final String COLOR_PARAM = "color";

	private TextPanelStyle style;
	private ObjectMap<String, LabelStyle> labelStyles;
	private Table panel;

	private StoryManager sm;

	public TextPanel(UI ui) {
		super(new Table(ui.getSkin()));

		Skin skin = ui.getSkin();
		sm = ui.getStoryManager();

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
		if (height > width)
			setSize(width * (DPIUtils.getSizeMultiplier() < 1.2 ? 0.9f : 0.7f), height * 0.75f); // portrait
		else
			setSize(width * (DPIUtils.getSizeMultiplier() < 1.2 ? 0.8f : 0.6f), height * 0.9f); // landscape

		setPosition((width - getWidth()) / 2, height - getHeight());

		panel.clearChildren();

		// refill panel
		for (Line l : sm.getRecod())
			addLine(l, null);
	}

	public void hide() {
		addAction(Actions.sequence(Actions.alpha(0, 0.7f), Actions.removeActor()));
	}

	public Drawable getBackground() {
		return style.background;
	}

	public void addLine(Line line, Runnable runWhenEnds) {

		float margin = DPIUtils.getSpacing() * 4;

		Label l = new Label(line.text, line.params.get(STYLE_PARAM) == null ? style.labelStyle
				: labelStyles.get(line.params.get(STYLE_PARAM)));
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
		panel.invalidateHierarchy();
		panel.pack();
		layout();
		setScrollPercentY(1);

		if (runWhenEnds != null) {
			addAction(Actions.sequence(Actions.delay(calcWaitingTime(line.text)), Actions.run(runWhenEnds)));
		}
	}

	private float calcWaitingTime(String text) {
		return DEFAULT_WAITING_TIME + DEFAULT_WAITING_TIME * text.length() / 20f;
	}

	public void clearPanel() {
		panel.clear();
	}

	public void addLines(List<Line> l) {
		for (Line t : l)
			addLine(t, null);
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
