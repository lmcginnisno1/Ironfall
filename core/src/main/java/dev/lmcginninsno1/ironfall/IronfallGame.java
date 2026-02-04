package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.lmcginninsno1.ironfall.buildings.BuildingManager;
import dev.lmcginninsno1.ironfall.buildings.BasicMiner;

public class IronfallGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private TileEngine engine;
    private BitmapFont font;

    private OrthographicCamera hudCamera;
    private BuildingManager buildingManager;

    private Texture minerTexture;
    private TextureRegion minerSprite;

    private GameMode mode = GameMode.NORMAL;

    private final int width = 480;
    private final int height = 270;

    @Override
    public void create() {
        batch = new SpriteBatch();
        engine = new TileEngine(width, height);

        font = new BitmapFont();
        font.getData().setScale(1f);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        buildingManager = new BuildingManager();

        // --- WORLD GENERATION ---
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double noise = Math.random();
                if (noise < 0.85) engine.setTile(x, y, 0);
                else engine.setTile(x, y, 1);
            }
        }

        generateVeins(engine, 11, 50, 60);
        generateVeins(engine, 12, 30, 60);
        generateVeins(engine, 13, 50, 60);
        generateVeins(engine, 10, 100, 100);

        // --- LOAD BUILDING SPRITES ---
        minerTexture = new Texture("buildings/miner.png");
        minerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        minerSprite = new TextureRegion(minerTexture);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Gdx.graphics.getDeltaTime();
        engine.update(delta);

        // --- INPUT: ENTER PLACING MODE ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            mode = GameMode.PLACING_MINER;
        }

        // --- MOUSE POSITION IN TILE COORDS ---
        Vector2 tilePos = engine.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        int tx = (int) tilePos.x;
        int ty = (int) tilePos.y;

        // --- RENDER WORLD ---
        engine.render(batch);

        // --- RENDER BUILDINGS + GHOST ---
        batch.setProjectionMatrix(engine.getCamera().combined);
        batch.begin();

        buildingManager.update(delta);
        buildingManager.render(batch);

        // --- GHOST BUILDING ---
        if (mode == GameMode.PLACING_MINER) {
            boolean valid = canPlaceMinerAt(tx, ty);

            if (valid) batch.setColor(0f, 1f, 0f, 0.5f);   // green tint
            else batch.setColor(1f, 0f, 0f, 0.5f);         // red tint

            batch.draw(minerSprite, tx * 16, ty * 16, 32, 32);
            batch.setColor(1f, 1f, 1f, 1f);

            // --- CLICK TO PLACE ---
            if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && valid) {
                buildingManager.add(new BasicMiner(tx, ty, 2, 2, minerSprite, engine));
                mode = GameMode.NORMAL;
            }

            // --- RIGHT CLICK TO CANCEL ---
            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                mode = GameMode.NORMAL;
            }
        }

        batch.end();

        // --- HUD ---
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
            int id = engine.getTile(tx, ty);
            String tooltip = tileName(id) + " (" + tx + ", " + ty + ")";

            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            font.draw(batch, tooltip,
                Gdx.input.getX() + 16,
                Gdx.graphics.getHeight() - Gdx.input.getY() + 16);
            batch.end();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        engine.dispose();
        font.dispose();
        minerTexture.dispose();
    }

    // --- VALIDATION: CAN PLACE MINER HERE? ---
    private boolean canPlaceMinerAt(int x, int y) {

        // --- 1. Prevent placing on top of another building ---
        if (buildingManager.isOccupied(x, y, 2, 2)) {
            return false;
        }

        int oreId = -1;
        int oreCount = 0;

        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int id = engine.getTile(x + dx, y + dy);

                if (id >= 11 && id <= 13) {
                    if (oreId == -1) oreId = id;
                    if (id != oreId) return false;
                    oreCount++;
                }
            }
        }

        return oreCount >= 1;
    }

    private void generateVeins(TileEngine engine, int tileId, int seedCount, int veinLength) {
        for (int i = 0; i < seedCount; i++) {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);

            for (int v = 0; v < veinLength; v++) {
                if (engine.getTile(x, y) <= 9) engine.setTile(x, y, tileId);

                int dir = (int)(Math.random() * 4);
                switch (dir) {
                    case 0 -> x++;
                    case 1 -> x--;
                    case 2 -> y++;
                    case 3 -> y--;
                }

                if (x < 0) x = 0;
                if (x >= width) x = width - 1;
                if (y < 0) y = 0;
                if (y >= height) y = height - 1;
            }
        }
    }

    private String tileName(int id) {
        return switch (id) {
            case 0 -> "Dirt";
            case 1 -> "Sand";
            case 10 -> "Stone";
            case 11 -> "Coal";
            case 12 -> "Iron";
            case 13 -> "Copper";
            default -> "Empty";
        };
    }
}
