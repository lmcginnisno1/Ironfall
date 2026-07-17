package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;

import java.util.HashMap;
import java.util.Map;

public class Core extends Building {

    private final Map<ItemType, Integer> inventory = new HashMap<>();

    // Instead of storing percentages, we store the exact locked amount of items to sell
    private final Map<ItemType, Integer> saleQuantities = new HashMap<>();

    public Core(int x, int y) {
        super(x, y, 4, 4, Assets.core, 0);
    }

    /** Returns false (accepting nothing) if this resource is already at its storage cap. */
    public boolean acceptItem(Item item) {
        ItemType type = item.type();
        int cap = world.getUpgrades().storageCap();
        int have = inventory.getOrDefault(type, 0);

        if (have >= cap) return false;

        inventory.put(type, have + 1);
        return true;
    }

    @Override
    public void update(float delta) {
        // Core has no active behavior yet
    }

    public Map<ItemType, Integer> getInventory() {
        return inventory;
    }

    /** Spends stored resources (e.g. to fund an upgrade). Returns false, spending nothing, if insufficient. */
    public boolean trySpendResource(ItemType type, int amount) {
        int have = inventory.getOrDefault(type, 0);
        if (have < amount) return false;
        inventory.put(type, have - amount);
        return true;
    }

    // --- ECONOMIC SYSTEM WITH LOCKED QUANTITIES ---

    public int getSaleQuantity(ItemType type) {
        return saleQuantities.getOrDefault(type, 0);
    }

    /**
     * Sets the sale amount based on a dragged percentage of the current stock.
     * This locks in an absolute number of items.
     */
    public void setSalePercentage(ItemType type, float pct) {
        int totalAmount = inventory.getOrDefault(type, 0);
        float clampedPct = Math.max(0f, Math.min(1f, pct));

        // Translate the visual drag percentage to a hard locked integer amount
        int targetQty = Math.round(totalAmount * clampedPct);
        saleQuantities.put(type, targetQty);
    }

    /**
     * Executes the sale using our locked target quantities.
     */
    public int sellSelectedItems() {
        int totalEarnings = 0;

        for (Map.Entry<ItemType, Integer> entry : inventory.entrySet()) {
            ItemType type = entry.getKey();
            int currentAmount = entry.getValue();
            int qtyToSell = getSaleQuantity(type);

            // Guard: ensure we don't sell more than actually available (safety check)
            qtyToSell = Math.min(qtyToSell, currentAmount);

            if (qtyToSell > 0) {
                int unitPrice = getPriceForType(type);
                totalEarnings += (qtyToSell * unitPrice);

                // Deduct the locked quantity
                inventory.put(type, currentAmount - qtyToSell);
            }

            // Reset this item's sale target back to 0
            saleQuantities.put(type, 0);
        }

        return totalEarnings;
    }

    public int getPriceForType(ItemType type) {
        if (type == null) return 0;

        return switch (type) {
            case COAL -> 1;
            case COPPER -> 2;
            case IRON -> 3;
            default -> 0;
        };
    }

    public int sellAllItems() {
        int totalEarnings = 0;

        for (Map.Entry<ItemType, Integer> entry : inventory.entrySet()) {
            ItemType type = entry.getKey();
            int qty = entry.getValue();
            if (qty > 0) {
                totalEarnings += (qty * getPriceForType(type));
            }
        }

        // Clean out inventory and clear slider targets
        inventory.clear();
        saleQuantities.clear();

        return totalEarnings;
    }

    @Override
    public Building copyAt(int x, int y) {
        return new Core(x, y);
    }
}
