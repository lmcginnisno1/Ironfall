package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;
import dev.lmcginnisno1.ironfall.tiles.Assets;

public class Smelter extends Building {

    public static final int COST = Balance.SMELTER_COST;

    private int coalBuffer = 0;
    private int ironBuffer = 0;
    private int copperBuffer = 0;

    private float progress = 0f;
    private ItemType currentTargetOutput = null;

    // Alternation state: when both Iron and Copper are simultaneously
    // ready to craft, this decides which one gets picked this cycle, then
    // flips — so a smelter fed both metals doesn't perpetually starve one
    // in favor of the other.
    private boolean preferCopperNext = false;

    public Smelter(int x, int y) {
        super(x, y, 2, 2, Assets.smelter, COST);
    }

    public boolean acceptItem(Item item) {
        if (item == null) return false;

        if (item.type() == ItemType.COAL && coalBuffer < Balance.SMELTER_MAX_BUFFER) {
            coalBuffer++;
            return true;
        } else if (item.type() == ItemType.IRON && ironBuffer < Balance.SMELTER_MAX_BUFFER) {
            ironBuffer++;
            return true;
        } else if (item.type() == ItemType.COPPER && copperBuffer < Balance.SMELTER_MAX_BUFFER) {
            copperBuffer++;
            return true;
        }
        return false;
    }

    @Override
    public void update(float delta) {
        if (world == null) return;

        // Determine active target output based on available stock
        if (currentTargetOutput == null) {
            boolean ironReady = ironBuffer >= Balance.SMELTER_ORE_REQUIREMENT
                && coalBuffer >= Balance.SMELTER_COAL_REQUIREMENT;
            boolean copperReady = copperBuffer >= Balance.SMELTER_ORE_REQUIREMENT
                && coalBuffer >= Balance.SMELTER_COAL_REQUIREMENT;

            if (ironReady && copperReady) {
                // Both ready at once: alternate so neither metal starves
                // the other when both are continuously being fed in.
                currentTargetOutput = preferCopperNext ? ItemType.COPPER_INGOT : ItemType.IRON_INGOT;
                preferCopperNext = !preferCopperNext;
            } else if (ironReady) {
                currentTargetOutput = ItemType.IRON_INGOT;
            } else if (copperReady) {
                currentTargetOutput = ItemType.COPPER_INGOT;
            }
        }

        if (currentTargetOutput != null) {
            progress += delta;
            if (progress >= Balance.SMELTER_CRAFT_TIME) {
                if (tryOutputIngot(currentTargetOutput)) {
                    // Consume ingredients on successful output
                    coalBuffer -= Balance.SMELTER_COAL_REQUIREMENT;
                    if (currentTargetOutput == ItemType.IRON_INGOT) {
                        ironBuffer -= Balance.SMELTER_ORE_REQUIREMENT;
                    } else {
                        copperBuffer -= Balance.SMELTER_ORE_REQUIREMENT;
                    }
                    progress = 0f;
                    currentTargetOutput = null;
                }
                // If tryOutputIngot failed (no room anywhere to put the
                // ingot), progress and buffers are left untouched — the
                // craft just waits at 100% until somewhere opens up. Same
                // backpressure behavior as a jammed conveyor or a full Core.
            }
        }
    }

    private boolean tryOutputIngot(ItemType ingotType) {
        // Check surrounding perimeter of the 2x2 footprint
        for (int checkX = x - 1; checkX <= x + 2; checkX++) {
            for (int checkY = y - 1; checkY <= y + 2; checkY++) {

                // Skip inner footprint tiles
                if (checkX >= x && checkX <= x + 1 && checkY >= y && checkY <= y + 1) {
                    continue;
                }

                // Skip the four diagonal corners -- they only touch the smelter
                // at a point, so they aren't real neighbours.
                boolean outsideX = checkX < x || checkX > x + 1;
                boolean outsideY = checkY < y || checkY > y + 1;
                if (outsideX && outsideY) {
                    continue;
                }

                Building building = world.getAt(checkX, checkY);

                if (building instanceof Core core) {
                    // Respect the Core's storage cap. If it's full for this
                    // ingot type, don't give up — keep scanning the rest of
                    // the perimeter in case a conveyor on another side can
                    // still take it.
                    if (core.acceptItem(new Item(ingotType))) {
                        return true;
                    }
                    continue;
                }

                if (building instanceof Conveyor conveyor) {
                    if (isConveyorFacingAway(conveyor, checkX, checkY) && conveyor.canAcceptAnotherItem()) {
                        conveyor.addIncomingItem(new Item(ingotType));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isConveyorFacingAway(Conveyor conveyor, int cx, int cy) {
        int targetX = cx;
        int targetY = cy;

        switch (conveyor.direction) {
            case RIGHT -> targetX++; // East
            case UP -> targetY++; // North
            case LEFT -> targetX--; // West
            case DOWN -> targetY--; // South
        }

        return targetX < x || targetX > x + 1 || targetY < y || targetY > y + 1;
    }

    @Override
    public Building copyAt(int x, int y) {
        return new Smelter(x, y);
    }
}
