package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.items.Item;

public class Conveyor extends Building {

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
    private static final float MOVE_TIME = 0.25f; // 4 items/sec
    private static final float SPACING = 0.33f;   // minimum distance between items

    // Cosmetic belt animation (unchanged)
    private float animPhase = 0f;

    public Conveyor(int x, int y, Direction direction) {
        super(x, y, 1, 1, getSpriteFor(direction));
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

    // Called by upstream belts
    public boolean canAcceptAnotherItem() {
        if (items.size == 0) return true;
        return items.peek().progress >= SPACING;
    }

    public void addIncomingItem(Item item) {
        MovingItem m = new MovingItem();
        m.item = item;
        m.progress = 0f;
        items.add(m);
    }

    @Override
    public void update(float delta) {
        // Cosmetic belt animation
        animPhase += delta / MOVE_TIME;
        if (animPhase >= 1f) animPhase -= 1f;

        if (items.size == 0) return;

        float speed = delta / MOVE_TIME;

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
        int nx = x, ny = y;

        switch (direction) {
            case UP -> ny++;
            case DOWN -> ny--;
            case LEFT -> nx--;
            case RIGHT -> nx++;
        }

        Building b = world.getAt(nx, ny);

        // Move to next conveyor
        if (b instanceof Conveyor next) {
            if (next.canAcceptAnotherItem()) {
                next.addIncomingItem(m.item);
                items.removeValue(m, true);
                return;
            }
        }

        // Move into core
        if (b instanceof Core core) {
            core.acceptItem(m.item);
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

    public float getAnimPhase() {
        return animPhase;
    }
}
