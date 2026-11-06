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
}
