package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.power.PowerBlock;
import dev.lmcginnisno1.ironfall.power.PowerGrid;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.util.List;

/** 1x1 pylon that links every power building within range into one grid. Links are automatic. */
public class PowerNode extends Building implements PowerBlock {

    public static final int COST = Balance.POWER_NODE_COST;

    private PowerGrid grid;

    public PowerNode(int x, int y) {
        super(x, y, 1, 1, Assets.powerNode, COST);
    }

    /** True if any part of {@code other} is within range of this node's centre. */
    public boolean inRange(Building other) {
        return inRange(x, y, other.x, other.y, other.width, other.height);
    }

    /**
     * Range test for a node at tile (nx, ny) against a w×h footprint at (ox, oy).
     * Static so the placement preview can use it before the node exists.
     */
    public static boolean inRange(int nx, int ny, int ox, int oy, int w, int h) {
        float cx = nx + 0.5f, cy = ny + 0.5f;
        float dx = Math.max(Math.max(ox - cx, 0f), cx - (ox + w));
        float dy = Math.max(Math.max(oy - cy, 0f), cy - (oy + h));
        return dx * dx + dy * dy <= Balance.POWER_NODE_RANGE * Balance.POWER_NODE_RANGE;
    }

    @Override
    public void setPowerGrid(PowerGrid grid) {
        this.grid = grid;
    }

    @Override
    public List<String> infoLines() {
        return List.of(
            "POWER NODE",
            "Links power buildings within " + (int) Balance.POWER_NODE_RANGE + " tiles",
            PowerGrid.describe(grid)
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new PowerNode(x, y);
    }

    @Override
    public BuildingType type() {
        return BuildingType.POWER_NODE;
    }
}
