package ape.spider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = createBasicSkin();

        // Create root table with safe area padding
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        // Content table that will be centered within safe area
        Table table = new Table();
        table.center();
        rootTable.add(table).expand().fill()
            .padTop(SafeAreaHelper.getTopInset())
            .padBottom(SafeAreaHelper.getBottomInset())
            .padLeft(SafeAreaHelper.getLeftInset())
            .padRight(SafeAreaHelper.getRightInset());

        TextButton soloPracticeButton = new TextButton("Solo Practice", skin);
        TextButton dailyGrindButton = new TextButton("Daily Grind", skin);
        TextButton infoButton = new TextButton("Info", skin);

        soloPracticeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new DifficultySelectScreen(game, GameConfig.GameMode.SOLO_PRACTICE));
                dispose();
            }
        });

        dailyGrindButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                long dailySeed = getDailySeed();
                game.setScreen(new DifficultySelectScreen(game, GameConfig.GameMode.DAILY_GRIND, dailySeed));
                dispose();
            }
        });

        infoButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new InfoScreen(game));
                dispose();
            }
        });

        // Calculate sizes based on screen dimensions for balanced feel
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float buttonWidth = screenWidth * 0.85f;  // 85% of screen width
        float buttonHeight = screenHeight * 0.12f;  // 12% of screen height - generous touch target
        float padding = screenHeight * 0.02f;  // 2% padding
        float titlePadding = screenHeight * 0.05f;  // 5% below title

        // Add title
        Label titleLabel = new Label("SPIDER SOLITAIRE", skin, "title");
        table.add(titleLabel).padBottom(titlePadding);
        table.row();

        table.add(soloPracticeButton).width(buttonWidth).height(buttonHeight).pad(padding);
        table.row();
        table.add(dailyGrindButton).width(buttonWidth).height(buttonHeight).pad(padding);
        table.row();
        table.add(infoButton).width(buttonWidth).height(buttonHeight).pad(padding);
    }

    private long getDailySeed() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH);
        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);
        return year * 10000L + month * 100L + day;
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        // Scale fonts based on screen density for crisp text on all devices
        float density = Gdx.graphics.getDensity();
        float fontScale = Math.max(1.8f, density * 1.5f);  // Button text
        float titleScale = Math.max(2.5f, density * 2.0f); // Title - not too big

        // Button font
        BitmapFont font = new BitmapFont();
        font.getData().setScale(fontScale);
        skin.add("default-font", font);

        // Title font
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(titleScale);
        skin.add("title-font", titleFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0.25f, 0.45f, 0.35f, 1f));  // Muted green
        textButtonStyle.down = skin.newDrawable("white", new Color(0.15f, 0.35f, 0.25f, 1f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.3f, 0.5f, 0.4f, 1f));
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = Color.WHITE;
        skin.add("title", titleStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        return skin;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.3f, 0.2f, 1f);  // Dark green background
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
