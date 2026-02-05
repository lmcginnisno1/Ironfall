package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import dev.lmcginninsno1.ironfall.buildings.*;
import dev.lmcginninsno1.ironfall.input.*;
import dev.lmcginninsno1.ironfall.render.*;
import dev.lmcginninsno1.ironfall.selection.*;
import dev.lmcginninsno1.ironfall.placement.*;

import java.util.function.Supplier;

public class IronfallGame extends ApplicationAdapter {

    // Core rendering
    private SpriteBatch batch;
    private BitmapFont font;

    // World
    private TileEngine engine;
    private BuildingManager buildingManager;

    // Cameras
    private OrthographicCamera hudCamera;
    private CameraController cameraController;

    // Subsystems
    private InputController inputController;
    private SelectionManager selectionManager;
    private SelectionRenderer selectionRenderer;
    private WorldRenderer worldRenderer;
    private OverlayRenderer overlayRenderer;
    private PlacementController placementController;

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

    // Supplier to disable camera dragging during placement
    private final Supplier<Boolean> canDragCamera = () -> mode == GameMode.NORMAL;

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

        buildingManager = new BuildingManager();

        // Subsystems
        selectionManager = new SelectionManager(this, buildingManager);
        inputController = new InputController(this, selectionManager);
        selectionRenderer = new SelectionRenderer(selectionManager);
        worldRenderer = new WorldRenderer(this, engine, buildingManager);
        overlayRenderer = new OverlayRenderer(this, selectionManager, font);
        cameraController = new CameraController(engine.getCamera(), () -> mode == GameMode.NORMAL);
        placementController = new PlacementController(this, engine, buildingManager);

        // Start game by placing core
        mode = GameMode.PLACING_CORE;

        // --- WORLD GENERATION ---
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double noise = Math.random();
                engine.setTile(x, y, noise < 0.85 ? TileType.DIRT.id : TileType.SAND.id);
            }
        }

        generateVein(engine, TileType.COAL.id, 50, 60);
        generateVein(engine, TileType.IRON.id, 30, 60);
        generateVein(engine, TileType.COPPER.id, 50, 60);
        generateVein(engine, TileType.STONE.id, 100, 100);
    }

    @Override
    public void render() {

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        float delta = Gdx.graphics.getDeltaTime();

        // Update tile coords
        screenToTile(Gdx.input.getX(), Gdx.input.getY());

        // Update subsystems
        engine.update();
        cameraController.update(delta);
        inputController.update();
        selectionManager.update();
        buildingManager.update(delta);
        placementController.update();

        // --- WORLD RENDERING ---
        batch.setProjectionMatrix(engine.getCamera().combined);
        batch.begin();
        worldRenderer.render(batch);
        selectionRenderer.render(batch);
        placementController.render(batch); // ghost previews
        batch.end();

        // --- HUD / OVERLAYS ---
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        overlayRenderer.render(batch);

        // Tooltip near mouse
        if (tileX >= 0 && tileX < width && tileY >= 0 && tileY < height) {
            TileType t = TileType.fromId(engine.getTile(tileX, tileY));
            font.draw(batch,
                t.name + " (" + tileX + ", " + tileY + ")",
                Gdx.input.getX() + 16,
                screenHeight - Gdx.input.getY() + 16);
        }

        batch.end();
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

    private void generateVein(TileEngine engine, int tileId, int seedCount, int veinLength) {
        for (int i = 0; i < seedCount; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            for (int v = 0; v < veinLength; v++) {
                TileType t = TileType.fromId(engine.getTile(x, y));

                if (t == TileType.DIRT || t == TileType.SAND)
                    engine.setTile(x, y, tileId);

                int dir = (int) (Math.random() * 4);
                switch (dir) {
                    case 0 -> x++;
                    case 1 -> x--;
                    case 2 -> y++;
                    case 3 -> y--;
                }

                x = Math.max(0, Math.min(x, width - 1));
                y = Math.max(0, Math.min(y, height - 1));
            }
        }
    }
}
