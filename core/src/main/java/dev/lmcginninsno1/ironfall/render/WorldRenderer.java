package dev.lmcginninsno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import dev.lmcginninsno1.ironfall.Assets;
import dev.lmcginninsno1.ironfall.IronfallGame;
import dev.lmcginninsno1.ironfall.TileEngine;
import dev.lmcginninsno1.ironfall.buildings.BuildingManager;

public class WorldRenderer {

    private final IronfallGame game;
    private final TileEngine tiles;
    private final BuildingManager buildings;

    public WorldRenderer(IronfallGame game, TileEngine tiles, BuildingManager buildings) {
        this.game = game;
        this.tiles = tiles;
        this.buildings = buildings;
    }

    public void render(SpriteBatch batch) {
        tiles.render(batch);

        buildings.render(batch);

        if (game.showGrid) {
            drawGrid(batch);
        }
    }

    private void drawGrid(SpriteBatch batch) {
        batch.setColor(1f, 1f, 1f, 0.1f);

        int w = tiles.getWidth();
        int h = tiles.getHeight();

        for (int x = 0; x <= w; x++) {
            batch.draw(
                Assets.whitePixel,
                x * 16,
                0,
                1,
                h * 16
            );
        }

        for (int y = 0; y <= h; y++) {
            batch.draw(
                Assets.whitePixel,
                0,
                y * 16,
                w * 16,
                1
            );
        }

        batch.setColor(Color.WHITE);
    }
}
