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
package com.bladecoder.inkplayer.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import com.badlogic.gdx.Gdx;
import com.bladecoder.inkplayer.InkApp;

public class I18N {
	public static final char PREFIX = '@';
	public static final String ENCODING = "UTF-8";

	private static Locale locale = Locale.getDefault();
	
	public static ResourceBundle getBundle(String filename, boolean clearCache) {
		ResourceBundle rb = null;
		
		try {
			if(clearCache)
				ResourceBundle.clearCache();
			
			rb = ResourceBundle.getBundle(filename, locale, new I18NControl(ENCODING));
		} catch (Exception e) {
			Gdx.app.error( InkApp.LOG_TAG, "ERROR LOADING BUNDLE: " + filename);
		}
		
		return rb;
	}

	public static void setLocale(Locale l) {
		locale = l;
	}	
	
	public static Locale getCurrentLocale() {
		return locale;
	}
}
