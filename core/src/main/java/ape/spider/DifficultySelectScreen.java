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
        "1 - Easy",
        "2 - Medium",
        "3 - Hard",
        "4 - Expert",
        "5 - Master",
        "6 - Impossible"
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

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Title
        Label titleLabel = new Label("Select Difficulty", skin, "title");
        table.add(titleLabel).colspan(2).padBottom(40f);
        table.row();

        // Difficulty buttons in 3 rows x 2 columns
        float buttonWidth = 240f;
        float buttonHeight = 48f;
        float padding = 8f;

        for (int i = 0; i < 6; i++) {
            final int numSuits = i + 1;
            TextButton button = new TextButton(DIFFICULTY_NAMES[i], skin);
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
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).colspan(2).width(160f).height(40f).padTop(24f);
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        BitmapFont font = new BitmapFont();
        font.getData().setScale(2.0f);
        skin.add("default-font", font);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(3.0f);
        skin.add("title-font", titleFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.4f, 1f));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 1f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.4f, 0.4f, 0.5f, 1f));
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
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        skin.dispose();
    }
}
