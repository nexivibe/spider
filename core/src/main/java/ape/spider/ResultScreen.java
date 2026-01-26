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

public class ResultScreen implements Screen {
    private final Main game;
    private final GameResult result;
    private final GameResult priorResult; // For retry comparison (null if first attempt)
    private Stage stage;
    private Skin skin;

    public ResultScreen(Main game, GameResult result) {
        this(game, result, null);
    }

    public ResultScreen(Main game, GameResult result, GameResult priorResult) {
        this.game = game;
        this.result = result;
        this.priorResult = priorResult;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = createBasicSkin();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Title based on outcome
        String titleText = result.getOutcome() == GameResult.Outcome.WON ? "VICTORY!" : "Game Over";
        Label titleLabel = new Label(titleText, skin, "title");
        if (result.getOutcome() == GameResult.Outcome.WON) {
            titleLabel.setColor(new Color(0.2f, 1f, 0.2f, 1f)); // Green for win
        }
        table.add(titleLabel).colspan(2).padBottom(40f);
        table.row();

        // Stats with optional comparison
        addStatRow(table, "Final Score", result.getScore(),
            priorResult != null ? priorResult.getScore() : null, true); // Higher is better
        addTimeStatRow(table, "Time", result.getTimeSeconds(), result.getFormattedTime(),
            priorResult != null ? priorResult.getTimeSeconds() : null,
            priorResult != null ? priorResult.getFormattedTime() : null);
        addStatRow(table, "Moves", result.getMoves(),
            priorResult != null ? priorResult.getMoves() : null, false); // Lower is better
        addStatRow(table, "Undos", result.getUndos(),
            priorResult != null ? priorResult.getUndos() : null, false); // Lower is better

        // Suits completed (special formatting)
        String suitsText = result.getCompletedSuits() + "/" + result.getRequiredSuits();
        Label suitsNameLabel = new Label("Suits Completed:", skin, "stats");
        Label suitsValueLabel = new Label(suitsText, skin, "stats");
        if (priorResult != null) {
            int diff = result.getCompletedSuits() - priorResult.getCompletedSuits();
            if (diff != 0) {
                String diffStr = " (" + (diff > 0 ? "+" : "") + diff + ")";
                Label diffLabel = new Label(diffStr, skin, "stats");
                diffLabel.setColor(diff > 0 ? Color.GREEN : Color.RED);
                table.add(suitsNameLabel).right().padRight(10f).padBottom(10f);
                Table valueTable = new Table();
                valueTable.add(suitsValueLabel);
                valueTable.add(diffLabel);
                table.add(valueTable).left().padBottom(10f);
            } else {
                table.add(suitsNameLabel).right().padRight(10f).padBottom(10f);
                table.add(suitsValueLabel).left().padBottom(10f);
            }
        } else {
            table.add(suitsNameLabel).right().padRight(10f).padBottom(10f);
            table.add(suitsValueLabel).left().padBottom(10f);
        }
        table.row();

        // Buttons (15% smaller, horizontal layout)
        table.row();
        float buttonWidth = 170f;  // 15% smaller than 200
        float buttonHeight = 51f;  // 15% smaller than 60

        // Create horizontal button table
        Table buttonTable = new Table();

        // Retry button (same seed, same difficulty)
        TextButton retryButton = new TextButton("Retry", skin);
        retryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Create new config with same seed and difficulty
                GameConfig retryConfig = new GameConfig(
                    result.getConfig().getMode(),
                    result.getConfig().getNumSuits(),
                    result.getConfig().getSeed()
                );
                // Pass current result as prior result for comparison
                game.setScreen(new GameScreen(game, retryConfig, result));
                dispose();
            }
        });
        buttonTable.add(retryButton).width(buttonWidth).height(buttonHeight).padRight(10f);

        // Play Again button (new seed)
        TextButton playAgainButton = new TextButton("Play Again", skin);
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Go back to difficulty select for solo practice
                game.setScreen(new DifficultySelectScreen(game, GameConfig.GameMode.SOLO_PRACTICE));
                dispose();
            }
        });
        buttonTable.add(playAgainButton).width(buttonWidth).height(buttonHeight).padRight(10f);

        TextButton menuButton = new TextButton("Menu", skin);
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        buttonTable.add(menuButton).width(buttonWidth).height(buttonHeight);

        table.add(buttonTable).colspan(2).padTop(25f);
    }

    private void addStatRow(Table table, String name, int value, Integer priorValue, boolean higherIsBetter) {
        Label nameLabel = new Label(name + ":", skin, "stats");
        Label valueLabel = new Label(String.valueOf(value), skin, "stats");

        table.add(nameLabel).right().padRight(10f).padBottom(10f);

        if (priorValue != null) {
            int diff = value - priorValue;
            if (diff != 0) {
                boolean isImprovement = higherIsBetter ? (diff > 0) : (diff < 0);
                String diffStr = " (" + (diff > 0 ? "+" : "") + diff + ")";
                Label diffLabel = new Label(diffStr, skin, "stats");
                diffLabel.setColor(isImprovement ? Color.GREEN : Color.RED);

                Table valueTable = new Table();
                valueTable.add(valueLabel);
                valueTable.add(diffLabel);
                table.add(valueTable).left().padBottom(10f);
            } else {
                table.add(valueLabel).left().padBottom(10f);
            }
        } else {
            table.add(valueLabel).left().padBottom(10f);
        }
        table.row();
    }

    private void addTimeStatRow(Table table, String name, float timeSeconds, String formattedTime,
                                 Float priorTimeSeconds, String priorFormattedTime) {
        Label nameLabel = new Label(name + ":", skin, "stats");
        Label valueLabel = new Label(formattedTime, skin, "stats");

        table.add(nameLabel).right().padRight(10f).padBottom(10f);

        if (priorTimeSeconds != null) {
            float diff = timeSeconds - priorTimeSeconds;
            if (Math.abs(diff) >= 1) { // Only show if difference is at least 1 second
                boolean isImprovement = diff < 0; // Lower time is better
                int diffSeconds = (int) Math.abs(diff);
                int diffMins = diffSeconds / 60;
                int diffSecs = diffSeconds % 60;
                String diffStr;
                if (diffMins > 0) {
                    diffStr = String.format(" (%s%d:%02d)", diff < 0 ? "-" : "+", diffMins, diffSecs);
                } else {
                    diffStr = String.format(" (%s%ds)", diff < 0 ? "-" : "+", diffSecs);
                }
                Label diffLabel = new Label(diffStr, skin, "stats");
                diffLabel.setColor(isImprovement ? Color.GREEN : Color.RED);

                Table valueTable = new Table();
                valueTable.add(valueLabel);
                valueTable.add(diffLabel);
                table.add(valueTable).left().padBottom(10f);
            } else {
                table.add(valueLabel).left().padBottom(10f);
            }
        } else {
            table.add(valueLabel).left().padBottom(10f);
        }
        table.row();
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        // Smaller font for stats (1.5x scale instead of 2x)
        BitmapFont statsFont = new BitmapFont();
        statsFont.getData().setScale(1.5f);
        skin.add("stats-font", statsFont);

        // Button font
        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.8f);
        skin.add("default-font", buttonFont);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(2.8f);  // 30% smaller than 4.0
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
        textButtonStyle.font = buttonFont;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = Color.WHITE;
        skin.add("title", titleStyle);

        // Stats label style (smaller)
        Label.LabelStyle statsStyle = new Label.LabelStyle();
        statsStyle.font = statsFont;
        statsStyle.fontColor = Color.WHITE;
        skin.add("stats", statsStyle);

        // Default label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = buttonFont;
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
