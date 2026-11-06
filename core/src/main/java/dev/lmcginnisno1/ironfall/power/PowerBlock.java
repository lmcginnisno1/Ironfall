package dev.lmcginnisno1.ironfall.power;

/**
 * A building that's part of a power grid: generators, consumers, and nodes.
 * Power is measured in units per second. Each frame, {@link PowerSystem}
 * totals production and demand for every grid, then hands each member its
 * grid so it can read the satisfaction (0..1) before its own update.
 */
public interface PowerBlock {

    /** Power produced per second right now. */
    default float powerProduction() {
        return 0f;
    }

    /** Power wanted per second right now. */
    default float powerDemand() {
        return 0f;
    }

    /** Called every frame, before buildings update, with the grid this block belongs to. */
    void setPowerGrid(PowerGrid grid);

    /** Fraction of the power this block wants that it's getting (0..1). Consumers override this. */
    default float powerReceived() {
        return 1f;
    }

    /** True while this block wants power but isn't getting all of it; shows a warning marker on the map. */
    default boolean isPowerStarved() {
        return powerDemand() > 0f && powerReceived() < 1f;
    }
}
