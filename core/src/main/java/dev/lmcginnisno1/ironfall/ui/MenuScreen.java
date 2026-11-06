package dev.lmcginnisno1.ironfall.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Full-screen menu drawn in HUD space. Two variants:
 * MAIN (on startup, and after "Main Menu") and PAUSE (Esc during play,
 * drawn over the frozen world). While open, the world doesn't update.
 */
public class MenuScreen {

    public enum Kind { MAIN, PAUSE }

    private static final int BUTTON_W = 260;
    private static final int BUTTON_H = 44;
    private static final int BUTTON_GAP = 14;

    private final IronfallGame game;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private Kind kind = null; // null = closed

    // "New World" over an existing save needs a second click within this many seconds.
    private float confirmNewWorldTimer = 0f;

    private record Button(String label, String subtitle, boolean enabled, Runnable action, Rectangle rect) {}

    private final List<Button> buttons = new ArrayList<>();

    public MenuScreen(IronfallGame game, BitmapFont font) {
        this.game = game;
        this.font = font;
    }

    public void open(Kind kind) {
        this.kind = kind;
        confirmNewWorldTimer = 0f;
    }

    public void close() {
        kind = null;
    }

    public boolean isOpen() {
        return kind != null;
    }

    public Kind getKind() {
        return kind;
    }

    /** Rebuilds the button list for the current state and screen size. */
    private void layoutButtons() {
        buttons.clear();

        if (kind == Kind.MAIN) {
            boolean hasSave = game.hasSave();
            String saved = hasSave ? "Saved " + formatTime(game.saveTimestamp()) : "No saved world";
            add("Continue", saved, hasSave, game::continueGame);

            boolean confirming = confirmNewWorldTimer > 0f;
            String newSub = confirming ? "Click again to replace your saved world"
                : (hasSave ? "Replaces your saved world" : null);
            add(confirming ? "Confirm New World" : "New World", newSub, true, () -> {
                if (hasSave && confirmNewWorldTimer <= 0f) {
                    confirmNewWorldTimer = 3f;
                } else {
                    game.newGame();
                }
            });

            add("Quit", null, true, () -> Gdx.app.exit());
        } else {
            add("Resume", null, true, this::close);
            add("Save", null, true, () -> game.saveGame(true));
            add("Save & Main Menu", null, true, game::returnToMainMenu);
            add("Save & Quit", null, true, () -> Gdx.app.exit()); // pause() saves on exit
        }

        // Centre the column below the title
        float total = buttons.size() * BUTTON_H + (buttons.size() - 1) * BUTTON_GAP;
        float x = (game.screenWidth - BUTTON_W) / 2f;
        float top = game.screenHeight / 2f + total / 2f - 40;
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).rect().set(x, top - BUTTON_H - i * (BUTTON_H + BUTTON_GAP), BUTTON_W, BUTTON_H);
        }
    }

    private void add(String label, String subtitle, boolean enabled, Runnable action) {
        buttons.add(new Button(label, subtitle, enabled, action, new Rectangle()));
    }

    public void update(float delta) {
        if (!isOpen()) return;
        if (confirmNewWorldTimer > 0f) confirmNewWorldTimer -= delta;

        if (kind == Kind.PAUSE && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
            return;
        }

        layoutButtons();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            float mx = Gdx.input.getX();
            float my = game.screenHeight - Gdx.input.getY();
            for (Button b : buttons) {
                if (b.enabled() && b.rect().contains(mx, my)) {
                    b.action().run();
                    return;
                }
            }
        }
    }

    /** Expects the batch to be running with the HUD projection. */
    public void render(SpriteBatch batch) {
        if (!isOpen()) return;
        layoutButtons();

        float w = game.screenWidth;
        float h = game.screenHeight;

        // Background: solid on the main menu, a dim layer over the world when paused
        if (kind == Kind.MAIN) batch.setColor(0.09f, 0.09f, 0.12f, 1f);
        else batch.setColor(0f, 0f, 0f, 0.6f);
        batch.draw(Assets.whitePixel, 0, 0, w, h);

        // Title
        String title = kind == Kind.MAIN ? "IRONFALL" : "PAUSED";
        float topButton = buttons.isEmpty() ? h / 2f : buttons.get(0).rect().y + BUTTON_H;
        font.getData().setScale(kind == Kind.MAIN ? 4f : 3f);
        font.setColor(1f, 0.85f, 0.2f, 1f);
        drawCentered(batch, title, w / 2f, topButton + 70);
        font.getData().setScale(1f);

        // Buttons
        float mx = Gdx.input.getX();
        float my = h - Gdx.input.getY();
        for (Button b : buttons) {
            Rectangle r = b.rect();
            boolean hover = b.enabled() && r.contains(mx, my);

            if (!b.enabled()) batch.setColor(0.18f, 0.18f, 0.2f, 0.9f);
            else if (hover) batch.setColor(0.35f, 0.32f, 0.22f, 0.95f);
            else batch.setColor(0.22f, 0.22f, 0.26f, 0.95f);
            batch.draw(Assets.whitePixel, r.x, r.y, r.width, r.height);

            // Left accent bar
            batch.setColor(b.enabled() ? 1f : 0.4f, b.enabled() ? 0.85f : 0.4f, b.enabled() ? 0.2f : 0.4f, 1f);
            batch.draw(Assets.whitePixel, r.x, r.y, 4, r.height);

            if (b.enabled()) font.setColor(1f, 1f, 1f, 1f);
            else font.setColor(0.5f, 0.5f, 0.5f, 1f);

            if (b.subtitle() == null) {
                drawCentered(batch, b.label(), r.x + r.width / 2f, r.y + r.height / 2f + 6);
            } else {
                drawCentered(batch, b.label(), r.x + r.width / 2f, r.y + r.height - 8);
                font.setColor(0.65f, 0.65f, 0.7f, 1f);
                font.getData().setScale(0.85f);
                drawCentered(batch, b.subtitle(), r.x + r.width / 2f, r.y + 17);
                font.getData().setScale(1f);
            }
        }

        // Version and controls hint
        font.setColor(0.5f, 0.5f, 0.55f, 1f);
        font.draw(batch, "v" + IronfallGame.version(), 12, 22);
        if (kind == Kind.PAUSE) {
            drawCentered(batch, "Esc to resume  -  F5 quick save  -  F9 load last save", w / 2f, 40);
        }

        font.setColor(1f, 1f, 1f, 1f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawCentered(SpriteBatch batch, String text, float cx, float baselineY) {
        layout.setText(font, text);
        font.draw(batch, text, cx - layout.width / 2f, baselineY);
    }

    private static String formatTime(long millis) {
        if (millis <= 0) return "";
        return new SimpleDateFormat("MMM d, h:mm a").format(new Date(millis));
    }
}
