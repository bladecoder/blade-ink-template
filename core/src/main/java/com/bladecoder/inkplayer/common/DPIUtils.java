package com.bladecoder.inkplayer.common;

import com.badlogic.gdx.Gdx;

public class DPIUtils {
	public final static float BASE_DPI = 160.0f;
	
	/**
	 * Current DPI
	 */
	public final static float DPI = BASE_DPI * Gdx.graphics.getDensity();

	/**
	 * The Google recommendations are 48 dp -> 9mm for touchable elements
	 */
	public final static float TOUCH_MIN_SIZE = 48 * Gdx.graphics.getDensity();

	/**
	 * The Google recommendations of space between UI objects is 8 dp
	 */
	public final static float UI_SPACE = 8 * Gdx.graphics.getDensity();

	/**
	 * The Google recommendations of space from bottom or top is 16 dp
	 */
	public final static float MARGIN_SIZE = 16 * Gdx.graphics.getDensity();

	/**
	 * The Google recommendations are 56 dp for action buttons
	 */
	public final static float BUTTON_SIZE = 56 * Gdx.graphics.getDensity();
	
	/**
	 * The Google recommendations are 24 dp for icons inside action buttons
	 */
	public final static float ICON_SIZE = 24 * Gdx.graphics.getDensity();

	
	/**
	 * The Google recommendations are 8 dp for space between ui elements
	 */
	public final static float SPACING = 8 * Gdx.graphics.getDensity();

	/**
	 * The screen height in DP
	 */
	public final static float SCREEN_HEIGHT_DP = Gdx.graphics.getHeight()
			/ Gdx.graphics.getDensity();

	public final static float NORMAL_MULTIPLIER = 1.0f; // 3-5"
	public final static float LARGE_MULTIPLIER = 1.5f; // 5-7"
	public final static float XLARGE_MULTIPLIER = 2f; // 8-10"
	public final static float XXLARGE_MULTIPLIER = 2.5f; // > 10"

	/**
	 * Calcs the button size based in screen size
	 *
	 * @return The recommended size in pixels
	 */
	public static float getPrefButtonSize() {
		return getSizeMultiplier() * BUTTON_SIZE;
	}
	
	/**
	 * Calcs the minimum size based in screen size
	 *
	 * @return The recommended size in pixels
	 */
	public static float getTouchMinSize() {
		return getSizeMultiplier() * TOUCH_MIN_SIZE;
	}
	
	/**
	 * Calcs the margin size based in screen size
	 *
	 * @return The recommended size in pixels
	 */
	public static float getMarginSize() {
		return getSizeMultiplier() * MARGIN_SIZE;
	}
	
	/**
	 * Calcs the space between ui elements based in screen size
	 *
	 * @return The recommended size in pixels
	 */
	public static float getSpacing() {
		return getSizeMultiplier() * SPACING;
	}
	
	public static float getSizeMultiplier() {
		int p = Gdx.graphics.getWidth() > Gdx.graphics.getHeight()? Gdx.graphics.getWidth():Gdx.graphics.getHeight();
		
		float inches = pixelsToInches(p);
		float s = inches / 6f;

		return Math.max(1.0f, s);

	}

	public static int dpToPixels(int dp) {
		return (int) (dp * Gdx.graphics.getDensity());
	}
	
	public static int pixelsToDP(int pixels) {
		return (int) (pixels / Gdx.graphics.getDensity());
	}
	
	public static float pixelsToInches(int pixels) {
		return (float)pixels / DPI;
	}
	
	public static float ptToPixels(float pts) {
		return pts * 72 / DPI;
	}
}
