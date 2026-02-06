package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class TileEngine {

    public static final int TILE_SIZE = 16;        // world tile size
    public static final int PADDED_TILE_SIZE = 18; // 16 + 2 padding
    public static final int CORE_TILE_SIZE = 16;   // actual tile inside padding

    private final int width;
    private final int height;
    private final int[][] tiles;

    private final Texture tilesheet;
    private final TextureRegion[][] regions;

    private final OrthographicCamera camera;

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
        camera.setToOrtho(false, 960, 544);

        // Center camera on world
        camera.position.set(width * TILE_SIZE / 2f, height * TILE_SIZE / 2f, 0);
        camera.update();
    }

    private TextureRegion[][] splitTilesheet(Texture sheet) {
        int rows = sheet.getHeight() / PADDED_TILE_SIZE;
        int cols = sheet.getWidth() / PADDED_TILE_SIZE;

        TextureRegion[][] out = new TextureRegion[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int px = x * PADDED_TILE_SIZE;
                int py = y * PADDED_TILE_SIZE;
                out[y][x] = new TextureRegion(sheet, px + 1, py + 1, CORE_TILE_SIZE, CORE_TILE_SIZE);
            }
        }

        return out;
    }

    public void update() {
        // Camera is controlled externally (CameraController)
        camera.update();
    }

    public void render(SpriteBatch batch) {
        // Projection matrix is set by WorldRenderer
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int id = tiles[y][x];
                TileType type = TileType.fromId(id);
                TextureRegion tex = regions[type.row][type.col];
                batch.draw(tex, x * TILE_SIZE, y * TILE_SIZE);
            }
        }
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
