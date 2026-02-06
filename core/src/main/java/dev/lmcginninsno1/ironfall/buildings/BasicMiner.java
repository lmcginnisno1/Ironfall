package dev.lmcginninsno1.ironfall.buildings;

import dev.lmcginninsno1.ironfall.items.Item;
import dev.lmcginninsno1.ironfall.items.ItemType;
import dev.lmcginninsno1.ironfall.tiles.Assets;
import dev.lmcginninsno1.ironfall.tiles.TileEngine;

public class BasicMiner extends Building {

    private float timer = 0f;

    private final int oreId;        // 11 = coal, 12 = iron, 13 = copper

    // 0 or 1 item waiting to be output
    private Item buffer = null;

    // 0.25 ore/sec per ore tile
    private final float rate;

    public BasicMiner(int x, int y, TileEngine engine) {
        super(x, y, 2, 2, Assets.basicMiner);

        int foundOreId = -1;
        int count = 0;

        for (int dy = 0; dy < 2; dy++) {
            for (int dx = 0; dx < 2; dx++) {
                int tile = engine.getTile(x + dx, y + dy);

                if (tile >= 11 && tile <= 13) {
                    if (foundOreId == -1) foundOreId = tile;
                    count++;
                }
            }
        }

        this.oreId = foundOreId;
        // number of ore tiles under it (1–4)
        int oreTiles = Math.min(count, 4);

        // production rate based on ore coverage
        this.rate = 0.25f * oreTiles;
    }

    @Override
    public void update(float delta) {
        // If buffer is full, try to output before producing more
        if (buffer != null) {
            tryOutput();
            if (buffer != null) return;
        }

        // Produce based on rate
        timer += delta;
        if (timer >= 1f / rate) {
            timer -= 1f / rate;
            buffer = new Item(ItemType.oreTypeFromId(oreId));
        }
    }

    private void tryOutput() {
        // Four tiles directly touching the miner's 2×2 footprint
        int[][] offsets = {
            { -1,  0 }, // left side, middle
            {  2,  0 }, // right side, middle
            {  0, -1 }, // bottom, middle
            {  0,  2 }  // top, middle
        };

        for (int[] o : offsets) {
            int tx = x + o[0];
            int ty = y + o[1];

            Building b = world.getAt(tx, ty);
            if (b instanceof Conveyor c && c.canAcceptItem()) {
                c.acceptItem(buffer);
                buffer = null;
                return;
            }
        }
    }
}
