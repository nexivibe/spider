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

public class InfoScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;

    public InfoScreen(Main game) {
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
        float titlePadding = screenHeight * 0.05f;
        float linePadding = screenHeight * 0.012f;

        // Title
        Label titleLabel = new Label("About", skin, "title");
        table.add(titleLabel).padBottom(titlePadding);
        table.row();

        // Info message
        String[] lines = {
            "Spider Solitaire",
            "made simply, without ads",
            "by the team at NexiVIBE.",
            "",
            "We want you to have",
            "good plain simple fun",
            "without distraction."
        };

        for (String line : lines) {
            if (line.isEmpty()) {
                table.add().height(linePadding * 2);
            } else {
                Label msgLabel = new Label(line, skin);
                table.add(msgLabel).padBottom(linePadding);
            }
            table.row();
        }

        // Back button
        TextButton backButton = new TextButton("Back to Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        float buttonWidth = screenWidth * 0.75f;
        float buttonHeight = screenHeight * 0.11f;
        table.add(backButton).width(buttonWidth).height(buttonHeight).padTop(titlePadding * 1.5f);
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        float density = Gdx.graphics.getDensity();
        float fontScale = Math.max(1.6f, density * 1.3f);  // Body text
        float titleScale = Math.max(2.2f, density * 1.8f); // Title

        BitmapFont font = new BitmapFont();
        font.getData().setScale(fontScale);
        skin.add("default-font", font);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(titleScale);
        skin.add("title-font", titleFont);

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
