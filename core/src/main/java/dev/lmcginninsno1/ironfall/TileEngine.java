package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class TileEngine {

    public static final int TILE_SIZE = 16;
    public static final int SHEET_TILE_SIZE = 16;
    public static final int SHEET_MARGIN = 1;

    private final int width;
    private final int height;
    private final int[][] tiles;

    private final Texture tilesheet;
    private final TextureRegion[][] regions;

    private final OrthographicCamera camera;

    // Mouse drag state
    private float lastMouseX;
    private float lastMouseY;
    private boolean dragging = false;

    public TileEngine(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new int[height][width];

        // Load tilesheet
        tilesheet = new Texture("tiles/tilesheet.png");

        // Slice it
        regions = splitTilesheet(tilesheet);

        // Camera setup
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);

        // Center camera on world
        camera.position.set(width * TILE_SIZE / 2f, height * TILE_SIZE / 2f, 0);

        // Input handling (zoom + drag)
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (Math.abs(amountY) < 0.01f) return false;

                camera.zoom += amountY * 0.05f;
                if (camera.zoom < 0.1f) camera.zoom = 0.1f;
                if (camera.zoom > 1f) camera.zoom = 1f;

                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                dragging = true;
                lastMouseX = screenX;
                lastMouseY = screenY;
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                dragging = false;
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (!dragging) return false;

                float dx = screenX - lastMouseX;
                float dy = screenY - lastMouseY;

                // Move camera opposite drag direction
                camera.position.x -= dx * camera.zoom;
                camera.position.y += dy * camera.zoom;

                lastMouseX = screenX;
                lastMouseY = screenY;

                clampCamera();
                return true;
            }
        });
    }

    private TextureRegion[][] splitTilesheet(Texture sheet) {
        int rows = (sheet.getHeight() + TileEngine.SHEET_MARGIN) / (TileEngine.SHEET_TILE_SIZE + TileEngine.SHEET_MARGIN);
        int cols = (sheet.getWidth() + TileEngine.SHEET_MARGIN) / (TileEngine.SHEET_TILE_SIZE + TileEngine.SHEET_MARGIN);

        TextureRegion[][] out = new TextureRegion[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int px = x * (TileEngine.SHEET_TILE_SIZE + TileEngine.SHEET_MARGIN);
                int py = y * (TileEngine.SHEET_TILE_SIZE + TileEngine.SHEET_MARGIN);

                out[y][x] = new TextureRegion(sheet, px, py, TileEngine.SHEET_TILE_SIZE, TileEngine.SHEET_TILE_SIZE);
            }
        }

        return out;
    }

    public void update(float delta) {
        handleCameraInput(delta);
        camera.update();
    }

    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int id = tiles[y][x];

                TileType type = switch (id) {
                    case 0 -> TileType.DIRT;
                    case 1 -> TileType.SAND;
                    case 10 -> TileType.STONE;
                    case 11 -> TileType.COAL;
                    case 12 -> TileType.IRON;
                    case 13 -> TileType.COPPER;
                    default -> TileType.EMPTY;
                };

                TextureRegion tex = regions[type.row][type.col];
                batch.draw(tex, x * TILE_SIZE, y * TILE_SIZE);
            }
        }

        batch.end();
    }

    private void handleCameraInput(float delta) {
        float speed = 300 * delta * camera.zoom;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;

        clampCamera();
    }

    private void clampCamera() {
        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

        float worldW = width * TILE_SIZE;
        float worldH = height * TILE_SIZE;

        // Clamp so the camera never shows outside the world
        camera.position.x = Math.max(halfW, Math.min(camera.position.x, worldW - halfW));
        camera.position.y = Math.max(halfH, Math.min(camera.position.y, worldH - halfH));
    }

    public void dispose() {
        tilesheet.dispose();
    }

    public void setTile(int x, int y, int id) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[y][x] = id;
        }
    }

    public int getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return -1; // out of bounds
        }
        return tiles[y][x];
    }
}
