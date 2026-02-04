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

public class DifficultySelectScreen implements Screen {
    private final Main game;
    private final GameConfig.GameMode mode;
    private final long seed;
    private Stage stage;
    private Skin skin;

    private static final String[] DIFFICULTY_NAMES = {
        "1 Suit", "2 Suits", "3 Suits", "4 Suits", "5 Suits", "6 Suits"
    };

    private static final String[] DIFFICULTY_LABELS = {
        "Easy", "Medium", "Hard", "Expert", "Master", "Impossible"
    };

    public DifficultySelectScreen(Main game, GameConfig.GameMode mode) {
        this(game, mode, System.currentTimeMillis());
    }

    public DifficultySelectScreen(Main game, GameConfig.GameMode mode, long seed) {
        this.game = game;
        this.mode = mode;
        this.seed = seed;
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

        // Content table centered within safe area
        Table table = new Table();
        table.center();
        rootTable.add(table).expand().fill()
            .padTop(SafeAreaHelper.getTopInset())
            .padBottom(SafeAreaHelper.getBottomInset())
            .padLeft(SafeAreaHelper.getLeftInset())
            .padRight(SafeAreaHelper.getRightInset());

        // Calculate sizes based on screen dimensions
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float buttonWidth = screenWidth * 0.44f;  // ~44% width for 2 columns with gap
        float buttonHeight = screenHeight * 0.11f;  // 11% of screen height
        float padding = screenHeight * 0.012f;
        float titlePadding = screenHeight * 0.03f;

        // Title
        Label titleLabel = new Label("Select Difficulty", skin, "title");
        table.add(titleLabel).colspan(2).padBottom(titlePadding);
        table.row();

        // Difficulty buttons in 3 rows x 2 columns
        for (int i = 0; i < 6; i++) {
            final int numSuits = i + 1;
            String buttonText = DIFFICULTY_NAMES[i] + "\n" + DIFFICULTY_LABELS[i];
            TextButton button = new TextButton(buttonText, skin);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    GameConfig config;
                    if (mode == GameConfig.GameMode.DAILY_GRIND) {
                        config = GameConfig.dailyGrind(numSuits, seed);
                    } else {
                        config = GameConfig.soloPractice(numSuits);
                    }
                    game.setScreen(new GameScreen(game, config));
                    dispose();
                }
            });
            table.add(button).width(buttonWidth).height(buttonHeight).pad(padding);

            // New row after every 2 buttons
            if (i % 2 == 1) {
                table.row();
            }
        }

        // Back button
        table.row();
        TextButton backButton = new TextButton("Back", skin, "secondary");
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        float backWidth = screenWidth * 0.5f;
        float backHeight = screenHeight * 0.09f;
        table.add(backButton).colspan(2).width(backWidth).height(backHeight).padTop(titlePadding * 1.5f);
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        // Scale fonts based on screen density
        float density = Gdx.graphics.getDensity();
        float fontScale = Math.max(1.6f, density * 1.3f);  // Button text
        float titleScale = Math.max(2.2f, density * 1.8f); // Title
        float smallFontScale = Math.max(1.4f, density * 1.1f); // Back button

        BitmapFont font = new BitmapFont();
        font.getData().setScale(fontScale);
        skin.add("default-font", font);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(titleScale);
        skin.add("title-font", titleFont);

        BitmapFont smallFont = new BitmapFont();
        smallFont.getData().setScale(smallFontScale);
        skin.add("small-font", smallFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        // Primary button style (difficulty buttons)
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0.25f, 0.45f, 0.35f, 1f));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.15f, 0.35f, 0.25f, 1f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.3f, 0.5f, 0.4f, 1f));
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        // Secondary button style (back button)
        TextButton.TextButtonStyle secondaryStyle = new TextButton.TextButtonStyle();
        secondaryStyle.up = skin.newDrawable("white", new Color(0.35f, 0.35f, 0.4f, 1f));
        secondaryStyle.down = skin.newDrawable("white", new Color(0.25f, 0.25f, 0.3f, 1f));
        secondaryStyle.over = skin.newDrawable("white", new Color(0.4f, 0.4f, 0.45f, 1f));
        secondaryStyle.font = smallFont;
        secondaryStyle.fontColor = Color.WHITE;
        skin.add("secondary", secondaryStyle);

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
        ScreenUtils.clear(0.1f, 0.3f, 0.2f, 1f);
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
