package dev.lmcginnisno1.ironfall.buildings;

/**
 * A building that can take water from a neighbour (conduits, tanks, and
 * buildings cooled by water). Senders call {@link Building#pushWater}.
 * Water is the only fluid so far, so amounts are plain floats.
 */
public interface WaterAcceptor {

    /**
     * Offers {@code amount} of water arriving from tile (fromX, fromY), the
     * sender's tile touching this building. Returns how much was taken (0..amount).
     */
    float acceptWater(float amount, int fromX, int fromY);
}
