package dev.lmcginninsno1.ironfall.worldgen;

import com.badlogic.gdx.math.Vector2;
import dev.lmcginninsno1.ironfall.buildings.BuildingManager;
import dev.lmcginninsno1.ironfall.tiles.TileEngine;
import dev.lmcginninsno1.ironfall.tiles.TileType;

public class WorldGenerator {

    public static void generate(TileEngine engine, BuildingManager buildings) {
        int width = engine.getWidth();
        int height = engine.getHeight();

        generateTerrain(engine, width, height);
        generateVeins(engine, width, height);

        Vector2 corePos = findCoreLocation(engine);
        buildings.placeCore((int) corePos.x, (int) corePos.y);
    }

    private static void generateTerrain(TileEngine engine, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double noise = Math.random();
                engine.setTile(x, y, noise < 0.85 ? TileType.DIRT.id : TileType.SAND.id);
            }
        }
    }

    private static void generateVeins(TileEngine engine, int width, int height) {
        generateVein(engine, TileType.COAL.id, width, height, 50, 60);
        generateVein(engine, TileType.IRON.id, width, height, 30, 60);
        generateVein(engine, TileType.COPPER.id, width, height, 50, 60);
        generateVein(engine, TileType.STONE.id, width, height, 100, 100);
    }

    private static void generateVein(TileEngine engine, int tileId, int width, int height, int seedCount, int veinLength) {
        for (int i = 0; i < seedCount; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);

            for (int v = 0; v < veinLength; v++) {
                TileType t = TileType.fromId(engine.getTile(x, y));
                if (t == TileType.DIRT || t == TileType.SAND)
                    engine.setTile(x, y, tileId);

                int dir = (int) (Math.random() * 4);
                switch (dir) {
                    case 0 -> x++;
                    case 1 -> x--;
                    case 2 -> y++;
                    case 3 -> y--;
                }

                x = Math.max(0, Math.min(x, width - 1));
                y = Math.max(0, Math.min(y, height - 1));
            }
        }
    }

    private static Vector2 findCoreLocation(TileEngine engine) {
        int width = engine.getWidth();
        int height = engine.getHeight();

        int bestX = width / 2;
        int bestY = height / 2;
        float bestScore = Float.NEGATIVE_INFINITY;

        for (int x = width / 4; x < width * 3 / 4; x++) {
            for (int y = height / 4; y < height * 3 / 4; y++) {

                float score = 0;
                score += oreScore(engine, x, y, TileType.COPPER.id, 20);
                score += oreScore(engine, x, y, TileType.IRON.id, 30);
                score += oreScore(engine, x, y, TileType.COAL.id, 30);

                if (score > bestScore) {
                    bestScore = score;
                    bestX = x;
                    bestY = y;
                }
            }
        }

        return new Vector2(bestX, bestY);
    }

    private static float oreScore(TileEngine engine, int cx, int cy, int oreId, int radius) {
        float score = 0;
        for (int y = cy - radius; y <= cy + radius; y++) {
            for (int x = cx - radius; x <= cx + radius; x++) {
                if (!engine.inBounds(x, y)) continue;
                if (engine.getTile(x, y) == oreId) score += 1;
            }
        }
        return score;
    }
}
