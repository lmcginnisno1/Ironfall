package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.power.PowerBlock;
import dev.lmcginnisno1.ironfall.power.PowerGrid;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;
import dev.lmcginnisno1.ironfall.tiles.TileType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 2x2 pump placed over water. Pumps faster the more water tiles it covers,
 * runs at its grid's power satisfaction, and pushes water into any
 * neighbouring conduit, tank, or water-cooled building. It stops drawing
 * power once its small buffer is full.
 */
public class WaterPump extends Building implements PowerBlock {

    public static final int COST = Balance.WATER_PUMP_COST;
    public static final int SIZE = 2;

    private final TileEngine engine;
    private final int waterTiles;

    private float water = 0f;
    private PowerGrid grid;

    public WaterPump(int x, int y, TileEngine engine) {
        super(x, y, SIZE, SIZE, Assets.waterPump, COST);
        this.engine = engine;

        int count = 0;
        for (int dy = 0; dy < SIZE; dy++) {
            for (int dx = 0; dx < SIZE; dx++) {
                if (engine.getTile(x + dx, y + dy) == TileType.WATER.id) count++;
            }
        }
        this.waterTiles = count;
    }

    private float fullRate() {
        return Balance.WATER_PUMP_RATE_PER_WATER_TILE * waterTiles;
    }

    private float satisfaction() {
        return grid == null ? 0f : grid.satisfaction();
    }

    private boolean full() {
        return water >= Balance.WATER_PUMP_BUFFER - 0.01f;
    }

    @Override
    public void update(float delta) {
        water = Math.min(Balance.WATER_PUMP_BUFFER, water + fullRate() * satisfaction() * delta);
        water -= pushWaterToNeighbours(water);
    }

    @Override
    public float powerDemand() {
        return (waterTiles > 0 && !full()) ? Balance.WATER_PUMP_POWER_USE : 0f;
    }

    @Override
    public void setPowerGrid(PowerGrid grid) {
        this.grid = grid;
    }

    @Override
    public float powerReceived() {
        return satisfaction();
    }

    @Override
    public boolean requiresWater() {
        return true;
    }

    @Override
    public List<String> infoLines() {
        int pct = Math.round(satisfaction() * 100f);
        String power = pct == 0 && !full() ? "No power: link a generator with a power node" : "Power: " + pct + "%";
        return List.of(
            "WATER PUMP",
            "On " + waterTiles + " water tiles: " + Math.round(fullRate()) + " water/s at full power",
            full() ? "Full: nothing is taking water" : "Holding " + Math.round(water) + "/" + Math.round(Balance.WATER_PUMP_BUFFER),
            power,
            PowerGrid.describe(grid)
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new WaterPump(x, y, engine);
    }

    @Override
    public BuildingType type() {
        return BuildingType.WATER_PUMP;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeFloat(water);
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        water = Math.max(0f, Math.min(in.readFloat(), Balance.WATER_PUMP_BUFFER));
    }
}
