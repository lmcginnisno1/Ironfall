package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;
import dev.lmcginnisno1.ironfall.power.PowerBlock;
import dev.lmcginnisno1.ironfall.power.PowerGrid;
import dev.lmcginnisno1.ironfall.save.SaveIO;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Tier-2 3x3 miner. Mines faster per ore tile than the Basic Miner but needs
 * power: it runs at the grid's satisfaction (half power = half speed) and
 * stops entirely without it. It only draws power while it has room to mine.
 */
public class ElectricMiner extends Building implements PowerBlock {

    public static final int COST = Balance.ELECTRIC_MINER_COST;
    public static final int SIZE = 3;

    private final TileEngine engine;
    private final ItemType ore;     // null if placed with no ore underneath (shouldn't happen)
    private final int oreTiles;

    private float timer = 0f;
    private Item buffer = null;     // one finished item waiting for room
    private PowerGrid grid;

    public ElectricMiner(int x, int y, TileEngine engine) {
        super(x, y, SIZE, SIZE, Assets.electricMiner, COST);
        this.engine = engine;

        int foundOreId = -1;
        int count = 0;
        for (int dy = 0; dy < SIZE; dy++) {
            for (int dx = 0; dx < SIZE; dx++) {
                int tile = engine.getTile(x + dx, y + dy);
                if (tile >= 11 && tile <= 13) {
                    if (foundOreId == -1) foundOreId = tile;
                    count++;
                }
            }
        }
        this.ore = ItemType.oreTypeFromId(foundOreId);
        this.oreTiles = count;
    }

    /** Items per second at full power, including the miner speed upgrade. */
    private float fullRate() {
        float upgrade = world == null ? 1f : world.getUpgrades().minerSpeedMultiplier();
        return Balance.ELECTRIC_MINER_RATE_PER_ORE_TILE * oreTiles * upgrade;
    }

    /** Fraction of the power this miner wants that it's getting, 0..1. */
    public float satisfaction() {
        return grid == null ? 0f : grid.satisfaction();
    }

    /** True while the miner wants to mine but isn't getting full power. */
    public boolean isPowerStarved() {
        return powerDemand() > 0f && satisfaction() < 1f;
    }

    @Override
    public void update(float delta) {
        if (ore == null) return;

        if (buffer != null) {
            if (pushItemToAnyNeighbour(buffer)) buffer = null;
            else return; // backed up: wait without mining
        }

        float rate = fullRate() * satisfaction();
        if (rate <= 0f) return;

        timer += delta;
        if (timer >= 1f / rate) {
            timer -= 1f / rate;
            buffer = new Item(ore);
        }
    }

    @Override
    public float powerDemand() {
        return (ore != null && buffer == null) ? Balance.ELECTRIC_MINER_POWER_USE : 0f;
    }

    @Override
    public void setPowerGrid(PowerGrid grid) {
        this.grid = grid;
    }

    @Override
    public boolean requiresOre() {
        return true;
    }

    @Override
    public List<String> infoLines() {
        String oreName = ore == null ? "nothing" : ore.name.substring(0, 1).toUpperCase() + ore.name.substring(1);
        int pct = Math.round(satisfaction() * 100f);
        String power = pct == 0 ? "No power: link a generator with a power node" : "Power: " + pct + "%";

        return List.of(
            "ELECTRIC MINER",
            "Mining " + oreName + " (" + oreTiles + " ore tiles)",
            String.format("Rate: %.2f/s at full power", fullRate()),
            power,
            PowerGrid.describe(grid)
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new ElectricMiner(x, y, engine);
    }

    @Override
    public BuildingType type() {
        return BuildingType.ELECTRIC_MINER;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeFloat(timer);
        SaveIO.writeItemType(out, buffer == null ? null : buffer.type());
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        timer = in.readFloat();
        ItemType type = SaveIO.readItemType(in);
        buffer = type == null ? null : new Item(type);
    }
}
