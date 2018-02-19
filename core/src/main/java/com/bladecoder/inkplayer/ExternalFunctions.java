package com.bladecoder.inkplayer;

import com.badlogic.gdx.Gdx;
import com.bladecoder.ink.runtime.Story.ExternalFunction;

public class ExternalFunctions {

	private StoryManager inkManager;

	public ExternalFunctions() {
	}

	public void bindExternalFunctions(StoryManager ink) throws Exception {

		this.inkManager = ink;

		inkManager.getStory().bindExternalFunction("getModelProp", new ExternalFunction() {

			@Override
			public Object call(Object[] args) throws Exception {
				try {
					String p = args[0].toString();
					
					return p;
				} catch (Exception e) {
					Gdx.app.error( InkApp.LOG_TAG, "Ink getModelProp: " + e.getMessage(), e);
				}

				return null;
			}
		});
	}
}
