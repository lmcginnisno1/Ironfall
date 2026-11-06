package dev.lmcginnisno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import dev.lmcginnisno1.ironfall.buildings.*;
import dev.lmcginnisno1.ironfall.game.GameMode;
import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.input.*;
import dev.lmcginnisno1.ironfall.render.*;
import dev.lmcginnisno1.ironfall.selection.*;
import dev.lmcginnisno1.ironfall.placement.*;
import dev.lmcginnisno1.ironfall.save.SaveManager;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;
import dev.lmcginnisno1.ironfall.tiles.TileType;
import dev.lmcginnisno1.ironfall.ui.MenuScreen;
import dev.lmcginnisno1.ironfall.worldgen.WorldGenerator;

import static dev.lmcginnisno1.ironfall.render.TextUtil.drawOutlined;

public class IronfallGame extends ApplicationAdapter {

    // Core rendering
    private SpriteBatch batch;
    private BitmapFont font;

    // World
    public TileEngine engine;
    public BuildingManager buildingManager;

    // Cameras
    public OrthographicCamera hudCamera;
    private CameraController cameraController;

    // Subsystems
    private InputController inputController;
    private SelectionManager selectionManager;
    private SelectionRenderer selectionRenderer;
    private WorldRenderer worldRenderer;
    private OverlayRenderer overlayRenderer;
    public PlacementController placementController;
    private MenuScreen menu;

    // Game state
    public GameMode mode = GameMode.NORMAL;

    private final int width = 480;
    private final int height = 270;

    // Screen-space info
    public int tileX;
    public int tileY;
    public int screenWidth;
    public int screenHeight;

    // Debug
    public boolean showGrid = false;

    // Enough for one Basic Miner ($50) plus a short belt run to the Core,
    // so a fresh save isn't a softlock before you've sold anything.
    public int credits = Balance.STARTING_CREDITS;

    // Seed the current world was generated from (kept in saves).
    public long worldSeed;

    // False until a world has been generated or loaded. While false nothing
    // is drawn or saved, so the empty startup world can never overwrite a save.
    private boolean worldReady = false;

    // Saving
    private SaveManager saveManager;
    private float autosaveTimer = 0f;

    // Short status message shown at the top of the screen (e.g. "Game saved")
    private String message = null;
    private float messageTime = 0f;
    private final GlyphLayout messageLayout = new GlyphLayout();

    /** Game version from the JAR manifest, or "dev" when running from Gradle/IDE. */
    public static String version() {
        String v = IronfallGame.class.getPackage().getImplementationVersion();
        return v != null ? v : "dev";
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        engine = new TileEngine(width, height);

        Assets.load();

        font = new BitmapFont();
        font.getData().setScale(1f);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        buildingManager = new BuildingManager(width, height);

        // Subsystems
        selectionManager = new SelectionManager(this, buildingManager);
        worldRenderer = new WorldRenderer(this, engine, buildingManager);
        overlayRenderer = new OverlayRenderer(this, selectionManager, font);
        inputController = new InputController(this, selectionManager, overlayRenderer);
        selectionRenderer = new SelectionRenderer(selectionManager);
        menu = new MenuScreen(this, font);
        cameraController = new CameraController(
            engine.getCamera(),
            () -> mode == GameMode.NORMAL && !menu.isOpen(),
            () -> !menu.isOpen(),
            width * TileEngine.TILE_SIZE,
            height * TileEngine.TILE_SIZE
        );
        placementController = new PlacementController(this, buildingManager);

        saveManager = new SaveManager(this);

        menu.open(MenuScreen.Kind.MAIN);
    }

    // ---------------------------------------------------------------
    // World lifecycle: menu actions, new / save / load
    // ---------------------------------------------------------------

    public boolean hasSave() {
        return saveManager.hasSave();
    }

    /** Last-modified time of the save file in millis, or 0 if there isn't one. */
    public long saveTimestamp() {
        return saveManager.hasSave() ? saveManager.saveFile().lastModified() : 0L;
    }

    /** Main menu "Continue": loads the save and starts playing. */
    public void continueGame() {
        try {
            saveManager.load();
            worldReady = true;
            autosaveTimer = 0f;
            resetTransientState();
            menu.close();
        } catch (Exception e) {
            Gdx.app.error("Save", "Couldn't load save", e);
            setAsideBrokenSave();
            showMessage("Couldn't load save: " + e.getMessage() + " (kept as world.sav.broken)");
        }
    }

    /** Main menu "New World": generates a fresh world and saves it straight away. */
    public void newGame() {
        startNewWorld(MathUtils.random.nextLong());
        worldReady = true;
        menu.close();
        saveGame(false);
        showMessage("New world started");
    }

    /** Pause menu "Save & Main Menu". */
    public void returnToMainMenu() {
        saveGame(true);
        worldReady = false;
        resetTransientState();
        menu.open(MenuScreen.Kind.MAIN);
    }

    public void openPauseMenu() {
        if (worldReady) menu.open(MenuScreen.Kind.PAUSE);
    }

    /** Throws away the current world and generates a new one from {@code seed}. */
    private void startNewWorld(long seed) {
        buildingManager.clear();
        worldSeed = seed;
        WorldGenerator.generate(engine, buildingManager, seed);
        credits = Balance.STARTING_CREDITS;

        // start camera centered on wherever the core is placed
        Core core = buildingManager.getCore();
        OrthographicCamera cam = engine.getCamera();

        float cx = (core.x + core.width  / 2f) * TileEngine.TILE_SIZE;
        float cy = (core.y + core.height / 2f) * TileEngine.TILE_SIZE;

        cam.position.set(cx, cy, 0);
        cam.zoom = 1f;
        cam.update();

        resetTransientState();
    }

    public void saveGame(boolean manual) {
        if (!worldReady) return;
        try {
            saveManager.save();
            autosaveTimer = 0f;
            showMessage(manual ? "Game saved" : "Autosaved");
        } catch (Exception e) {
            Gdx.app.error("Save", "Save failed", e);
            showMessage("Save failed: " + e.getMessage());
        }
    }

    /** F9: reload the last save, discarding unsaved progress. */
    public void loadGame() {
        if (!worldReady) return;
        if (!saveManager.hasSave()) {
            showMessage("No save found");
            return;
        }
        try {
            saveManager.load();
            resetTransientState();
            autosaveTimer = 0f;
            showMessage("Save loaded");
        } catch (Exception e) {
            Gdx.app.error("Save", "Load failed", e);
            showMessage("Load failed: " + e.getMessage());
        }
    }

    /** Keeps a save that failed to load, so a new world doesn't overwrite it. */
    private void setAsideBrokenSave() {
        try {
            FileHandle file = saveManager.saveFile();
            file.moveTo(file.sibling(file.name() + ".broken"));
        } catch (Exception e) {
            Gdx.app.error("Save", "Couldn't move broken save aside", e);
        }
    }

    /** Clears selections, placement and UI drags that refer to the old world. */
    private void resetTransientState() {
        selectionManager.clearSelection();
        placementController.cancel();
        overlayRenderer.stopDragging();
        mode = GameMode.NORMAL;
    }

    public void showMessage(String text) {
        message = text;
        messageTime = 3f;
    }

    // ---------------------------------------------------------------
    // Frame
    // ---------------------------------------------------------------

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Gdx.graphics.getDeltaTime();
        if (messageTime > 0f) messageTime -= delta;

        OrthographicCamera cam = engine.getCamera();
        cam.viewportWidth = Gdx.graphics.getWidth();
        cam.viewportHeight = Gdx.graphics.getHeight();

        if (menu.isOpen()) {
            // The world is frozen while any menu is open.
            menu.update(delta);
            cam.update();
        } else {
            autosaveTimer += delta;
            if (autosaveTimer >= SaveManager.AUTOSAVE_INTERVAL) {
                saveGame(false);
            }

            cameraController.update(delta);
            cam.update();

            engine.update();
            inputController.update();
            selectionManager.update();
            buildingManager.update(delta);
            // placementController.update() is NOT called here -- InputController
            // already calls it internally (via handlePlacementMode()) whenever
            // mode == PLACING_GENERIC. Calling it again here was causing it to
            // run twice per frame, silently double-firing anything unprotected
            // by its own state guards -- notably Q/E rotation, which isn't
            // idempotent within a frame the way placement/drag-start are.

            screenToTile(Gdx.input.getX(), Gdx.input.getY());
        }

        // A menu action above may have closed or swapped the world this frame.
        boolean showWorld = worldReady && (!menu.isOpen() || menu.getKind() == MenuScreen.Kind.PAUSE);

        if (showWorld) {
            batch.setProjectionMatrix(cam.combined);
            batch.begin();
            worldRenderer.render(batch);
            selectionRenderer.render(batch);
            placementController.render(batch);
            batch.end();
        }

        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        if (showWorld && !menu.isOpen()) {
            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            overlayRenderer.render(batch);

            if (tileX >= 0 && tileX < width && tileY >= 0 && tileY < height) {
                TileType t = TileType.fromId(engine.getTile(tileX, tileY));
                drawOutlined(
                    font,
                    batch,
                    t.name + " (" + tileX + ", " + tileY + ")",
                    Gdx.input.getX() + 16,
                    screenHeight - Gdx.input.getY() + 16
                );
            }

            batch.end();
        }

        // Menu and status message always draw last, on top, in HUD space.
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        menu.render(batch);
        drawMessage();
        batch.end();
    }

    private void drawMessage() {
        if (message == null || messageTime <= 0f) return;
        messageLayout.setText(font, message);
        float mx = (screenWidth - messageLayout.width) / 2f;
        drawOutlined(font, batch, message, mx, screenHeight - 24);
        font.setColor(1f, 1f, 1f, 1f);
    }

    // Called when the window closes (desktop) or the app goes to the background (Android).
    @Override
    public void pause() {
        saveGame(false);
    }

    @Override
    public void dispose() {
        batch.dispose();
        engine.dispose();
        font.dispose();
        Assets.dispose();
    }

    public void screenToTile(int mx, int my) {
        Vector2 tilePos = engine.screenToWorld(mx, my);
        tileX = (int) tilePos.x;
        tileY = (int) tilePos.y;
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        hudCamera.setToOrtho(false, width, height);
    }
}
