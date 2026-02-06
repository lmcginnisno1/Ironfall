package dev.lmcginninsno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.lmcginninsno1.ironfall.Assets;
import dev.lmcginninsno1.ironfall.Item;

public class Conveyor extends Building {

    public enum Direction { UP, DOWN, LEFT, RIGHT }
    public final Direction direction;

    private Item item;

    // 4 items/sec → 0.25s per move
    private static final float MOVE_TIME = 0.25f;
    private float moveCooldown = 0f;

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

    public boolean canAcceptItem() {
        return item == null;
    }

    public void acceptItem(Item item) {
        this.item = item;
    }

    @Override
    public void update(float delta) {
        if (item == null) return;

        moveCooldown -= delta;
        if (moveCooldown > 0f) return;

        tryMoveForward();
        moveCooldown = MOVE_TIME;
    }

    private void tryMoveForward() {
        int nx = x;
        int ny = y;

        switch (direction) {
            case UP -> ny += 1;
            case DOWN -> ny -= 1;
            case LEFT -> nx -= 1;
            case RIGHT -> nx += 1;
        }

        Building b = world.getAt(nx, ny);

        if (b instanceof Conveyor next && next.canAcceptItem()) {
            next.acceptItem(item);
            item = null;
            return;
        }

        if (b instanceof Core core) {
            core.acceptItem(item);
            item = null;
        }
    }
}
