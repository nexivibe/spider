package ape.spider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class SplashScreen implements Screen {
    private final Main game;
    private final Texture logo;
    private final ShapeRenderer shapeRenderer;
    private float elapsedTime;

    private static final float OUTER_MARGIN = 5f;  // Black margin from screen edge
    private static final float BORDER_WIDTH = 2f;  // White border thickness

    public SplashScreen(Main game) {
        this.game = game;
        this.logo = new Texture("logo.png");
        this.shapeRenderer = new ShapeRenderer();
        this.elapsedTime = 0f;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        // Black background
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Account for safe areas on mobile devices
        float safeTop = SafeAreaHelper.getTopInset();
        float safeBottom = SafeAreaHelper.getBottomInset();
        float safeLeft = SafeAreaHelper.getLeftInset();
        float safeRight = SafeAreaHelper.getRightInset();

        float safeWidth = screenWidth - safeLeft - safeRight;
        float safeHeight = screenHeight - safeTop - safeBottom;

        // Calculate available space for image (after margins and border)
        float availableSize = Math.min(safeWidth, safeHeight) - (OUTER_MARGIN + BORDER_WIDTH) * 2;

        // Calculate scaled logo dimensions preserving aspect ratio
        float logoAspect = (float) logo.getWidth() / logo.getHeight();
        float drawWidth, drawHeight;

        if (logoAspect > 1) {
            // Wider than tall
            drawWidth = availableSize;
            drawHeight = availableSize / logoAspect;
        } else {
            // Taller than wide
            drawHeight = availableSize;
            drawWidth = availableSize * logoAspect;
        }

        // Center the logo
        float logoX = (screenWidth - drawWidth) / 2f;
        float logoY = (screenHeight - drawHeight) / 2f;

        // Draw white border (rectangle behind the image)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
            logoX - BORDER_WIDTH,
            logoY - BORDER_WIDTH,
            drawWidth + BORDER_WIDTH * 2,
            drawHeight + BORDER_WIDTH * 2
        );
        shapeRenderer.end();

        // Draw the logo image
        game.getBatch().begin();
        game.getBatch().draw(logo, logoX, logoY, drawWidth, drawHeight);
        game.getBatch().end();

        if (elapsedTime >= 1.5f) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        logo.dispose();
        shapeRenderer.dispose();
    }
}
