package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;
import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.save.SaveIO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Conveyor extends Building implements ItemAcceptor {

    public enum Direction { UP, DOWN, LEFT, RIGHT }
    public final Direction direction;

    // Mindustry-style moving item
    public static class MovingItem {
        public Item item;
        public float progress; // 0 → 1
    }

    // Multiple items per tile
    private final Array<MovingItem> items = new Array<>();

    // Movement timing
    private static final float MOVE_TIME = Balance.CONVEYOR_MOVE_TIME;
    private static final float SPACING = Balance.CONVEYOR_ITEM_SPACING;

    // Cosmetic belt animation
    private float animPhase = 0f;

    public static final int COST = Balance.CONVEYOR_COST_PER_TILE;

    public Conveyor(int x, int y, Direction direction) {
        super(x, y, 1, 1, getSpriteFor(direction), COST);
        this.direction = direction;
    }

    private static TextureRegion getSpriteFor(Direction dir) {
        return switch (dir) {
            case UP -> Assets.conveyorUp;
            case DOWN -> Assets.conveyorDown;
            case LEFT -> Assets.conveyorLeft;
            case RIGHT -> Assets.conveyorRight;
        };
    }

    private boolean canAcceptAnotherItem() {
        if (items.size == 0) return true;
        return items.peek().progress >= SPACING;
    }

    @Override
    public boolean acceptItem(Item item, int fromX, int fromY) {
        // Refuse items from the tile this belt feeds into, so a belt pointing
        // at a building never takes that building's output and jams.
        if (fromX == x + dx(direction) && fromY == y + dy(direction)) return false;
        if (!canAcceptAnotherItem()) return false;

        MovingItem m = new MovingItem();
        m.item = item;
        m.progress = 0f;
        items.add(m);
        return true;
    }

    public static int dx(Direction d) {
        return switch (d) {
            case LEFT -> -1;
            case RIGHT -> 1;
            default -> 0;
        };
    }

    public static int dy(Direction d) {
        return switch (d) {
            case DOWN -> -1;
            case UP -> 1;
            default -> 0;
        };
    }

    @Override
    public void update(float delta) {
        // Cosmetic belt animation
        animPhase += delta / MOVE_TIME;
        if (animPhase >= 1f) animPhase -= 1f;

        if (items.size == 0) return;

        float speed = (delta / MOVE_TIME) * world.getUpgrades().beltSpeedMultiplier();

        for (int i = 0; i < items.size; i++) {
            MovingItem m = items.get(i);

            // Enforce spacing behind previous item
            if (i > 0) {
                MovingItem prev = items.get(i - 1);
                float maxProgress = prev.progress - SPACING;
                if (m.progress > maxProgress) {
                    m.progress = maxProgress;
                }
            }

            m.progress += speed;

            if (m.progress >= 1f) {
                tryMoveForward(m);
            }
        }
    }

    private void tryMoveForward(MovingItem m) {
        if (pushItem(m.item, x + dx(direction), y + dy(direction))) {
            items.removeValue(m, true);
            return;
        }

        // Blocked → wait at edge
        m.progress = 0.99f;
    }

    // Used by renderer
    public Array<MovingItem> getItems() {
        return items;
    }

    @Override
    public Building copyAt(int x, int y) {
        return new Conveyor(x, y, this.direction);
    }

    @Override
    public BuildingType type() {
        return BuildingType.CONVEYOR;
    }

    @Override
    public Direction facing() {
        return direction;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeInt(items.size);
        for (MovingItem m : items) {
            SaveIO.writeItemType(out, m.item.type());
            out.writeFloat(m.progress);
        }
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        items.clear();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            ItemType type = SaveIO.readItemType(in);
            float progress = in.readFloat();
            if (type == null) continue;

            MovingItem m = new MovingItem();
            m.item = new Item(type);
            m.progress = progress;
            items.add(m);
        }
    }
}
