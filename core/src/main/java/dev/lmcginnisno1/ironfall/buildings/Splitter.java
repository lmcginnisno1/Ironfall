package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.util.ArrayDeque;

/**
 * A 1x2 (or 2x1, rotated) footprint building: two parallel lanes that get
 * redistributed between each other. Both tiles are functionally identical
 * -- each has its own input, arriving from the direction opposite facing,
 * and its own output, emerging in the facing direction. Items entering
 * either input feed a shared buffer, which alternates emitting to whichever
 * output has room (falling back to the other if one's blocked, stalling if
 * both are).
 */
public class Splitter extends Building {

    public static final int COST = Balance.SPLITTER_COST;

    private final Conveyor.Direction facing;

    private final int inputAX, inputAY;
    private final int inputBX, inputBY;
    private final int outputAX, outputAY;
    private final int outputBX, outputBY;

    private final ArrayDeque<Item> buffer = new ArrayDeque<>();

    // Alternation state: false = try output A first, true = try B first.
    private boolean preferB = false;

    public Splitter(int x, int y, Conveyor.Direction facing) {
        super(x, y, widthFor(facing), heightFor(facing), spriteFor(facing), COST);
        this.facing = facing;

        int tileAX = x, tileAY = y;
        int tileBX = x + (widthFor(facing) - 1);
        int tileBY = y + (heightFor(facing) - 1);

        Conveyor.Direction outDir = facing;
        Conveyor.Direction inDir = opposite(facing);

        inputAX = tileAX + dx(inDir);
        inputAY = tileAY + dy(inDir);
        inputBX = tileBX + dx(inDir);
        inputBY = tileBY + dy(inDir);

        outputAX = tileAX + dx(outDir);
        outputAY = tileAY + dy(outDir);
        outputBX = tileBX + dx(outDir);
        outputBY = tileBY + dy(outDir);
    }

    public static int widthFor(Conveyor.Direction facing) {
        return (facing == Conveyor.Direction.UP || facing == Conveyor.Direction.DOWN) ? 2 : 1;
    }

    public static int heightFor(Conveyor.Direction facing) {
        return (facing == Conveyor.Direction.UP || facing == Conveyor.Direction.DOWN) ? 1 : 2;
    }

    // The cursor tile is a fixed pivot: it's always one of the splitter's two
    // tiles, and the other tile swings around it on the opposite side as facing rotates.
    // Origin is the bottom-left tile of the resulting footprint.
    public static int originXFor(int cursorX, int cursorY, Conveyor.Direction facing) {
        return switch (facing) {
            case UP -> cursorX - 1;
            case DOWN -> cursorX;
            default -> cursorX;
        };
    }

    public static int originYFor(int cursorX, int cursorY, Conveyor.Direction facing) {
        return switch (facing) {
            case LEFT -> cursorY - 1;
            default -> cursorY;
        };
    }

    private static TextureRegion spriteFor(Conveyor.Direction facing) {
        return switch (facing) {
            case UP -> Assets.splitterUp;
            case RIGHT -> Assets.splitterRight;
            case DOWN -> Assets.splitterDown;
            case LEFT -> Assets.splitterLeft;
        };
    }

    private static Conveyor.Direction opposite(Conveyor.Direction d) {
        return switch (d) {
            case UP -> Conveyor.Direction.DOWN;
            case DOWN -> Conveyor.Direction.UP;
            case LEFT -> Conveyor.Direction.RIGHT;
            case RIGHT -> Conveyor.Direction.LEFT;
        };
    }

    private static int dx(Conveyor.Direction d) {
        return switch (d) {
            case LEFT -> -1;
            case RIGHT -> 1;
            default -> 0;
        };
    }

    private static int dy(Conveyor.Direction d) {
        return switch (d) {
            case DOWN -> -1;
            case UP -> 1;
            default -> 0;
        };
    }

    /**
     * Accepts an item from a conveyor only if the item is entering from
     * the back face (opposite to facing direction) relative to the target tile.
     */
    public boolean acceptItem(Item item, int fromX, int fromY, int toX, int toY) {
        // Compute where an item would have to come from to enter through the back of toX, toY
        int expectedFromX = toX;
        int expectedFromY = toY;

        switch (facing) {
            case UP -> expectedFromY--;
            case DOWN -> expectedFromY++;
            case LEFT -> expectedFromX++;
            case RIGHT -> expectedFromX--;
        }

        // Verify that the delivery is coming directly from the back position
        if (fromX != expectedFromX || fromY != expectedFromY) {
            return false;
        }

        // Buffer limits check (optional max buffer size safeguard, if needed)
        buffer.add(item);
        return true;
    }

    @Override
    public void update(float delta) {
        if (world == null) return;
        if (buffer.isEmpty()) return;

        Item next = buffer.peek();

        boolean success = preferB
            ? (tryOutput(next, outputBX, outputBY) || tryOutput(next, outputAX, outputAY))
            : (tryOutput(next, outputAX, outputAY) || tryOutput(next, outputBX, outputBY));

        if (success) {
            buffer.poll();
            preferB = !preferB;
        }
    }

    private boolean tryOutput(Item item, int tx, int ty) {
        Building b = world.getAt(tx, ty);

        if (b instanceof Conveyor c && c.canAcceptAnotherItem()) {
            c.addIncomingItem(item);
            return true;
        }
        if (b instanceof Core core) {
            return core.acceptItem(item);
        }
        if (b instanceof Smelter smelter) {
            return smelter.acceptItem(item);
        }
        if (b instanceof Splitter splitter) {
            return splitter.acceptItem(item, tx, ty, outputAX, outputAY);
        }

        return false;
    }

    @Override
    public Building copyAt(int x, int y) {
        return new Splitter(x, y, facing);
    }
}
