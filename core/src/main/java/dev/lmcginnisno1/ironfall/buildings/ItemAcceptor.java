package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.items.Item;

/**
 * A building that can take items from a neighbour. Producers hand items over
 * through {@link Building#pushItem}, so new building types only need to
 * implement this, not be added to every producer.
 */
public interface ItemAcceptor {

    /**
     * Offers an item arriving from tile (fromX, fromY), which is the sender's
     * tile directly next to this building. Returns true if the item was taken;
     * false means the sender keeps it and tries again later.
     */
    boolean acceptItem(Item item, int fromX, int fromY);
}
