package dev.lmcginnisno1.ironfall.game;

import dev.lmcginnisno1.ironfall.items.ItemType;

/**
 * Every tunable number in the game lives here. If you're rebalancing,
 * this is the one file to open — nothing outside this file should
 * hardcode a price, cost, rate, or cap; it should reference a constant
 * from here instead.
 */
public final class Balance {

    private Balance() {} // constants only, never instantiated

    // --- Starting state ---

    // Enough for one Basic Miner plus a short belt run to the Core, so a
    // fresh save isn't a softlock before you've sold anything.
    public static final int STARTING_CREDITS = 100;

    // --- Building costs (credits) ---

    public static final int MINER_COST = 50;
    public static final int CONVEYOR_COST_PER_TILE = 2;

    // --- Building behavior ---

    public static final float MINER_BASE_RATE_PER_ORE_TILE = 0.25f; // ore/sec per ore tile covered
    public static final float CONVEYOR_MOVE_TIME = 0.25f;           // seconds to cross one tile (4 items/sec)
    public static final float CONVEYOR_ITEM_SPACING = 0.33f;        // minimum gap between items on a belt

    // --- Resource sell prices (credits per item) ---

    public static final int COAL_SELL_PRICE = 1;
    public static final int COPPER_SELL_PRICE = 2;
    public static final int IRON_SELL_PRICE = 3;

    // Ingots sell at 4x their source ore's price — smelting is meant to be
    // a clear value-add over just selling raw ore.
    public static final int IRON_INGOT_SELL_PRICE = IRON_SELL_PRICE * 4;     // 12
    public static final int COPPER_INGOT_SELL_PRICE = COPPER_SELL_PRICE * 4; // 8

    public static int sellPriceOf(ItemType type) {
        if (type == null) return 0;

        return switch (type) {
            case COAL -> COAL_SELL_PRICE;
            case COPPER -> COPPER_SELL_PRICE;
            case IRON -> IRON_SELL_PRICE;
            case IRON_INGOT -> IRON_INGOT_SELL_PRICE;
            case COPPER_INGOT -> COPPER_INGOT_SELL_PRICE;
        };
    }

    // --- Smelter ---

    public static final int SMELTER_COST = 75;                // credits
    public static final int SMELTER_MAX_BUFFER = 10;           // per ingredient (coal, iron, copper)
    public static final int SMELTER_ORE_REQUIREMENT = 1;       // ore consumed per craft
    public static final int SMELTER_COAL_REQUIREMENT = 1;      // coal consumed per craft
    public static final float SMELTER_CRAFT_TIME = 2.0f;       // seconds per ingot

    // --- Splitter ---

    public static final int SPLITTER_COST = 20;                // credits, flat (not per-tile like Conveyor)
    public static final int SPLITTER_MAX_BUFFER = 4;           // items queued internally awaiting a clear output

    // --- Power (all rates in power units per second) ---

    // Combustion generator: burns one coal per COAL_BURN_TIME seconds, but
    // only while something on its grid wants power.
    public static final int GENERATOR_COST = 100;              // credits
    public static final float GENERATOR_OUTPUT = 60f;          // power/sec while burning
    public static final float GENERATOR_COAL_BURN_TIME = 2.0f; // seconds per coal (0.5 coal/sec)
    public static final int GENERATOR_MAX_COAL = 10;           // coal buffered inside

    // Power node: links every power building within range to one grid.
    public static final int POWER_NODE_COST = 15;              // credits
    public static final float POWER_NODE_RANGE = 6f;           // tiles, from node centre to nearest edge of target

    // Electric miner: tier-2, 3x3, faster per ore tile than the Basic Miner.
    // One generator (60/sec) runs two of these at full speed.
    public static final int ELECTRIC_MINER_COST = 150;         // credits
    public static final float ELECTRIC_MINER_RATE_PER_ORE_TILE = 0.3f; // ore/sec per ore tile at full power
    public static final float ELECTRIC_MINER_POWER_USE = 30f;  // power/sec while mining

    // --- Upgrades: shared ---

    public static final int UPGRADE_MAX_LEVEL = 5;

    // --- Upgrades: miner speed (paid in Iron) ---

    public static final int MINER_UPGRADE_BASE_COST = 20;         // Iron, per level
    public static final float MINER_UPGRADE_BONUS_PER_LEVEL = 0.15f; // +15% mining rate per level

    // --- Upgrades: belt speed (paid in Copper) ---

    public static final int BELT_UPGRADE_BASE_COST = 15;          // Copper, per level
    public static final float BELT_UPGRADE_BONUS_PER_LEVEL = 0.15f; // +15% belt speed per level

    // --- Upgrades: storage capacity (paid in Coal) ---

    public static final int STORAGE_UPGRADE_BASE_COST = 25;       // Coal, per level
    public static final int STORAGE_BASE_CAP = 200;               // per resource type, at level 0
    public static final int STORAGE_CAP_PER_LEVEL = 100;          // added per level, per resource type
}
