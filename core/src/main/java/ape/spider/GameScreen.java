package ape.spider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameScreen implements Screen, InputProcessor {
    private final Main game;
    private final GameConfig config;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont cardFont;

    // Virtual screen size for consistent layout
    private static final float VIRTUAL_WIDTH = 900f;
    private static final float VIRTUAL_HEIGHT = 700f;
    private Viewport viewport;

    // Card dimensions - calculated to fill width with 2px gaps
    // VIRTUAL_WIDTH (900) - 2px margins on each side (4) - 9 gaps of 2px (18) = 878 for 10 cards
    // Card width = 878 / 10 = 87.8, height maintains ratio (100/70 * 87.8 = 125.4)
    private static final float CARD_GAP = 2f;
    private static final float EDGE_MARGIN = 2f;
    private static final float CARD_WIDTH = (VIRTUAL_WIDTH - EDGE_MARGIN * 2 - CARD_GAP * 9) / 10f;  // ~87.8
    private static final float CARD_HEIGHT = CARD_WIDTH * (100f / 70f);  // Maintain aspect ratio ~125.4
    private static final float CARD_SPACING_REVEALED = 30f;  // Standard spacing for revealed cards (scaled up)
    private static final float CARD_SPACING_HIDDEN_EXPANDED = 10f;  // Hidden cards when expanded
    private static final float CARD_SPACING_HIDDEN_COLLAPSED = 4f; // Hidden cards when collapsed (single card height)
    private static final float MIN_REVEALED_SPACING = 15f;  // Minimum spacing when compressed
    private static final float TABLEAU_SPACING = CARD_WIDTH + CARD_GAP;  // Card width + 2px gap
    private static final float CARD_CORNER_RADIUS = 10f;  // Slightly larger for bigger cards
    private static final float TOP_MARGIN = 10f;  // Space from top of screen
    private static final float BOTTOM_MARGIN = 30f;  // Minimal space for HUD at bottom, cards can overlap

    // Layout positions (calculated in render)
    private float startX;
    private float startY;
    private float stockX;
    private float stockY;

    // Collapsed state per tableau (for unrevealed cards)
    private boolean[] tableauCollapsed;

    // Game state
    private List<List<Card>> tableaus;
    private List<Card> stock;
    private List<List<Card>> foundations;
    private long randomSeed;

    // Scoring - Based on competitive Spider Solitaire rules
    // Starting score of 500, -1 per move, -10 per undo, +100 per completed suit
    private static final int STARTING_SCORE = 500;
    private static final int POINTS_PER_MOVE = -1;
    private static final int POINTS_PER_UNDO = -10;
    private static final int POINTS_PER_COMPLETED_SUIT = 100;

    private int score;
    private int completedSuits;
    private int requiredSuitsToWin;
    private int totalMoves;
    private int totalUndos;

    // Undo system - stores complete game state history
    private List<GameState> moveHistory;

    // Timer
    private float elapsedGameTime;

    // Undo button position (15% smaller: 40 * 0.85 = 34)
    private static final float UNDO_ICON_SIZE = 34f;
    private float undoIconX;
    private float undoIconY;

    // Undo debounce to prevent double-firing
    private static final long UNDO_DEBOUNCE_MS = 300;
    private long lastUndoTime;

    // Drag state
    private boolean isDragging;
    private int dragSourceCol;
    private int dragSourceCardIndex;
    private List<Card> draggedCards;
    private float dragOffsetX, dragOffsetY;
    private float dragCurrentX, dragCurrentY;

    // Tap/double-click detection
    private long lastTapTime;
    private int lastTapCol;
    private int lastTapCardIndex;
    private static final long TAP_THRESHOLD_MS = 200;
    private static final long DOUBLE_CLICK_THRESHOLD_MS = 400;
    private float touchDownX, touchDownY;
    private static final float DRAG_THRESHOLD = 10f;

    // Menu state
    private boolean menuOpen;
    private Stage menuStage;
    private Skin menuSkin;
    private static final float MENU_ICON_SIZE = 34f;  // 15% smaller: 40 * 0.85 = 34
    private static final float MENU_ICON_PADDING = 8f;  // Moved up and left

    // Prior result for retry comparison (null if first attempt)
    private final GameResult priorResult;

    public GameScreen(Main game) {
        this(game, GameConfig.soloPractice(4), null); // Default to 4 suits for backwards compatibility
    }

    public GameScreen(Main game, GameConfig config) {
        this(game, config, null);
    }

    public GameScreen(Main game, GameConfig config, GameResult priorResult) {
        this.game = game;
        this.config = config;
        this.priorResult = priorResult;
    }

    @Override
    public void show() {
        // Set up viewport for proper resizing
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        viewport.apply(true);

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        cardFont = new BitmapFont();
        cardFont.getData().setScale(1.8f); // Larger font for card numbers

        randomSeed = config.getSeed();
        initializeGame(randomSeed);

        // Initialize drag state
        isDragging = false;
        draggedCards = new ArrayList<>();
        elapsedGameTime = 0f;

        // Initialize collapsed state for each tableau (start expanded)
        tableauCollapsed = new boolean[10];
        for (int i = 0; i < 10; i++) {
            tableauCollapsed[i] = false;
        }

        // Initialize scoring and move tracking
        score = STARTING_SCORE;
        completedSuits = 0;
        totalMoves = 0;
        totalUndos = 0;

        // Initialize undo history
        moveHistory = new ArrayList<>();
        saveGameState(); // Save initial state

        // Initialize menu - use FitViewport to match game viewport for proper overlay
        menuOpen = false;
        menuStage = new Stage(new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT));
        menuSkin = createMenuSkin();

        Gdx.input.setInputProcessor(this);
    }

    private void initializeGame(long seed) {
        Random random = new Random(seed);

        // Get suits to use based on config (1-6 suits)
        int numSuits = config.getNumSuits();
        Suit[] suitsToUse = new Suit[numSuits];
        Suit[] allSuits = Suit.values();
        for (int i = 0; i < numSuits; i++) {
            suitsToUse[i] = allSuits[i];
        }

        // Determine total cards based on number of suits:
        // 1-4 suits: 104 cards (8 completed suits to win)
        // 5 suits: 130 cards (10 completed suits to win)
        // 6 suits: 156 cards (12 completed suits to win)
        int totalCards;
        if (numSuits <= 4) {
            totalCards = 104;
            requiredSuitsToWin = 8;
        } else if (numSuits == 5) {
            totalCards = 130;
            requiredSuitsToWin = 10;
        } else {
            totalCards = 156;
            requiredSuitsToWin = 12;
        }

        // Create deck with cards distributed evenly across suits
        List<Card> deck = new ArrayList<>();
        int cardsPerSuit = totalCards / numSuits;

        for (int suitIndex = 0; suitIndex < numSuits; suitIndex++) {
            Suit suit = suitsToUse[suitIndex];
            int fullDecks = cardsPerSuit / 13;

            // Add full decks (13 cards each: A through K)
            for (int deckNum = 0; deckNum < fullDecks; deckNum++) {
                for (int rank = 1; rank <= 13; rank++) {
                    deck.add(new Card(suit, rank));
                }
            }
        }

        // Shuffle the deck
        Collections.shuffle(deck, random);

        // Initialize 10 tableaus
        tableaus = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tableaus.add(new ArrayList<>());
        }

        // Deal cards to tableaus
        // First 4 tableaus get 6 cards each, remaining 6 tableaus get 5 cards each
        // Total: 4*6 + 6*5 = 24 + 30 = 54 cards dealt
        int cardIndex = 0;
        for (int col = 0; col < 10; col++) {
            int cardsInColumn = (col < 4) ? 6 : 5;
            for (int row = 0; row < cardsInColumn; row++) {
                Card card = deck.get(cardIndex++);
                // Only the top card (last dealt to each column) is face up
                if (row == cardsInColumn - 1) {
                    card.setFaceUp(true);
                }
                tableaus.get(col).add(card);
            }
        }

        // Remaining 50 cards go to stock
        stock = new ArrayList<>();
        while (cardIndex < deck.size()) {
            stock.add(deck.get(cardIndex++));
        }

        // Initialize 8 foundation piles (for completed suits)
        foundations = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            foundations.add(new ArrayList<>());
        }
    }

    @Override
    public void render(float delta) {
        // Update timer if not paused
        if (!menuOpen) {
            elapsedGameTime += delta;
        }

        ScreenUtils.clear(0.0f, 0.4f, 0.2f, 1f); // Green table color

        // Apply viewport
        viewport.apply();

        float screenWidth = VIRTUAL_WIDTH;
        float screenHeight = VIRTUAL_HEIGHT;

        // Stock and foundation at top of screen (stick to top for mobile)
        stockX = screenWidth - CARD_WIDTH - EDGE_MARGIN;
        stockY = screenHeight - CARD_HEIGHT - TOP_MARGIN;

        // Tableaus start at edge margin, cards fill the width with minimal gaps
        startX = EDGE_MARGIN;
        // Cards start just below the stock/foundation area
        startY = stockY - 10f;

        // Available height for card stacks (from startY down to bottom margin)
        float availableHeight = startY - BOTTOM_MARGIN;

        // Set projection matrix for shape renderer
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        // Draw bottom HUD background using screen coordinates so it anchors to actual bottom
        drawBottomHudBackground();

        // Draw stock pile at top right - show deals remaining as large number
        int dealsRemaining = stock.size() / 10;
        int remainingCards = stock.size() % 10;
        boolean stockBlocked = hasEmptyTableau();
        if (!stock.isEmpty()) {
            // Draw stock card back with pattern (grayed out if blocked)
            Color stockColor = stockBlocked ? new Color(0.3f, 0.3f, 0.35f, 1f) : new Color(0.15f, 0.15f, 0.5f, 1f);
            Color stockBorderColor = stockBlocked ? new Color(0.4f, 0.4f, 0.45f, 1f) : new Color(0.3f, 0.3f, 0.7f, 1f);
            drawRoundedRect(stockX, stockY, CARD_WIDTH, CARD_HEIGHT,
                CARD_CORNER_RADIUS, stockColor, Color.BLACK);
            // Add inner border for card back design
            float inset = 4f;
            drawRoundedBorder(stockX + inset, stockY + inset, CARD_WIDTH - inset * 2, CARD_HEIGHT - inset * 2,
                CARD_CORNER_RADIUS - 2, stockBorderColor);
            // Number will be drawn in batch section below
        } else {
            // Draw empty stock placeholder - just outline
            drawCardPlaceholder(stockX, stockY);
        }

        // Draw completed suits as cascaded pile centered in available space
        // Available space is between menu buttons and stock pile
        float completedAreaLeft = MENU_ICON_PADDING + MENU_ICON_SIZE + 40f;
        float completedAreaRight = stockX - 20f;
        float completedAreaWidth = completedAreaRight - completedAreaLeft;
        float cascadeOffset = 18f; // Offset between cascaded cards
        float completedPileWidth = CARD_WIDTH + (completedSuits > 0 ? (completedSuits - 1) * cascadeOffset : 0);
        float completedStartX = completedAreaLeft + (completedAreaWidth - completedPileWidth) / 2f;

        // Always draw placeholder for completed suits area
        drawCardPlaceholder(completedAreaLeft + (completedAreaWidth - CARD_WIDTH) / 2f, stockY);

        // Draw completed suits on top of placeholder
        for (int i = 0; i < completedSuits; i++) {
            float fx = completedStartX + i * cascadeOffset;
            // Completed suit - show gold/bronze card with proper card styling
            drawCompletedSuitCard(fx, stockY);
        }

        // Draw tableaus with dynamic spacing
        for (int col = 0; col < 10; col++) {
            float x = startX + col * TABLEAU_SPACING;
            List<Card> tableau = tableaus.get(col);

            if (tableau.isEmpty()) {
                // Draw empty placeholder - just outline
                drawCardPlaceholder(x, startY - CARD_HEIGHT);
            } else {
                // Calculate dynamic spacing for this column
                float[] spacings = calculateCardSpacings(col, availableHeight);

                // Draw cards in tableau (skip dragged cards)
                float currentY = startY;
                for (int row = 0; row < tableau.size(); row++) {
                    // Skip cards being dragged
                    if (isDragging && col == dragSourceCol && row >= dragSourceCardIndex) {
                        continue;
                    }
                    Card card = tableau.get(row);
                    float cardY = currentY - CARD_HEIGHT;
                    drawCardRounded(x, cardY, card);

                    // Move down for next card using dynamic spacing
                    if (row < tableau.size() - 1) {
                        currentY -= spacings[row];
                    }
                }
            }
        }

        // Draw dragged cards with standard spacing
        if (isDragging && !draggedCards.isEmpty()) {
            float dragX = dragCurrentX - dragOffsetX;
            float dragY = dragCurrentY - dragOffsetY;
            for (int i = 0; i < draggedCards.size(); i++) {
                Card card = draggedCards.get(i);
                drawCardRounded(dragX, dragY - i * CARD_SPACING_REVEALED, card);
            }
        }

        // Draw menu and undo buttons adjacent (menu left of undo)
        float buttonMargin = 6f;
        float buttonSpacing = 4f;
        float menuButtonX = MENU_ICON_PADDING;
        float menuButtonY = screenHeight - MENU_ICON_PADDING - MENU_ICON_SIZE - buttonMargin;
        drawButtonBackground(menuButtonX - buttonMargin, menuButtonY - buttonMargin,
            MENU_ICON_SIZE + buttonMargin * 2, MENU_ICON_SIZE + buttonMargin * 2);
        drawHamburgerIcon(menuButtonX, menuButtonY);

        // Draw undo button to the right of menu button
        undoIconX = menuButtonX + MENU_ICON_SIZE + buttonMargin * 2 + buttonSpacing;
        undoIconY = menuButtonY;
        drawButtonBackground(undoIconX - buttonMargin, undoIconY - buttonMargin,
            UNDO_ICON_SIZE + buttonMargin * 2, UNDO_ICON_SIZE + buttonMargin * 2);
        drawUndoIcon(undoIconX, undoIconY, canUndo());

        // Draw bottom HUD text using screen coordinates (anchored to bottom)
        drawBottomHudText();

        // Draw text labels for cards
        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Draw stock deals remaining as large number on card
        if (!stock.isEmpty()) {
            if (stockBlocked) {
                // Show "FILL GAPS" message when stock is blocked
                cardFont.getData().setScale(1.2f);
                cardFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
                cardFont.draw(batch, "FILL", stockX + 14f, stockY + CARD_HEIGHT - 25f);
                cardFont.draw(batch, "GAPS", stockX + 12f, stockY + CARD_HEIGHT - 50f);
                cardFont.getData().setScale(1.8f); // Restore normal scale
            } else {
                // Use a large font scale for the number
                cardFont.getData().setScale(3.0f);
                cardFont.setColor(Color.WHITE);
                // Show full deals remaining, or "*" for partial last deal
                String dealsStr = dealsRemaining > 0 ? String.valueOf(dealsRemaining) : "*";
                // Center the number on the card
                float textX = stockX + CARD_WIDTH / 2 - 15f;
                float textY = stockY + CARD_HEIGHT / 2 + 20f;
                cardFont.draw(batch, dealsStr, textX, textY);
                cardFont.getData().setScale(1.8f); // Restore normal scale
            }
        }

        // Draw card values on face-up cards (using same dynamic spacing as card rendering)
        for (int col = 0; col < 10; col++) {
            float x = startX + col * TABLEAU_SPACING;
            List<Card> tableau = tableaus.get(col);
            if (tableau.isEmpty()) continue;

            float[] spacings = calculateCardSpacings(col, availableHeight);
            float currentY = startY;

            for (int row = 0; row < tableau.size(); row++) {
                // Skip cards being dragged
                if (isDragging && col == dragSourceCol && row >= dragSourceCardIndex) {
                    continue;
                }
                Card card = tableau.get(row);
                if (card.isFaceUp()) {
                    float cardY = currentY - CARD_HEIGHT;
                    String cardText = card.getRankSymbol() + card.getSuit().getSymbol();
                    cardFont.setColor(card.getSuit().getColor());
                    cardFont.draw(batch, cardText, x + 8, cardY + CARD_HEIGHT - 8);
                }
                if (row < tableau.size() - 1) {
                    currentY -= spacings[row];
                }
            }
        }

        // Draw values on dragged cards
        if (isDragging && !draggedCards.isEmpty()) {
            float dragX = dragCurrentX - dragOffsetX;
            float dragY = dragCurrentY - dragOffsetY;
            for (int i = 0; i < draggedCards.size(); i++) {
                Card card = draggedCards.get(i);
                if (card.isFaceUp()) {
                    float cardY = dragY - i * CARD_SPACING_REVEALED;
                    String cardText = card.getRankSymbol() + card.getSuit().getSymbol();
                    cardFont.setColor(card.getSuit().getColor());
                    cardFont.draw(batch, cardText, dragX + 8, cardY + CARD_HEIGHT - 8);
                }
            }
        }
        batch.end();

        // Draw menu overlay if open
        if (menuOpen) {
            // Draw full-screen overlay using screen coordinates (covers letterbox areas too)
            drawFullScreenOverlay();

            // Reset menu stage viewport to full screen for proper centering
            int screenW = Gdx.graphics.getWidth();
            int screenH = Gdx.graphics.getHeight();
            Gdx.gl.glViewport(0, 0, screenW, screenH);
            menuStage.getViewport().update(screenW, screenH, true);

            menuStage.act(delta);
            menuStage.draw();
        }
    }

    private void drawFullScreenOverlay() {
        // Use actual screen coordinates to draw overlay that covers entire window
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        // Reset GL viewport to full screen (override any game viewport offset)
        Gdx.gl.glViewport(0, 0, screenW, screenH);

        // Enable blending for transparency
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Create fresh matrix for screen coordinates
        com.badlogic.gdx.math.Matrix4 screenMatrix = new com.badlogic.gdx.math.Matrix4();
        screenMatrix.setToOrtho2D(0, 0, screenW, screenH);
        shapeRenderer.setProjectionMatrix(screenMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.2f, 0.85f)); // Gray transparent overlay
        shapeRenderer.rect(0, 0, screenW, screenH);
        shapeRenderer.end();

        // Restore game viewport for subsequent drawing
        viewport.apply();
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    }

    private void drawBottomHudBackground() {
        // Use actual screen coordinates so HUD anchors to real bottom of window
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        float hudHeight = 25f; // Fixed pixel height for bottom bar

        // Reset GL viewport to full screen (override the game viewport's offset)
        Gdx.gl.glViewport(0, 0, screenW, screenH);

        // Create fresh matrix for screen coordinates
        com.badlogic.gdx.math.Matrix4 screenMatrix = new com.badlogic.gdx.math.Matrix4();
        screenMatrix.setToOrtho2D(0, 0, screenW, screenH);
        shapeRenderer.setProjectionMatrix(screenMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.0f, 0.35f, 0.18f, 1f)); // Off-green
        shapeRenderer.rect(0, 0, screenW, hudHeight);
        shapeRenderer.end();

        // Restore game viewport
        viewport.apply();
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
    }

    private void drawBottomHudText() {
        // Use actual screen coordinates so text anchors to real bottom of window
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        // Reset GL viewport to full screen (override the game viewport's offset)
        Gdx.gl.glViewport(0, 0, screenW, screenH);

        // Create fresh matrix for screen coordinates
        com.badlogic.gdx.math.Matrix4 screenMatrix = new com.badlogic.gdx.math.Matrix4();
        screenMatrix.setToOrtho2D(0, 0, screenW, screenH);

        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(screenMatrix);

        batch.begin();
        font.setColor(new Color(0.8f, 0.9f, 0.8f, 1f)); // Light greenish white

        float hudY = 8f; // Y position from bottom
        // Seed - left aligned
        font.draw(batch, "Seed: " + randomSeed, 10, hudY + font.getCapHeight());
        // Time - centered
        String timeStr = formatTime(elapsedGameTime);
        font.draw(batch, timeStr, screenW / 2f - 25, hudY + font.getCapHeight());
        // Score, moves, and undos - right aligned
        String scoreText = "Score: " + score + "  Moves: " + totalMoves + "  Undos: " + totalUndos;
        font.draw(batch, scoreText, screenW - 230, hudY + font.getCapHeight());

        batch.end();

        // Restore game viewport
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
    }

    private void drawButtonBackground(float x, float y, float width, float height) {
        // Draw button background with slight transparency
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.2f, 0.2f, 0.3f, 0.8f));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // Draw button border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(0.4f, 0.4f, 0.5f, 1f));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    private void drawHamburgerIcon(float x, float y) {
        float lineHeight = 3f;
        float lineWidth = MENU_ICON_SIZE - 16f;
        float lineSpacing = 8f;
        float offsetX = 8f;
        float offsetY = 10f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);

        // Three horizontal lines centered in button
        for (int i = 0; i < 3; i++) {
            float lineY = y + MENU_ICON_SIZE - offsetY - i * lineSpacing;
            shapeRenderer.rect(x + offsetX, lineY, lineWidth, lineHeight);
        }

        shapeRenderer.end();
    }

    private void drawUndoIcon(float x, float y, boolean enabled) {
        Color color = enabled ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 0.5f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);

        // Draw curved arrow for undo
        float centerX = x + UNDO_ICON_SIZE / 2;
        float centerY = y + UNDO_ICON_SIZE / 2;
        float radius = UNDO_ICON_SIZE / 3;

        // Draw arc (partial circle going counter-clockwise)
        shapeRenderer.arc(centerX, centerY, radius, 45, 270, 20);

        shapeRenderer.end();

        // Draw arrowhead
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);

        // Arrow pointing left at the top of the arc
        float arrowX = centerX - radius * 0.7f;
        float arrowY = centerY + radius * 0.7f;
        shapeRenderer.triangle(
            arrowX - 8, arrowY,
            arrowX + 4, arrowY + 8,
            arrowX + 4, arrowY - 8
        );

        shapeRenderer.end();
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius, Color fillColor, Color borderColor) {
        // Draw filled rounded rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fillColor);

        // Center rectangle
        shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
        // Left rectangle
        shapeRenderer.rect(x, y + radius, radius, height - 2 * radius);
        // Right rectangle
        shapeRenderer.rect(x + width - radius, y + radius, radius, height - 2 * radius);

        // Corners (arcs)
        int segments = 10;
        // Bottom-left
        shapeRenderer.arc(x + radius, y + radius, radius, 180, 90, segments);
        // Bottom-right
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270, 90, segments);
        // Top-right
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0, 90, segments);
        // Top-left
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90, 90, segments);

        shapeRenderer.end();

        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(borderColor);

        // Bottom edge
        shapeRenderer.line(x + radius, y, x + width - radius, y);
        // Top edge
        shapeRenderer.line(x + radius, y + height, x + width - radius, y + height);
        // Left edge
        shapeRenderer.line(x, y + radius, x, y + height - radius);
        // Right edge
        shapeRenderer.line(x + width, y + radius, x + width, y + height - radius);

        // Corner arcs
        shapeRenderer.arc(x + radius, y + radius, radius, 180, 90, segments);
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270, 90, segments);
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0, 90, segments);
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90, 90, segments);

        shapeRenderer.end();
    }

    private void drawCardRounded(float x, float y, Card card) {
        if (card.isFaceUp()) {
            // Face-up card - white with black border, looks like playing card
            drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, CARD_CORNER_RADIUS, Color.WHITE, Color.BLACK);
        } else {
            // Face-down card - blue pattern back
            drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, CARD_CORNER_RADIUS,
                new Color(0.15f, 0.15f, 0.5f, 1f), Color.BLACK);
            // Add inner border for card back design
            float inset = 4f;
            drawRoundedBorder(x + inset, y + inset, CARD_WIDTH - inset * 2, CARD_HEIGHT - inset * 2,
                CARD_CORNER_RADIUS - 2, new Color(0.3f, 0.3f, 0.7f, 1f));
        }
    }

    private void drawCardPlaceholder(float x, float y) {
        // Empty card slot - semi-transparent fill with visible border
        drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, CARD_CORNER_RADIUS,
            new Color(0.0f, 0.3f, 0.15f, 0.5f), new Color(0.3f, 0.5f, 0.3f, 0.8f));
    }

    private void drawCompletedSuitCard(float x, float y) {
        // Completed suit card - gold/bronze colored card
        drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, CARD_CORNER_RADIUS,
            new Color(0.85f, 0.75f, 0.3f, 1f), new Color(0.6f, 0.5f, 0.1f, 1f));
        // Add decorative inner border
        float inset = 4f;
        drawRoundedBorder(x + inset, y + inset, CARD_WIDTH - inset * 2, CARD_HEIGHT - inset * 2,
            CARD_CORNER_RADIUS - 2, new Color(0.95f, 0.85f, 0.4f, 1f));
    }

    private void drawRoundedBorder(float x, float y, float width, float height, float radius, Color borderColor) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(borderColor);

        // Bottom edge
        shapeRenderer.line(x + radius, y, x + width - radius, y);
        // Top edge
        shapeRenderer.line(x + radius, y + height, x + width - radius, y + height);
        // Left edge
        shapeRenderer.line(x, y + radius, x, y + height - radius);
        // Right edge
        shapeRenderer.line(x + width, y + radius, x + width, y + height - radius);

        // Draw corner curves using line segments (avoids arc's extra center lines)
        int segments = 8;
        // Bottom-left corner (180 to 270 degrees)
        drawCornerCurve(x + radius, y + radius, radius, 180, 270, segments);
        // Bottom-right corner (270 to 360 degrees)
        drawCornerCurve(x + width - radius, y + radius, radius, 270, 360, segments);
        // Top-right corner (0 to 90 degrees)
        drawCornerCurve(x + width - radius, y + height - radius, radius, 0, 90, segments);
        // Top-left corner (90 to 180 degrees)
        drawCornerCurve(x + radius, y + height - radius, radius, 90, 180, segments);

        shapeRenderer.end();
    }

    // Draw a corner curve using line segments (no center lines like arc does)
    private void drawCornerCurve(float cx, float cy, float radius, float startAngle, float endAngle, int segments) {
        float angleStep = (endAngle - startAngle) / segments;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) Math.toRadians(startAngle + i * angleStep);
            float angle2 = (float) Math.toRadians(startAngle + (i + 1) * angleStep);
            float x1 = cx + radius * (float) Math.cos(angle1);
            float y1 = cy + radius * (float) Math.sin(angle1);
            float x2 = cx + radius * (float) Math.cos(angle2);
            float y2 = cy + radius * (float) Math.sin(angle2);
            shapeRenderer.line(x1, y1, x2, y2);
        }
    }

    // Calculate dynamic card spacings for a tableau based on available height
    private float[] calculateCardSpacings(int col, float availableHeight) {
        List<Card> tableau = tableaus.get(col);
        int numCards = tableau.size();
        if (numCards <= 1) {
            return new float[0];
        }

        float[] spacings = new float[numCards - 1];

        // Count hidden and revealed cards
        int hiddenCount = 0;
        int revealedCount = 0;
        for (Card card : tableau) {
            if (card.isFaceUp()) {
                revealedCount++;
            } else {
                hiddenCount++;
            }
        }

        // Determine hidden card spacing based on collapsed state
        float hiddenSpacing = tableauCollapsed[col] ? CARD_SPACING_HIDDEN_COLLAPSED : CARD_SPACING_HIDDEN_EXPANDED;

        // Calculate total height needed with current spacing
        float hiddenHeight = hiddenCount > 0 ? hiddenCount * hiddenSpacing : 0;
        float revealedHeight = revealedCount > 1 ? (revealedCount - 1) * CARD_SPACING_REVEALED : 0;
        float totalNeededHeight = CARD_HEIGHT + hiddenHeight + revealedHeight;

        // Determine revealed card spacing (may need compression)
        float revealedSpacing = CARD_SPACING_REVEALED;
        if (totalNeededHeight > availableHeight && revealedCount > 1) {
            // Need to compress revealed cards
            float availableForRevealed = availableHeight - CARD_HEIGHT - hiddenHeight;
            revealedSpacing = Math.max(MIN_REVEALED_SPACING, availableForRevealed / (revealedCount - 1));
        }

        // Fill in spacings array
        for (int i = 0; i < numCards - 1; i++) {
            Card card = tableau.get(i);
            if (!card.isFaceUp()) {
                spacings[i] = hiddenSpacing;
            } else {
                spacings[i] = revealedSpacing;
            }
        }

        return spacings;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        // Anchor viewport to top of screen when letterboxing
        float worldAspect = VIRTUAL_WIDTH / VIRTUAL_HEIGHT;
        float screenAspect = (float) width / height;
        if (screenAspect < worldAspect) {
            // Screen is taller than world - move viewport to top
            int viewportHeight = viewport.getScreenHeight();
            int yOffset = height - viewportHeight; // Position at top
            viewport.setScreenY(yOffset);
            viewport.apply();
        }

        menuStage.getViewport().update(width, height, true);
        if (screenAspect < worldAspect) {
            int viewportHeight = menuStage.getViewport().getScreenHeight();
            int yOffset = height - viewportHeight;
            menuStage.getViewport().setScreenY(yOffset);
            menuStage.getViewport().apply();
        }
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
        shapeRenderer.dispose();
        font.dispose();
        cardFont.dispose();
        menuStage.dispose();
        menuSkin.dispose();
    }

    // --- Menu Skin Creation ---

    private Skin createMenuSkin() {
        Skin skin = new Skin();

        BitmapFont menuFont = new BitmapFont();
        menuFont.getData().setScale(2.0f); // Double the font size for menu
        skin.add("default-font", menuFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", new Color(0.3f, 0.3f, 0.4f, 1f));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.3f, 1f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.4f, 0.4f, 0.5f, 1f));
        textButtonStyle.font = menuFont;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle =
            new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = menuFont;
        windowStyle.titleFontColor = Color.WHITE;
        windowStyle.background = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.25f, 0.95f));
        skin.add("default", windowStyle);

        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle =
            new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = menuFont;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        return skin;
    }

    private void showPauseMenu() {
        menuOpen = true;
        menuStage.clear();

        // Note: Full-screen overlay is drawn separately in render() to cover letterbox areas

        // Create centered table for menu content
        com.badlogic.gdx.scenes.scene2d.ui.Table menuTable = new com.badlogic.gdx.scenes.scene2d.ui.Table();
        menuTable.setFillParent(true);
        menuTable.center();

        // Title
        com.badlogic.gdx.scenes.scene2d.ui.Label titleLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label(
            "GAME PAUSED", menuSkin);
        menuTable.add(titleLabel).padBottom(30f);
        menuTable.row();

        // Stats
        String statsText = "Time: " + formatTime(elapsedGameTime) + "\n" +
                          "Score: " + score + "\n" +
                          "Moves: " + totalMoves + "\n" +
                          "Undos: " + totalUndos;
        com.badlogic.gdx.scenes.scene2d.ui.Label statsLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label(
            statsText, menuSkin);
        menuTable.add(statsLabel).padBottom(30f);
        menuTable.row();

        // Resume button
        TextButton resumeButton = new TextButton("Resume Game", menuSkin);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                menuOpen = false;
                Gdx.input.setInputProcessor(GameScreen.this);
            }
        });
        menuTable.add(resumeButton).width(250f).height(60f).padBottom(15f);
        menuTable.row();

        // Abort button
        TextButton abortButton = new TextButton("Abort Game", menuSkin);
        abortButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Create aborted game result
                GameResult result = new GameResult(config, GameResult.Outcome.ABORTED, score, totalMoves, totalUndos, elapsedGameTime, completedSuits);

                // Navigate based on game mode
                if (config.getMode() == GameConfig.GameMode.SOLO_PRACTICE) {
                    // Solo Practice shows result screen (pass prior result for comparison)
                    game.setScreen(new ResultScreen(game, result, priorResult));
                } else {
                    // Daily Grind returns to main menu
                    game.setScreen(new MainMenuScreen(game));
                }
                dispose();
            }
        });
        menuTable.add(abortButton).width(250f).height(60f);

        menuStage.addActor(menuTable);
        Gdx.input.setInputProcessor(menuStage);
    }

    private String formatTime(float seconds) {
        int totalSeconds = (int) seconds;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    // --- InputProcessor Implementation ---

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            if (menuOpen) {
                menuOpen = false;
                Gdx.input.setInputProcessor(this);
            } else {
                showPauseMenu();
            }
            return true;
        }

        // Ctrl+Z for undo
        if (keycode == Input.Keys.Z && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                                         Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
            if (canUndo()) {
                undo();
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    private float[] screenToWorld(int screenX, int screenY) {
        // Convert screen coordinates to world coordinates using viewport
        com.badlogic.gdx.math.Vector2 worldCoords = viewport.unproject(
            new com.badlogic.gdx.math.Vector2(screenX, screenY));
        return new float[]{worldCoords.x, worldCoords.y};
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (menuOpen) return false;

        float[] world = screenToWorld(screenX, screenY);
        float worldX = world[0];
        float worldY = world[1];

        // Check hamburger menu click
        if (isOnMenuIcon(worldX, worldY)) {
            showPauseMenu();
            return true;
        }

        // Check undo icon click - just mark that we're on the undo button
        // Actual undo happens on touchUp for proper button behavior
        if (isOnUndoIcon(worldX, worldY)) {
            return true; // Consume the event but don't undo yet
        }

        // Check stock pile click
        if (isOnStock(worldX, worldY)) {
            dealFromStock();
            return true;
        }

        touchDownX = worldX;
        touchDownY = worldY;

        // Find which card was clicked
        int[] hit = getCardAtPosition(worldX, worldY);
        if (hit != null) {
            int col = hit[0];
            int cardIndex = hit[1];
            List<Card> tableau = tableaus.get(col);
            Card card = tableau.get(cardIndex);

            if (!card.isFaceUp()) {
                // Tapped on unrevealed card - toggle collapsed state for this column
                tableauCollapsed[col] = !tableauCollapsed[col];
                return true;
            }

            if (card.isFaceUp()) {
                // Check for double-click / quick tap
                long currentTime = System.currentTimeMillis();
                if (col == lastTapCol && cardIndex == lastTapCardIndex &&
                    (currentTime - lastTapTime) < DOUBLE_CLICK_THRESHOLD_MS) {
                    // Double-click detected - try auto-move
                    tryAutoMove(col, cardIndex);
                    lastTapTime = 0;
                    // Clear any drag state to prevent accidental moves
                    draggedCards = new ArrayList<>();
                    isDragging = false;
                    return true;
                }

                lastTapTime = currentTime;
                lastTapCol = col;
                lastTapCardIndex = cardIndex;

                // Start potential drag
                List<Card> validStack = getValidStackFromCard(col, cardIndex);
                if (validStack != null && !validStack.isEmpty()) {
                    dragSourceCol = col;
                    dragSourceCardIndex = cardIndex;
                    draggedCards = validStack;

                    // Calculate card Y position using dynamic spacing
                    float availableHeight = startY - BOTTOM_MARGIN;
                    float[] spacings = calculateCardSpacings(col, availableHeight);
                    float cardX = startX + col * TABLEAU_SPACING;
                    float cardY = startY;
                    for (int i = 0; i < cardIndex; i++) {
                        cardY -= spacings[i];
                    }
                    cardY -= CARD_HEIGHT;

                    dragOffsetX = worldX - cardX;
                    dragOffsetY = worldY - cardY;
                    dragCurrentX = worldX;
                    dragCurrentY = worldY;
                }
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (menuOpen) return false;

        float[] world = screenToWorld(screenX, screenY);
        float worldX = world[0];
        float worldY = world[1];

        // Check undo icon click - trigger on touchUp for proper button behavior
        if (isOnUndoIcon(worldX, worldY) && canUndo()) {
            undo();
            // Reset drag state just in case
            isDragging = false;
            draggedCards = new ArrayList<>();
            return true;
        }

        if (isDragging && !draggedCards.isEmpty()) {
            // Find the best valid drop target (closest column that accepts the cards)
            int targetCol = findBestDropTarget(worldX, draggedCards.get(0), dragSourceCol);
            if (targetCol >= 0 && targetCol != dragSourceCol) {
                moveCards(dragSourceCol, dragSourceCardIndex, targetCol);
            }
        } else if (!isDragging && draggedCards != null && !draggedCards.isEmpty()) {
            // Was a tap, not a drag - check for quick tap auto-move
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTapTime) < TAP_THRESHOLD_MS) {
                // Quick tap - handled in touchDown with double-click detection
            }
        }

        // Reset drag state
        isDragging = false;
        draggedCards = new ArrayList<>();
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        isDragging = false;
        draggedCards = new ArrayList<>();
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (menuOpen) return false;

        float[] world = screenToWorld(screenX, screenY);
        float worldX = world[0];
        float worldY = world[1];

        if (draggedCards != null && !draggedCards.isEmpty()) {
            float dx = worldX - touchDownX;
            float dy = worldY - touchDownY;
            if (!isDragging && (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD)) {
                isDragging = true;
            }
            if (isDragging) {
                dragCurrentX = worldX;
                dragCurrentY = worldY;
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    // --- Hit Detection ---

    private boolean isOnMenuIcon(float worldX, float worldY) {
        float buttonMargin = 6f;
        float menuX = MENU_ICON_PADDING - buttonMargin;
        float menuY = VIRTUAL_HEIGHT - MENU_ICON_PADDING - MENU_ICON_SIZE - buttonMargin - buttonMargin;
        float buttonSize = MENU_ICON_SIZE + buttonMargin * 2;
        return worldX >= menuX && worldX <= menuX + buttonSize &&
               worldY >= menuY && worldY <= menuY + buttonSize;
    }

    private boolean isOnUndoIcon(float worldX, float worldY) {
        float buttonMargin = 6f;
        float x = undoIconX - buttonMargin;
        float y = undoIconY - buttonMargin;
        float buttonSize = UNDO_ICON_SIZE + buttonMargin * 2;
        return worldX >= x && worldX <= x + buttonSize &&
               worldY >= y && worldY <= y + buttonSize;
    }

    private boolean isOnStock(float worldX, float worldY) {
        return worldX >= stockX && worldX <= stockX + CARD_WIDTH &&
               worldY >= stockY && worldY <= stockY + CARD_HEIGHT;
    }

    private int[] getCardAtPosition(float worldX, float worldY) {
        float availableHeight = startY - BOTTOM_MARGIN;

        for (int col = 0; col < 10; col++) {
            List<Card> tableau = tableaus.get(col);
            float cardX = startX + col * TABLEAU_SPACING;

            if (worldX >= cardX && worldX <= cardX + CARD_WIDTH) {
                if (tableau.isEmpty()) continue;

                // Calculate card positions using dynamic spacing
                float[] spacings = calculateCardSpacings(col, availableHeight);
                float[] cardYPositions = new float[tableau.size()];

                float currentY = startY;
                for (int row = 0; row < tableau.size(); row++) {
                    cardYPositions[row] = currentY - CARD_HEIGHT;
                    if (row < tableau.size() - 1) {
                        currentY -= spacings[row];
                    }
                }

                // Check from top card down (reverse order for proper overlap detection)
                for (int row = tableau.size() - 1; row >= 0; row--) {
                    float cardY = cardYPositions[row];
                    float cardTop = cardY + CARD_HEIGHT;
                    float cardBottom = cardY;

                    // For non-top cards, only the visible portion is clickable
                    if (row < tableau.size() - 1) {
                        float spacing = spacings[row];
                        cardBottom = cardY + CARD_HEIGHT - spacing;
                    }

                    if (worldY >= cardBottom && worldY <= cardTop) {
                        return new int[]{col, row};
                    }
                }
            }
        }
        return null;
    }

    private int findBestDropTarget(float worldX, Card topCard, int sourceCol) {
        // Find the closest valid column to drop the card
        // Must be within 1.5x card width to count as a valid drop
        float maxDropDistance = CARD_WIDTH * 1.5f;
        int bestCol = -1;
        float bestDistance = Float.MAX_VALUE;

        for (int col = 0; col < 10; col++) {
            if (col == sourceCol) continue; // Skip source column

            // Check if this column can accept the card
            if (!canDropOnColumn(col, topCard)) continue;

            // Calculate distance to this column's center
            float colCenterX = startX + col * TABLEAU_SPACING + CARD_WIDTH / 2f;
            float distance = Math.abs(worldX - colCenterX);

            // Only consider if within max drop distance
            if (distance <= maxDropDistance && distance < bestDistance) {
                bestDistance = distance;
                bestCol = col;
            }
        }

        return bestCol;
    }

    // --- Undo System ---

    private void saveGameState() {
        moveHistory.add(new GameState(tableaus, stock, completedSuits, score, totalMoves));
    }

    private void undo() {
        if (moveHistory.size() <= 1) return; // Can't undo initial state

        // Debounce to prevent double-firing within 300ms
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUndoTime < UNDO_DEBOUNCE_MS) {
            return;
        }
        lastUndoTime = currentTime;

        // History contains states saved BEFORE each move
        // The last entry is the state before the most recent move
        // We restore that state and remove it from history
        GameState stateToRestore = moveHistory.remove(moveHistory.size() - 1);

        // Deep copy back from saved state
        tableaus = new ArrayList<>();
        for (List<Card> tableau : stateToRestore.tableaus) {
            List<Card> tableCopy = new ArrayList<>();
            for (Card card : tableau) {
                tableCopy.add(card.copy());
            }
            tableaus.add(tableCopy);
        }

        stock = new ArrayList<>();
        for (Card card : stateToRestore.stock) {
            stock.add(card.copy());
        }

        completedSuits = stateToRestore.completedSuits;

        // Restore the score and moves from the saved state, then apply undo penalty
        score = stateToRestore.score + POINTS_PER_MOVE;
        totalMoves = stateToRestore.totalMoves + 1;
        totalUndos++;
    }

    private boolean canUndo() {
        return moveHistory.size() > 1;
    }

    private boolean hasEmptyTableau() {
        for (List<Card> tableau : tableaus) {
            if (tableau.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // --- Stock Dealing ---

    private void dealFromStock() {
        if (stock.isEmpty()) return;

        // Check that all tableaus have at least one card (spider solitaire rule)
        for (List<Card> tableau : tableaus) {
            if (tableau.isEmpty()) {
                // Can't deal if any tableau is empty
                return;
            }
        }

        // Save state before dealing
        saveGameState();

        // Deal one card to each tableau (10 cards total per deal)
        for (int col = 0; col < 10; col++) {
            if (!stock.isEmpty()) {
                Card card = stock.remove(stock.size() - 1);
                card.setFaceUp(true);
                tableaus.get(col).add(card);
            }
        }

        // Count as a move
        totalMoves++;
        score += POINTS_PER_MOVE;

        // Check all columns for completed suits after dealing
        for (int col = 0; col < 10; col++) {
            checkAndRemoveCompletedSuit(col);
        }
    }

    // --- Stack Validation ---

    private List<Card> getValidStackFromCard(int col, int cardIndex) {
        List<Card> tableau = tableaus.get(col);
        List<Card> stack = new ArrayList<>();

        if (cardIndex < 0 || cardIndex >= tableau.size()) return null;

        Card firstCard = tableau.get(cardIndex);
        if (!firstCard.isFaceUp()) return null;

        stack.add(firstCard);

        // Check if all cards below form a same-suit descending sequence
        for (int i = cardIndex + 1; i < tableau.size(); i++) {
            Card prevCard = tableau.get(i - 1);
            Card card = tableau.get(i);

            if (!card.isFaceUp()) return null;
            if (card.getSuit() != prevCard.getSuit()) return null;
            if (card.getRank() != prevCard.getRank() - 1) return null;

            stack.add(card);
        }

        return stack;
    }

    private boolean canDropOnColumn(int targetCol, Card topDraggedCard) {
        List<Card> targetTableau = tableaus.get(targetCol);

        if (targetTableau.isEmpty()) {
            return true; // Can always drop on empty column
        }

        Card targetTopCard = targetTableau.get(targetTableau.size() - 1);
        // Can drop if target card is exactly one rank higher
        return targetTopCard.getRank() == topDraggedCard.getRank() + 1;
    }

    private void moveCards(int sourceCol, int sourceCardIndex, int targetCol) {
        // Save state before move
        saveGameState();

        List<Card> sourceTableau = tableaus.get(sourceCol);
        List<Card> targetTableau = tableaus.get(targetCol);

        // Move cards from source to target
        List<Card> cardsToMove = new ArrayList<>(sourceTableau.subList(sourceCardIndex, sourceTableau.size()));
        targetTableau.addAll(cardsToMove);

        // Remove from source
        sourceTableau.subList(sourceCardIndex, sourceTableau.size()).clear();

        // Flip the new top card if face down
        if (!sourceTableau.isEmpty()) {
            Card newTopCard = sourceTableau.get(sourceTableau.size() - 1);
            if (!newTopCard.isFaceUp()) {
                newTopCard.setFaceUp(true);
            }
        }

        // Track move and update score
        totalMoves++;
        score += POINTS_PER_MOVE;

        // Check for completed suit
        checkAndRemoveCompletedSuit(targetCol);
    }

    private void checkAndRemoveCompletedSuit(int col) {
        List<Card> tableau = tableaus.get(col);
        if (tableau.size() < 13) return;

        // Check if the last 13 cards form a complete same-suit sequence K to A
        int startIndex = tableau.size() - 13;
        Card firstCard = tableau.get(startIndex);

        if (firstCard.getRank() != 13) return; // Must start with King

        Suit suit = firstCard.getSuit();
        for (int i = 0; i < 13; i++) {
            Card card = tableau.get(startIndex + i);
            if (card.getSuit() != suit || card.getRank() != 13 - i || !card.isFaceUp()) {
                return;
            }
        }

        // Found a complete suit! Remove from tableau
        tableau.subList(startIndex, tableau.size()).clear();

        // Flip new top card
        if (!tableau.isEmpty()) {
            Card newTop = tableau.get(tableau.size() - 1);
            if (!newTop.isFaceUp()) {
                newTop.setFaceUp(true);
            }
        }

        // Update scoring
        completedSuits++;
        score += POINTS_PER_COMPLETED_SUIT;

        // Check for win
        if (completedSuits >= requiredSuitsToWin) {
            showWinDialog();
        }
    }

    private void showWinDialog() {
        // Create game result
        GameResult result = new GameResult(config, GameResult.Outcome.WON, score, totalMoves, totalUndos, elapsedGameTime, completedSuits);

        // Navigate based on game mode
        if (config.getMode() == GameConfig.GameMode.SOLO_PRACTICE) {
            // Solo Practice shows result screen with play again option (pass prior result for comparison)
            game.setScreen(new ResultScreen(game, result, priorResult));
            dispose();
        } else {
            // Daily Grind returns to main menu (future: could show leaderboard)
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    // --- Auto-Move ---

    private void tryAutoMove(int col, int cardIndex) {
        List<Card> stack = getValidStackFromCard(col, cardIndex);
        if (stack == null || stack.isEmpty()) return;

        Card topCard = stack.get(0);
        int bestTarget = -1;
        int priority = Integer.MAX_VALUE;

        for (int targetCol = 0; targetCol < 10; targetCol++) {
            if (targetCol == col) continue;

            List<Card> targetTableau = tableaus.get(targetCol);

            if (targetTableau.isEmpty()) {
                // Priority 3: Empty column (only if we don't have a better option)
                if (priority > 3) {
                    bestTarget = targetCol;
                    priority = 3;
                }
            } else {
                Card targetTop = targetTableau.get(targetTableau.size() - 1);
                if (targetTop.getRank() == topCard.getRank() + 1) {
                    if (targetTop.getSuit() == topCard.getSuit()) {
                        // Priority 1: Same suit, one rank higher
                        if (priority > 1) {
                            bestTarget = targetCol;
                            priority = 1;
                        }
                    } else {
                        // Priority 2: Different suit, one rank higher
                        if (priority > 2) {
                            bestTarget = targetCol;
                            priority = 2;
                        }
                    }
                }
            }
        }

        if (bestTarget >= 0) {
            moveCards(col, cardIndex, bestTarget);
        }
    }

    // --- Inner classes for card data model ---

    enum Suit {
        SPADES("S", new Color(0.1f, 0.1f, 0.1f, 1f)),      // Dark gray/black
        HEARTS("H", new Color(0.9f, 0.1f, 0.1f, 1f)),      // Red
        DIAMONDS("D", new Color(0.1f, 0.4f, 0.9f, 1f)),    // Blue
        CLUBS("C", new Color(0.1f, 0.6f, 0.1f, 1f)),       // Green
        HORSES("O", new Color(0.7f, 0.4f, 0.0f, 1f)),      // Orange (horseshoe)
        BALLS("B", new Color(0.6f, 0.1f, 0.6f, 1f));       // Purple

        private final String symbol;
        private final Color color;

        Suit(String symbol, Color color) {
            this.symbol = symbol;
            this.color = color;
        }

        public String getSymbol() {
            return symbol;
        }

        public Color getColor() {
            return color;
        }
    }

    static class Card {
        private final Suit suit;
        private final int rank; // 1 = Ace, 11 = Jack, 12 = Queen, 13 = King
        private boolean faceUp;

        public Card(Suit suit, int rank) {
            this.suit = suit;
            this.rank = rank;
            this.faceUp = false;
        }

        public Suit getSuit() {
            return suit;
        }

        public int getRank() {
            return rank;
        }

        public boolean isFaceUp() {
            return faceUp;
        }

        public void setFaceUp(boolean faceUp) {
            this.faceUp = faceUp;
        }

        public String getRankSymbol() {
            switch (rank) {
                case 1: return "A";
                case 11: return "J";
                case 12: return "Q";
                case 13: return "K";
                default: return String.valueOf(rank);
            }
        }

        public Card copy() {
            Card copy = new Card(this.suit, this.rank);
            copy.faceUp = this.faceUp;
            return copy;
        }
    }

    // Stores a complete snapshot of game state for undo
    static class GameState {
        final List<List<Card>> tableaus;
        final List<Card> stock;
        final int completedSuits;
        final int score;
        final int totalMoves;

        GameState(List<List<Card>> tableaus, List<Card> stock, int completedSuits, int score, int totalMoves) {
            // Deep copy tableaus
            this.tableaus = new ArrayList<>();
            for (List<Card> tableau : tableaus) {
                List<Card> tableCopy = new ArrayList<>();
                for (Card card : tableau) {
                    tableCopy.add(card.copy());
                }
                this.tableaus.add(tableCopy);
            }

            // Deep copy stock
            this.stock = new ArrayList<>();
            for (Card card : stock) {
                this.stock.add(card.copy());
            }

            this.completedSuits = completedSuits;
            this.score = score;
            this.totalMoves = totalMoves;
        }
    }
}
