package ape.spider;

import com.badlogic.gdx.Gdx;

/**
 * Helper class to get safe area insets for different platforms.
 * Handles notches, rounded corners, status bars, and navigation bars.
 */
public class SafeAreaHelper {

    // Minimum safe padding even when system reports 0 (for older devices/desktop)
    private static final int MIN_TOP_PADDING = 10;
    private static final int MIN_BOTTOM_PADDING = 10;
    private static final int MIN_SIDE_PADDING = 5;

    /**
     * Get the safe area inset from the top of the screen.
     * Accounts for notches, status bars, and rounded corners.
     */
    public static int getTopInset() {
        int systemInset = Gdx.graphics.getSafeInsetTop();
        return Math.max(systemInset, MIN_TOP_PADDING);
    }

    /**
     * Get the safe area inset from the bottom of the screen.
     * Accounts for home indicators, navigation bars, and rounded corners.
     */
    public static int getBottomInset() {
        int systemInset = Gdx.graphics.getSafeInsetBottom();
        return Math.max(systemInset, MIN_BOTTOM_PADDING);
    }

    /**
     * Get the safe area inset from the left of the screen.
     * Accounts for side notches (landscape mode) and rounded corners.
     */
    public static int getLeftInset() {
        int systemInset = Gdx.graphics.getSafeInsetLeft();
        return Math.max(systemInset, MIN_SIDE_PADDING);
    }

    /**
     * Get the safe area inset from the right of the screen.
     * Accounts for side notches (landscape mode) and rounded corners.
     */
    public static int getRightInset() {
        int systemInset = Gdx.graphics.getSafeInsetRight();
        return Math.max(systemInset, MIN_SIDE_PADDING);
    }

    /**
     * Check if we're running on a mobile device (Android or iOS).
     */
    public static boolean isMobile() {
        return Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android ||
               Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.iOS;
    }

    /**
     * Get recommended minimum touch target size in pixels.
     * Apple recommends 44pt, Android recommends 48dp.
     */
    public static float getMinTouchTargetSize() {
        if (isMobile()) {
            // Use density to calculate appropriate touch size
            float density = Gdx.graphics.getDensity();
            return 48f * density; // 48dp for Android, roughly similar for iOS
        }
        return 40f; // Desktop can have smaller targets
    }

    /**
     * Scale a value based on screen density for consistent physical size across devices.
     */
    public static float scaleForDensity(float baseValue) {
        if (isMobile()) {
            float density = Gdx.graphics.getDensity();
            return baseValue * Math.max(1f, density);
        }
        return baseValue;
    }
}
