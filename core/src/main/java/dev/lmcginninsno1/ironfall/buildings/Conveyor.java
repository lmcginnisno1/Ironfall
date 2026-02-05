package dev.lmcginninsno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.lmcginninsno1.ironfall.Assets;

public class Conveyor extends Building {

    public enum Direction { UP, DOWN, LEFT, RIGHT }
    public final Direction direction;

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
}
