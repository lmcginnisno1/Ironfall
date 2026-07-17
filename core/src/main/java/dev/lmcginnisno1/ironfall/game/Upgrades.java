package dev.lmcginnisno1.ironfall.game;

import dev.lmcginnisno1.ironfall.items.ItemType;

/**
 * Global, account-wide upgrades. Deliberately simple for now: three tracks
 * (miner speed, belt speed, storage capacity), each with a fixed number of
 * levels, each level costing more of a single resource than the last. Paid
 * for out of the Core's stored (unsold) inventory — resources fund
 * upgrades, credits fund buildings.
 */
public class Upgrades {

    public static final int MAX_LEVEL = 5;

    private static final int MINER_BASE_COST = 20;   // Iron, per level
    private static final int BELT_BASE_COST = 15;    // Copper, per level
    private static final int STORAGE_BASE_COST = 25; // Coal, per level

    private static final float MINER_SPEED_BONUS_PER_LEVEL = 0.15f; // +15% mining rate per level
    private static final float BELT_SPEED_BONUS_PER_LEVEL = 0.15f;  // +15% belt speed per level

    private static final int BASE_STORAGE_CAP = 200;       // per resource type, at level 0
    private static final int STORAGE_CAP_PER_LEVEL = 100;  // added per level, per resource type

    public int minerSpeedLevel = 0;
    public int beltSpeedLevel = 0;
    public int storageCapacityLevel = 0;

    public boolean isMinerMaxed() {
        return minerSpeedLevel >= MAX_LEVEL;
    }

    public boolean isBeltMaxed() {
        return beltSpeedLevel >= MAX_LEVEL;
    }

    public boolean isStorageMaxed() {
        return storageCapacityLevel >= MAX_LEVEL;
    }

    public ItemType minerUpgradeResource() {
        return ItemType.IRON;
    }

    public ItemType beltUpgradeResource() {
        return ItemType.COPPER;
    }

    public ItemType storageUpgradeResource() {
        return ItemType.COAL;
    }

    /** Cost, in minerUpgradeResource(), of the next miner speed level. */
    public int minerUpgradeCost() {
        return MINER_BASE_COST * (minerSpeedLevel + 1);
    }

    /** Cost, in beltUpgradeResource(), of the next belt speed level. */
    public int beltUpgradeCost() {
        return BELT_BASE_COST * (beltSpeedLevel + 1);
    }

    /** Cost, in storageUpgradeResource(), of the next storage capacity level. */
    public int storageUpgradeCost() {
        return STORAGE_BASE_COST * (storageCapacityLevel + 1);
    }

    public float minerSpeedMultiplier() {
        return 1f + minerSpeedLevel * MINER_SPEED_BONUS_PER_LEVEL;
    }

    public float beltSpeedMultiplier() {
        return 1f + beltSpeedLevel * BELT_SPEED_BONUS_PER_LEVEL;
    }

    /** Per-resource-type storage cap in the Core, at the current storage level. */
    public int storageCap() {
        return BASE_STORAGE_CAP + storageCapacityLevel * STORAGE_CAP_PER_LEVEL;
    }
}
