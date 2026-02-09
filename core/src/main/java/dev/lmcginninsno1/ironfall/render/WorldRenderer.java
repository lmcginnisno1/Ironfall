package dev.lmcginninsno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import dev.lmcginninsno1.ironfall.buildings.Conveyor;
import dev.lmcginninsno1.ironfall.tiles.Assets;
import dev.lmcginninsno1.ironfall.IronfallGame;
import dev.lmcginninsno1.ironfall.tiles.TileEngine;
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
        renderConveyorItems(batch);

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

    private void renderConveyorItems(SpriteBatch batch) {
        int w = tiles.getWidth();
        int h = tiles.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                var b = buildings.getAt(x, y);
                if (!(b instanceof Conveyor c)) continue;

                var list = c.getItems();
                if (list == null || list.size == 0) continue;

                float baseX = x * TileEngine.TILE_SIZE;
                float baseY = y * TileEngine.TILE_SIZE;

                for (var m : list) {
                    var type = m.item.type();
                    var sprite = tiles.getRegion(type.row, type.col);

                    float p = m.progress;
                    float ox = 0f, oy = 0f;

                    switch (c.direction) {
                        case UP -> oy = TileEngine.TILE_SIZE * p;
                        case DOWN -> oy = -TileEngine.TILE_SIZE * p;
                        case LEFT -> ox = -TileEngine.TILE_SIZE * p;
                        case RIGHT -> ox = TileEngine.TILE_SIZE * p;
                    }

                    batch.draw(sprite, baseX + ox, baseY + oy);
                }
            }
        }
    }
}
