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

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Title
        Label titleLabel = new Label("About", skin, "title");
        table.add(titleLabel).padBottom(40f);
        table.row();

        // Info message - split into multiple lines for better readability
        String line1 = "This is spider solitaire made very simply";
        String line2 = "without ads by the team at NexiVIBE.";
        String line3 = "We want you to have good plain simple fun";
        String line4 = "without distraction.";

        Label msg1 = new Label(line1, skin);
        Label msg2 = new Label(line2, skin);
        Label msg3 = new Label(line3, skin);
        Label msg4 = new Label(line4, skin);

        table.add(msg1).padBottom(5f);
        table.row();
        table.add(msg2).padBottom(20f);
        table.row();
        table.add(msg3).padBottom(5f);
        table.row();
        table.add(msg4).padBottom(50f);
        table.row();

        // Back button
        TextButton backButton = new TextButton("Back to Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        table.add(backButton).width(300f).height(70f).pad(20f);
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        // Regular font with 2x scale
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2.0f);
        skin.add("default-font", font);

        // Large title font with 3x scale
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

        // Title label style
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = Color.WHITE;
        skin.add("title", titleStyle);

        // Default label style
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
