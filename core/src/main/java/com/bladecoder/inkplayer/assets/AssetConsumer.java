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
package com.bladecoder.inkplayer.assets;

import com.badlogic.gdx.utils.Disposable;

public interface AssetConsumer extends Disposable {
	/**
	 * Send the assets to the AssetManager queue to load asynchronous
	 */
	public void loadAssets();

	/**
	 * Called when the AssetManager has loaded all the assets and can be retrieved.
	 */
	public void retrieveAssets();
}
