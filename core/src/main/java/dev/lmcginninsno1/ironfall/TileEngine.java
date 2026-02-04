package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TileEngine {

    public static final int TILE_SIZE = 16;       // world tile size
    public static final int PADDED_TILE_SIZE = 18; // 16 + 2 padding
    public static final int CORE_TILE_SIZE = 16;   // actual tile inside padding

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

        // Load padded tilesheet
        tilesheet = new Texture("tiles/tilesheet.png");
        tilesheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Slice padded tilesheet
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

                // 1. World position under mouse BEFORE zoom
                Vector3 before = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                // 2. Apply zoom
                camera.zoom += amountY * 0.05f;
                camera.zoom = Math.max(0.1f, Math.min(camera.zoom, 1f));
                camera.update();

                // 3. World position under mouse AFTER zoom
                Vector3 after = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

                // 4. Shift camera so the mouse stays over the same world point
                camera.position.add(before.x - after.x, before.y - after.y, 0);

                clampCamera();
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
        int rows = sheet.getHeight() / PADDED_TILE_SIZE;
        int cols = sheet.getWidth() / PADDED_TILE_SIZE;

        TextureRegion[][] out = new TextureRegion[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                // Position of padded tile
                int px = x * PADDED_TILE_SIZE;
                int py = y * PADDED_TILE_SIZE;

                // Extract ONLY the inner 16×16 tile (skip 1px padding)
                out[y][x] = new TextureRegion(sheet, px + 1, py + 1, CORE_TILE_SIZE, CORE_TILE_SIZE);
            }
        }

        return out;
    }

    public void update(float delta) {
        handleCameraInput(delta);

        // Snap camera to whole pixels to avoid subpixel sampling
        camera.position.x = Math.round(camera.position.x);
        camera.position.y = Math.round(camera.position.y);

        camera.update();
    }

    public void render(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int id = tiles[y][x];

                TileType type = getTileTypeById(id);

                TextureRegion tex = regions[type.row][type.col];
                batch.draw(tex, x * TILE_SIZE, y * TILE_SIZE);
            }
        }

        batch.end();
    }

    public TileType getTileTypeById(int id) {
        return switch (id) {
            case 0 -> TileType.DIRT;
            case 1 -> TileType.SAND;
            case 10 -> TileType.STONE;
            case 11 -> TileType.COAL;
            case 12 -> TileType.IRON;
            case 13 -> TileType.COPPER;
            default -> TileType.EMPTY;
        };
    }

    private void handleCameraInput(float delta) {
        float speed = 1200 * delta * Math.max(camera.zoom, 0.05f);

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
            return -1;
        }
        return tiles[y][x];
    }

    public Vector2 screenToWorld(int screenX, int screenY) {
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        return new Vector2((int)(world.x / TILE_SIZE), (int)(world.y / TILE_SIZE));
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
