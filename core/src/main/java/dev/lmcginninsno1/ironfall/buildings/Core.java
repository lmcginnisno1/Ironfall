package dev.lmcginninsno1.ironfall.buildings;

import dev.lmcginninsno1.ironfall.Assets;
import dev.lmcginninsno1.ironfall.Item;
import dev.lmcginninsno1.ironfall.TileType;

import java.util.HashMap;
import java.util.Map;

public class Core extends Building {

    private final Map<TileType, Integer> inventory = new HashMap<>();

    public Core(int x, int y) {
        super(x, y, 4, 4, Assets.core);
    }

    public void acceptItem(Item item) {
        TileType type = item.type;
        inventory.put(type, inventory.getOrDefault(type, 0) + 1);
    }

    @Override
    public void update(float delta) {
        // Core has no active behavior yet
    }

    public Map<TileType, Integer> getInventory() {
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
