package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;

public class BasicMiner extends Building {

    private float timer = 0f;

    private final int oreId;        // 11 = coal, 12 = iron, 13 = copper

    // 0 or 1 item waiting to be output
    private Item buffer = null;

    // 0.25 ore/sec per ore tile
    private final float rate;

    private final TileEngine engine;

    public static final int COST = 50;

    public BasicMiner(int x, int y, TileEngine engine) {
        super(x, y, 2, 2, Assets.basicMiner, COST);
        this.engine = engine;

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

        // Produce based on rate, scaled by the current miner speed upgrade level
        float effectiveRate = rate * world.getUpgrades().minerSpeedMultiplier();
        timer += delta;
        if (timer >= 1f / effectiveRate) {
            timer -= 1f / effectiveRate;
            buffer = new Item(ItemType.oreTypeFromId(oreId));
        }
    }

    private void tryOutput() {
        // For a 2×2 building, each side has two adjacent tiles
        int[][] offsets = {
            // left side
            { -1, 0 }, { -1, 1 },
            // right side
            {  2, 0 }, {  2, 1 },
            // bottom side
            { 0, -1 }, { 1, -1 },
            // top side
            { 0,  2 }, { 1,  2 }
        };

        for (int[] o : offsets) {
            int tx = x + o[0];
            int ty = y + o[1];

            Building b = world.getAt(tx, ty);

            if (b instanceof Conveyor c && c.canAcceptAnotherItem()) {
                c.addIncomingItem(buffer);
                buffer = null;
                return;
            }

            // Also allow feeding straight into an adjacent Core, no belt
            // required — mirrors how WorldGenerator already favors placing
            // the Core near ore, so touching placements should just work.
            // If the Core is at its storage cap for this resource, acceptItem
            // returns false and the miner simply stays full (backpressure),
            // same as when an adjacent conveyor is jammed.
            if (b instanceof Core core && core.acceptItem(buffer)) {
                buffer = null;
                return;
            }
        }
    }

    @Override
    public Building copyAt(int x, int y) {
        return new BasicMiner(x, y, engine);
    }
}
