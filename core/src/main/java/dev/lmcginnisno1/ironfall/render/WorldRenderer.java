package dev.lmcginnisno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import dev.lmcginnisno1.ironfall.buildings.Building;
import dev.lmcginnisno1.ironfall.buildings.Conveyor;
import dev.lmcginnisno1.ironfall.power.PowerBlock;
import dev.lmcginnisno1.ironfall.power.PowerSystem;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;
import dev.lmcginnisno1.ironfall.buildings.BuildingManager;

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
        renderPowerLinks(batch);
        renderPowerWarnings(batch);

        if (game.showGrid) {
            drawGrid(batch);
        }
    }

    /** Lasers from each power node to the buildings it links. */
    private void renderPowerLinks(SpriteBatch batch) {
        for (PowerSystem.Link link : buildings.getPower().getLinks()) {
            float x1 = DrawUtil.centerX(link.from()), y1 = DrawUtil.centerY(link.from());
            float x2 = DrawUtil.centerX(link.to()), y2 = DrawUtil.centerY(link.to());

            batch.setColor(0.55f, 0.4f, 0.05f, 0.55f);
            DrawUtil.line(batch, x1, y1, x2, y2, 3f);
            batch.setColor(1f, 0.85f, 0.3f, 0.9f);
            DrawUtil.line(batch, x1, y1, x2, y2, 1f);
        }
        batch.setColor(Color.WHITE);
    }

    /** A small marker on powered buildings that aren't getting enough power: red = none, orange = some. */
    private void renderPowerWarnings(SpriteBatch batch) {
        for (Building b : buildings.getBuildings()) {
            if (!(b instanceof PowerBlock block) || !block.isPowerStarved()) continue;

            float size = 7f;
            float x = (b.x + b.width) * TileEngine.TILE_SIZE - size - 3;
            float y = (b.y + b.height) * TileEngine.TILE_SIZE - size - 3;

            batch.setColor(0.1f, 0.1f, 0.1f, 1f);
            batch.draw(Assets.whitePixel, x - 1, y - 1, size + 2, size + 2);
            if (block.powerReceived() <= 0f) batch.setColor(0.9f, 0.2f, 0.2f, 1f);
            else batch.setColor(1f, 0.6f, 0.1f, 1f);
            batch.draw(Assets.whitePixel, x, y, size, size);
        }
        batch.setColor(Color.WHITE);
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
