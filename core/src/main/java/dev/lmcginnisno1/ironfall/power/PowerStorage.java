package dev.lmcginnisno1.ironfall.power;

/**
 * A power block that stores energy (batteries). {@link PowerSystem} charges
 * storage with a grid's surplus and drains it to cover a shortfall, so
 * storage blocks don't produce or demand power themselves.
 */
public interface PowerStorage extends PowerBlock {

    /** Maximum energy this block can hold, in power units. */
    float capacity();

    /** Energy currently stored, in power units. */
    float stored();

    void setStored(float amount);
}
