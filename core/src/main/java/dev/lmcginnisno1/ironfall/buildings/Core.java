package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;

import java.util.HashMap;
import java.util.Map;

public class Core extends Building {

    private final Map<ItemType, Integer> inventory = new HashMap<>();

    public Core(int x, int y) {
        super(x, y, 4, 4, Assets.core);
    }

    public void acceptItem(Item item) {
        ItemType type = item.type();
        inventory.put(type, inventory.getOrDefault(type, 0) + 1);
    }

    @Override
    public void update(float delta) {
        // Core has no active behavior yet
    }

    public Map<ItemType, Integer> getInventory() {
        return inventory;
    }

    public String getInventoryString() {
        StringBuilder sb = new StringBuilder();
        for (var entry : inventory.entrySet()) {
            sb.append(entry.getKey().name)
                .append(": ")
                .append(entry.getValue())
                .append("\n");
        }
        return sb.toString();
    }
}
