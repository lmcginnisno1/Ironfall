package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class IronfallGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private TileEngine engine;
    private BitmapFont font;

    private OrthographicCamera hudCamera;

    private final int width = 480;
    private final int height = 270;

    @Override
    public void create() {
        batch = new SpriteBatch();
        engine = new TileEngine(width, height);

        font = new BitmapFont();
        font.getData().setScale(1f);   // Make text readable

        // HUD camera in screen space (pixels)
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        // Fill with mostly dirt, some sand
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double noise = Math.random();

                if (noise < 0.85) engine.setTile(x, y, 0); // dirt
                else engine.setTile(x, y, 1); // sand
            }
        }

        generateVeins(engine, 11, 50, 60); // coal
        generateVeins(engine, 12, 30, 60); // iron
        generateVeins(engine, 13, 50, 60); // copper
        generateVeins(engine, 10, 100, 100); // stone
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Gdx.graphics.getDeltaTime();
        engine.update(delta);
        engine.render(batch);

        // Update HUD camera in case window resized
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        // Convert mouse to tile coordinates
        Vector2 tilePos = engine.screenToWorld(Gdx.input.getX(), Gdx.input.getY());
        int tx = (int) tilePos.x;
        int ty = (int) tilePos.y;

        if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
            int id = engine.getTile(tx, ty);
            String name = tileName(id);

            // Draw tooltip near cursor in SCREEN SPACE using HUD camera
            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            font.draw(
                batch,
                name,
                Gdx.input.getX() + 16,
                Gdx.graphics.getHeight() - Gdx.input.getY() + 16
            );
            batch.end();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        engine.dispose();
        font.dispose();
    }

    private void generateVeins(TileEngine engine, int tileId, int seedCount, int veinLength) {
        for (int i = 0; i < seedCount; i++) {

            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);

            // Grow the vein
            for (int v = 0; v < veinLength; v++) {
                if (engine.getTile(x, y) <= 9) {
                    engine.setTile(x, y, tileId);
                }

                // Random walk
                int dir = (int)(Math.random() * 4);
                switch (dir) {
                    case 0 -> x++;
                    case 1 -> x--;
                    case 2 -> y++;
                    case 3 -> y--;
                }

                // Clamp to world
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
