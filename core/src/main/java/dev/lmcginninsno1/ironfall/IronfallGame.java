package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class IronfallGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private TileEngine engine;

    @Override
    public void create() {
        batch = new SpriteBatch();
        engine = new TileEngine(480, 270);

        int width = 480;
        int height = 270;

        // Step 1: Fill with mostly dirt, some sand
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                double noise = Math.random();

                if (noise < 0.85) engine.setTile(x, y, 0); // dirt
                else engine.setTile(x, y, 1); // sand
            }
        }


        generateVeins(engine, width, height, 11, 50, 60); // coal
        generateVeins(engine, width, height, 12, 30, 60); // iron
        generateVeins(engine, width, height, 13, 50, 60); // copper
        generateVeins(engine, width, height, 10, 100, 100); // stone
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        engine.update(delta);
        engine.render(batch);
    }

    @Override
    public void dispose() {
        batch.dispose();
        engine.dispose();
    }

    private void generateVeins(TileEngine engine, int width, int height, int tileId,
                               int seedCount, int veinLength) {

        for (int i = 0; i < seedCount; i++) {

            int x = (int)(Math.random() * width);
            int y = (int)(Math.random() * height);

            // Grow the vein
            for (int v = 0; v < veinLength; v++) {
                // Only place ore on empty / terrain tiles
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
}
