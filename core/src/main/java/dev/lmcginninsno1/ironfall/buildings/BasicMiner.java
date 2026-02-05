package dev.lmcginninsno1.ironfall.buildings;

import dev.lmcginninsno1.ironfall.Assets;
import dev.lmcginninsno1.ironfall.TileEngine;

public class BasicMiner extends Building {

    private float timer = 0f;

    private final int oreId;        // 11 = coal, 12 = iron, 13 = copper
    private final int oreTiles;     // number of ore tiles under it (1–4)

    public BasicMiner(int x, int y, TileEngine engine) {
        super(x, y, 2, 2, Assets.basicMiner);

        // Scan the 2×2 footprint for ore tiles
        int foundOreId = -1;
        int count = 0;

        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int tile = engine.getTile(x + dx, y + dy);

                // Ore tiles are 11, 12, 13
                if (tile >= 11 && tile <= 13) {
                    if (foundOreId == -1) foundOreId = tile;
                    count++;
                }
            }
        }

        this.oreId = foundOreId;
        this.oreTiles = Math.min(count, 4); // cap at 4
    }

    @Override
    public void update(float delta) {
        timer += delta;

        if (timer >= 1f) {
            timer = 0f;

            if (oreId != -1 && oreTiles > 0) {
                System.out.println(
                    "Miner at (" + x + "," + y + ") produced " +
                        oreTiles + " of ore type " + oreId
                );
            }
        }
    }


}
