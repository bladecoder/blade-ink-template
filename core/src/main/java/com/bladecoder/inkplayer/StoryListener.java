package com.bladecoder.inkplayer;

import java.util.HashMap;
import java.util.List;

public interface StoryListener {
	public void line(Line l);
	public void command(String name, HashMap<String, String> params);
	public void choices(List<String> choices);
	public void end();
}
