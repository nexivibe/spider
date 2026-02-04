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
    private final GameResult priorResult;
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

        // Calculate sizes
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float titlePadding = screenHeight * 0.04f;
        float statsPadding = screenHeight * 0.015f;

        // Title based on outcome
        String titleText = result.getOutcome() == GameResult.Outcome.WON ? "VICTORY!" : "Game Over";
        Label titleLabel = new Label(titleText, skin, "title");
        if (result.getOutcome() == GameResult.Outcome.WON) {
            titleLabel.setColor(new Color(0.3f, 1f, 0.3f, 1f));
        }
        table.add(titleLabel).colspan(2).padBottom(titlePadding);
        table.row();

        // Stats with optional comparison
        addStatRow(table, "Score", result.getScore(),
            priorResult != null ? priorResult.getScore() : null, true, statsPadding);
        addTimeStatRow(table, "Time", result.getTimeSeconds(), result.getFormattedTime(),
            priorResult != null ? priorResult.getTimeSeconds() : null,
            priorResult != null ? priorResult.getFormattedTime() : null, statsPadding);
        addStatRow(table, "Moves", result.getMoves(),
            priorResult != null ? priorResult.getMoves() : null, false, statsPadding);
        addStatRow(table, "Undos", result.getUndos(),
            priorResult != null ? priorResult.getUndos() : null, false, statsPadding);

        // Suits completed
        String suitsText = result.getCompletedSuits() + "/" + result.getRequiredSuits();
        Label suitsNameLabel = new Label("Suits:", skin, "stats");
        Label suitsValueLabel = new Label(suitsText, skin, "stats");
        if (priorResult != null) {
            int diff = result.getCompletedSuits() - priorResult.getCompletedSuits();
            if (diff != 0) {
                String diffStr = " (" + (diff > 0 ? "+" : "") + diff + ")";
                Label diffLabel = new Label(diffStr, skin, "stats");
                diffLabel.setColor(diff > 0 ? Color.GREEN : Color.RED);
                table.add(suitsNameLabel).right().padRight(statsPadding).padBottom(statsPadding);
                Table valueTable = new Table();
                valueTable.add(suitsValueLabel);
                valueTable.add(diffLabel);
                table.add(valueTable).left().padBottom(statsPadding);
            } else {
                table.add(suitsNameLabel).right().padRight(statsPadding).padBottom(statsPadding);
                table.add(suitsValueLabel).left().padBottom(statsPadding);
            }
        } else {
            table.add(suitsNameLabel).right().padRight(statsPadding).padBottom(statsPadding);
            table.add(suitsValueLabel).left().padBottom(statsPadding);
        }
        table.row();

        // Buttons - stack vertically for better touch targets on mobile
        table.row();
        float buttonWidth = screenWidth * 0.7f;
        float buttonHeight = screenHeight * 0.10f;
        float buttonPad = screenHeight * 0.015f;

        // Retry button
        TextButton retryButton = new TextButton("Retry Same Deal", skin);
        retryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameConfig retryConfig = new GameConfig(
                    result.getConfig().getMode(),
                    result.getConfig().getNumSuits(),
                    result.getConfig().getSeed()
                );
                game.setScreen(new GameScreen(game, retryConfig, result));
                dispose();
            }
        });
        table.add(retryButton).colspan(2).width(buttonWidth).height(buttonHeight).pad(buttonPad);
        table.row();

        // Play Again button
        TextButton playAgainButton = new TextButton("New Game", skin);
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new DifficultySelectScreen(game, GameConfig.GameMode.SOLO_PRACTICE));
                dispose();
            }
        });
        table.add(playAgainButton).colspan(2).width(buttonWidth).height(buttonHeight).pad(buttonPad);
        table.row();

        // Menu button
        TextButton menuButton = new TextButton("Main Menu", skin, "secondary");
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(menuButton).colspan(2).width(buttonWidth).height(buttonHeight).pad(buttonPad);
    }

    private void addStatRow(Table table, String name, int value, Integer priorValue,
                           boolean higherIsBetter, float padding) {
        Label nameLabel = new Label(name + ":", skin, "stats");
        Label valueLabel = new Label(String.valueOf(value), skin, "stats");

        table.add(nameLabel).right().padRight(padding).padBottom(padding);

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
                table.add(valueTable).left().padBottom(padding);
            } else {
                table.add(valueLabel).left().padBottom(padding);
            }
        } else {
            table.add(valueLabel).left().padBottom(padding);
        }
        table.row();
    }

    private void addTimeStatRow(Table table, String name, float timeSeconds, String formattedTime,
                                Float priorTimeSeconds, String priorFormattedTime, float padding) {
        Label nameLabel = new Label(name + ":", skin, "stats");
        Label valueLabel = new Label(formattedTime, skin, "stats");

        table.add(nameLabel).right().padRight(padding).padBottom(padding);

        if (priorTimeSeconds != null) {
            float diff = timeSeconds - priorTimeSeconds;
            if (Math.abs(diff) >= 1) {
                boolean isImprovement = diff < 0;
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
                table.add(valueTable).left().padBottom(padding);
            } else {
                table.add(valueLabel).left().padBottom(padding);
            }
        } else {
            table.add(valueLabel).left().padBottom(padding);
        }
        table.row();
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        float density = Gdx.graphics.getDensity();
        float fontScale = Math.max(1.6f, density * 1.3f);  // Button text
        float titleScale = Math.max(2.5f, density * 2.0f); // Title
        float statsScale = Math.max(1.5f, density * 1.2f); // Stats text
        float smallScale = Math.max(1.4f, density * 1.1f); // Secondary buttons

        BitmapFont font = new BitmapFont();
        font.getData().setScale(fontScale);
        skin.add("default-font", font);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(titleScale);
        skin.add("title-font", titleFont);

        BitmapFont statsFont = new BitmapFont();
        statsFont.getData().setScale(statsScale);
        skin.add("stats-font", statsFont);

        BitmapFont smallFont = new BitmapFont();
        smallFont.getData().setScale(smallScale);
        skin.add("small-font", smallFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0.25f, 0.45f, 0.35f, 1f));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.15f, 0.35f, 0.25f, 1f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.3f, 0.5f, 0.4f, 1f));
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

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

        Label.LabelStyle statsStyle = new Label.LabelStyle();
        statsStyle.font = statsFont;
        statsStyle.fontColor = Color.WHITE;
        skin.add("stats", statsStyle);

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
