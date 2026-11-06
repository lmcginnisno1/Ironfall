package dev.lmcginnisno1.ironfall.power;

import java.util.ArrayList;
import java.util.List;

/** One connected group of power blocks. Totals are recomputed every frame by {@link PowerSystem}. */
public class PowerGrid {

    final List<PowerBlock> members = new ArrayList<>();

    float produced;           // per second
    float demand;             // per second
    float stored;             // energy in this grid's batteries
    float capacity;           // total battery capacity
    float satisfaction = 1f;  // fraction of demand met this frame, batteries included

    public float produced() {
        return produced;
    }

    public float demand() {
        return demand;
    }

    public float stored() {
        return stored;
    }

    public float capacity() {
        return capacity;
    }

    /** Fraction of demand being met this frame (batteries included), 0..1. No demand counts as fully met. */
    public float satisfaction() {
        return satisfaction;
    }

    /** True if anything on this grid could use more power: a consumer, or a battery that isn't full. */
    public boolean wantsPower() {
        return demand > 0f || stored < capacity - 0.01f;
    }

    public int size() {
        return members.size();
    }

    /** One-line summary for info panels, e.g. "Grid: 60 made / 30 used per s (100%)". */
    public static String describe(PowerGrid grid) {
        if (grid == null) return "Grid: not connected";
        String line = "Grid: " + Math.round(grid.produced) + " made / " + Math.round(grid.demand)
            + " used per s (" + Math.round(grid.satisfaction() * 100f) + "%)";
        if (grid.capacity > 0f) {
            line += ", stored " + Math.round(grid.stored) + "/" + Math.round(grid.capacity);
        }
        return line;
    }
}
