package dev.lmcginnisno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import dev.lmcginnisno1.ironfall.buildings.*;
import dev.lmcginnisno1.ironfall.game.GameMode;
import dev.lmcginnisno1.ironfall.input.*;
import dev.lmcginnisno1.ironfall.render.*;
import dev.lmcginnisno1.ironfall.selection.*;
import dev.lmcginnisno1.ironfall.placement.*;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;
import dev.lmcginnisno1.ironfall.tiles.TileType;
import dev.lmcginnisno1.ironfall.worldgen.WorldGenerator;

import static dev.lmcginnisno1.ironfall.render.TextUtil.drawOutlined;

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
        inputController = new InputController(this, selectionManager);
        selectionRenderer = new SelectionRenderer(selectionManager);
        worldRenderer = new WorldRenderer(this, engine, buildingManager);
        overlayRenderer = new OverlayRenderer(this, selectionManager, font);
        cameraController = new CameraController(
            engine.getCamera(),
            () -> mode == GameMode.NORMAL,
            width * TileEngine.TILE_SIZE,
            height * TileEngine.TILE_SIZE
        );
        placementController = new PlacementController(this, engine, buildingManager);

        // Generate the world and pick the best location for the core
        WorldGenerator.generate(engine, buildingManager);

        // start camera centered on wherever the core is placed
        Core core = buildingManager.getCore();
        OrthographicCamera cam = engine.getCamera();

        float cx = (core.x + core.width  / 2f) * TileEngine.TILE_SIZE;
        float cy = (core.y + core.height / 2f) * TileEngine.TILE_SIZE;

        cam.position.set(cx, cy, 0);
        cam.update();

        // After worldgen, game starts in normal mode
        mode = GameMode.NORMAL;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Gdx.graphics.getDeltaTime();

        cameraController.update(delta);

        OrthographicCamera cam = engine.getCamera();
        cam.viewportWidth = Gdx.graphics.getWidth();
        cam.viewportHeight = Gdx.graphics.getHeight();
        cam.update();

        engine.update();
        inputController.update();
        selectionManager.update();
        buildingManager.update(delta);
        placementController.update();

        screenToTile(Gdx.input.getX(), Gdx.input.getY());

        batch.setProjectionMatrix(engine.getCamera().combined);
        batch.begin();
        worldRenderer.render(batch);
        selectionRenderer.render(batch);
        placementController.render(batch);
        batch.end();

        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

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
