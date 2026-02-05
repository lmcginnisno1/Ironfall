package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.lmcginninsno1.ironfall.buildings.*;

import java.util.ArrayList;
import java.util.function.Supplier;

public class IronfallGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private TileEngine engine;
    private BitmapFont font;

    private OrthographicCamera hudCamera;
    private BuildingManager buildingManager;

    private GameMode mode = GameMode.NORMAL;

    private final int width = 480;
    private final int height = 270;

    // Conveyor drag state
    private boolean draggingConveyor = false;
    private int dragStartX, dragStartY;

    // Mouse state tracking for release detection
    private boolean prevLeftDown = false;

    //supplier to disable camera dragging when placing buildings
    private final Supplier<Boolean> canDragCamera = () -> mode == GameMode.NORMAL;

    @Override
    public void create() {
        batch = new SpriteBatch();
        engine = new TileEngine(width, height, canDragCamera);

        Assets.load();

        font = new BitmapFont();
        font.getData().setScale(1f);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        buildingManager = new BuildingManager();

        // Start game by placing core
        mode = GameMode.PLACING_CORE;

        // --- WORLD GENERATION ---
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double noise = Math.random();
                if (noise < 0.85)
                    engine.setTile(x, y, TileType.DIRT.id);
                else
                    engine.setTile(x, y, TileType.SAND.id);
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
        engine.update(delta);

        // --- INPUT: ENTER PLACING MODES ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.M) && mode == GameMode.NORMAL)
            mode = GameMode.PLACING_MINER;

        if (Gdx.input.isKeyJustPressed(Input.Keys.C) && mode == GameMode.NORMAL)
            mode = GameMode.PLACING_CONVEYOR;

        // --- MOUSE POSITION IN TILE COORDS ---
        Vector2 tilePos = engine.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        int tx = (int) tilePos.x;
        int ty = (int) tilePos.y;

        // --- Track mouse state for release detection ---
        boolean leftDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean leftJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean leftJustReleased = prevLeftDown && !leftDown;
        prevLeftDown = leftDown;

        // --- RENDER WORLD ---
        engine.render(batch);

        // --- RENDER BUILDINGS + GHOST ---
        batch.setProjectionMatrix(engine.getCamera().combined);
        batch.begin();

        buildingManager.update(delta);
        buildingManager.render(batch);

        if (mode == GameMode.PLACING_MINER) {
            int px = tx - 1;
            int py = ty - 1;

            boolean valid = buildingManager.canPlace(px, py, 2, 2) &&
                oreCheck(px, py, 2, 2);

            batch.setColor(valid ? 0f : 1f, valid ? 1f : 0f, 0f, 0.5f);
            batch.draw(Assets.basicMiner, px * 16, py * 16, 32, 32);
            batch.setColor(1f, 1f, 1f, 1f);

            if (leftJustPressed && valid) {
                buildingManager.place(new BasicMiner(px, py, engine));
                mode = GameMode.NORMAL;
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT))
                mode = GameMode.NORMAL;
        }

        if (mode == GameMode.PLACING_CORE) {
            int px = tx - 2;
            int py = ty - 2;

            boolean valid = buildingManager.canPlace(px, py, 4, 4);

            batch.setColor(valid ? 0f : 1f, valid ? 1f : 0f, 0f, 0.5f);
            batch.draw(Assets.core, px * 16, py * 16, 64, 64);
            batch.setColor(1f, 1f, 1f, 1f);

            if (leftJustPressed && valid) {
                buildingManager.place(new Core(px, py));
                mode = GameMode.NORMAL;
            }

            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT))
                mode = GameMode.NORMAL;
        }

        if (mode == GameMode.PLACING_CONVEYOR) {
            // Start drag
            if (leftJustPressed) {
                draggingConveyor = true;
                dragStartX = tx;
                dragStartY = ty;
            }

            // While dragging, draw ghost path
            if (draggingConveyor) {
                ArrayList<Vector2> path = computeConveyorPath(dragStartX, dragStartY, tx, ty);

                for (int i = 0; i < path.size(); i++) {
                    Vector2 p = path.get(i);

                    Conveyor.Direction dir = getDirectionForIndex(path, i);

                    batch.setColor(1f, 1f, 1f, 0.5f);
                    batch.draw(getConveyorSprite(dir), p.x * 16, p.y * 16, 16, 16);
                }

                batch.setColor(1f, 1f, 1f, 1f);
            }

            // Release drag → place conveyors
            if (draggingConveyor && leftJustReleased) {

                draggingConveyor = false;

                ArrayList<Vector2> path = computeConveyorPath(dragStartX, dragStartY, tx, ty);

                for (int i = 0; i < path.size(); i++) {
                    Vector2 p = path.get(i);

                    Conveyor.Direction dir = getDirectionForIndex(path, i);

                    buildingManager.place(new Conveyor((int)p.x, (int)p.y, dir));
                }

                mode = GameMode.NORMAL;
            }

            // Cancel with RMB
            if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                draggingConveyor = false;
                mode = GameMode.NORMAL;
            }
        }

        batch.end();

        // --- HUD ---
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
            TileType t = TileType.fromId(engine.getTile(tx, ty));
            String tooltip = t.name + " (" + tx + ", " + ty + ")";

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
        Assets.dispose();
    }

    private ArrayList<Vector2> computeConveyorPath(int sx, int sy, int ex, int ey) {
        ArrayList<Vector2> path = new ArrayList<>();

        int x = sx;
        int y = sy;

        while (x != ex) {
            path.add(new Vector2(x, y));
            x += (ex > x ? 1 : -1);
        }

        while (y != ey) {
            path.add(new Vector2(x, y));
            y += (ey > y ? 1 : -1);
        }

        path.add(new Vector2(ex, ey));
        return path;
    }

    private Conveyor.Direction directionFor(Vector2 a, Vector2 b) {
        if (b.x > a.x) return Conveyor.Direction.RIGHT;
        if (b.x < a.x) return Conveyor.Direction.LEFT;
        if (b.y > a.y) return Conveyor.Direction.UP;
        return Conveyor.Direction.DOWN;
    }

    private com.badlogic.gdx.graphics.g2d.TextureRegion getConveyorSprite(Conveyor.Direction dir) {
        return switch (dir) {
            case UP -> Assets.conveyorUp;
            case DOWN -> Assets.conveyorDown;
            case LEFT -> Assets.conveyorLeft;
            case RIGHT -> Assets.conveyorRight;
        };
    }

    private boolean oreCheck(int x, int y, int w, int h) {
        TileType oreType = null;
        int oreCount = 0;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                TileType t = TileType.fromId(engine.getTile(x + dx, y + dy));

                if (t == TileType.COAL || t == TileType.IRON || t == TileType.COPPER) {
                    if (oreType == null) oreType = t;
                    if (t != oreType) return false;
                    oreCount++;
                }
            }
        }

        return oreCount >= 1;
    }

    private void generateVein(TileEngine engine, int tileId, int seedCount, int veinLength) {
        for (int i = 0; i < seedCount; i++) {
            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);

            for (int v = 0; v < veinLength; v++) {
                TileType t = TileType.fromId(engine.getTile(x, y));

                if (t == TileType.DIRT || t == TileType.SAND)
                    engine.setTile(x, y, tileId);

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

    private Conveyor.Direction getDirectionForIndex(ArrayList<Vector2> path, int i) {
        // Only one tile in the path → arbitrary default
        if (path.size() == 1) {
            return Conveyor.Direction.RIGHT;
        }

        Vector2 current = path.get(i);

        // First tile → look forward
        if (i == 0) {
            Vector2 next = path.get(i + 1);
            return directionFor(current, next);
        }

        // Last tile → look backward
        if (i == path.size() - 1) {
            Vector2 prev = path.get(i - 1);
            return directionFor(prev, current);
        }

        // Middle tile → look forward
        Vector2 next = path.get(i + 1);
        return directionFor(current, next);
    }
}
