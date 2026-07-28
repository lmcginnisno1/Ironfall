package dev.lmcginnisno1.ironfall.game;

import dev.lmcginnisno1.ironfall.items.ItemType;

/**
 * Global, account-wide upgrades. Deliberately simple for now: three tracks
 * (miner speed, belt speed, storage capacity), each with a fixed number of
 * levels, each level costing more of a single resource than the last. Paid
 * for out of the Core's stored (unsold) inventory — resources fund
 * upgrades, credits fund buildings.
 *
 * Level state (how far you've upgraded) lives here, since that's genuine
 * runtime game state. The tuning numbers themselves (costs, bonuses, caps)
 * live in Balance.java, the single source of truth for rebalancing.
 */
public class Upgrades {

    public int minerSpeedLevel = 0;
    public int beltSpeedLevel = 0;
    public int storageCapacityLevel = 0;

    public boolean isMinerMaxed() {
        return minerSpeedLevel >= Balance.UPGRADE_MAX_LEVEL;
    }

    public boolean isBeltMaxed() {
        return beltSpeedLevel >= Balance.UPGRADE_MAX_LEVEL;
    }

    public boolean isStorageMaxed() {
        return storageCapacityLevel >= Balance.UPGRADE_MAX_LEVEL;
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
        return Balance.MINER_UPGRADE_BASE_COST * (minerSpeedLevel + 1);
    }

    /** Cost, in beltUpgradeResource(), of the next belt speed level. */
    public int beltUpgradeCost() {
        return Balance.BELT_UPGRADE_BASE_COST * (beltSpeedLevel + 1);
    }

    /** Cost, in storageUpgradeResource(), of the next storage capacity level. */
    public int storageUpgradeCost() {
        return Balance.STORAGE_UPGRADE_BASE_COST * (storageCapacityLevel + 1);
    }

    public float minerSpeedMultiplier() {
        return 1f + minerSpeedLevel * Balance.MINER_UPGRADE_BONUS_PER_LEVEL;
    }

    public float beltSpeedMultiplier() {
        return 1f + beltSpeedLevel * Balance.BELT_UPGRADE_BONUS_PER_LEVEL;
    }

    /** Per-resource-type storage cap in the Core, at the current storage level. */
    public int storageCap() {
        return Balance.STORAGE_BASE_CAP + storageCapacityLevel * Balance.STORAGE_CAP_PER_LEVEL;
    }
}
