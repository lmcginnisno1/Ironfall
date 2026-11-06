package dev.lmcginnisno1.ironfall.power;

import java.util.ArrayList;
import java.util.List;

/** One connected group of power blocks. Totals are recomputed every frame by {@link PowerSystem}. */
public class PowerGrid {

    final List<PowerBlock> members = new ArrayList<>();

    float produced; // per second
    float demand;   // per second

    public float produced() {
        return produced;
    }

    public float demand() {
        return demand;
    }

    /** Fraction of demand being met, 0..1. A grid with no demand counts as fully satisfied. */
    public float satisfaction() {
        if (demand <= 0f) return 1f;
        return Math.min(1f, produced / demand);
    }

    public int size() {
        return members.size();
    }

    /** One-line summary for info panels, e.g. "Grid: 60 made / 30 used per s (100%)". */
    public static String describe(PowerGrid grid) {
        if (grid == null) return "Grid: not connected";
        return "Grid: " + Math.round(grid.produced) + " made / " + Math.round(grid.demand)
            + " used per s (" + Math.round(grid.satisfaction() * 100f) + "%)";
    }
}
