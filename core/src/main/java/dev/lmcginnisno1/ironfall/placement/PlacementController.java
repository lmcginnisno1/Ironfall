package dev.lmcginnisno1.ironfall.placement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.buildings.*;
import dev.lmcginnisno1.ironfall.game.GameMode;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileType;

import java.util.ArrayList;

public class PlacementController {

    private final IronfallGame game;
    private final BuildingManager buildings;

    private final ConveyorPathHelper conveyorHelper;
    private Building prototype = null;

    // Conveyor drag state
    private boolean draggingConveyor = false;
    private int dragStartX, dragStartY;
    private boolean prevLeftDown = false;
    private boolean dragDirectionLocked = false;
    private boolean horizontalFirst = true;

    private ArrayList<Vector2> cachedPath = null;
    private int cachedEndX = -1, cachedEndY = -1;

    // Guards against the same mouse-down that selected a sidebar entry
    // also being read as the start of a drag/placement this same frame.
    private boolean justSelected = false;

    public PlacementController(IronfallGame game, BuildingManager buildings) {
        this.game = game;
        this.buildings = buildings;
        this.conveyorHelper = new ConveyorPathHelper(buildings);
    }

    // Called by sidebar
    public void setPrototype(Building proto) {
        this.prototype = proto;
        this.justSelected = true;
        game.mode = GameMode.PLACING_GENERIC;
    }

    public void update() {

        boolean leftDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean leftJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean leftJustReleased = prevLeftDown && !leftDown;
        prevLeftDown = leftDown;

        if (justSelected) {
            // Swallow this frame entirely: it's still the same click that
            // opened the sidebar entry, so it must not also start a drag.
            justSelected = false;
            return;
        }

        if (game.mode == GameMode.PLACING_GENERIC) {
            updateGenericPlacement(leftJustPressed, leftJustReleased);
            // Conveyor drag still works inside generic placement
        }
    }

    public void render(SpriteBatch batch) {
        if (game.mode == GameMode.PLACING_GENERIC) {
            renderGenericGhost(batch);
        }
    }

    private void updateGenericPlacement(boolean leftJustPressed, boolean leftJustReleased) {
        if (prototype == null) {
            game.mode = GameMode.NORMAL;
            return;
        }

        // Conveyor placement is special
        if (prototype instanceof Conveyor) {
            updateConveyorPlacement(leftJustPressed, leftJustReleased);
            return;
        }

        int px = game.tileX - prototype.width / 2;
        int py = game.tileY - prototype.height / 2;

        boolean valid = buildings.canPlace(px, py, prototype.width, prototype.height);

        // Miner-specific validation
        if (prototype instanceof BasicMiner) {
            valid &= oreCheck(px, py, prototype.width, prototype.height);
        }

        if (leftJustPressed && valid) {
            buildings.place(prototype.copyAt(px, py));
            prototype = null;
            game.mode = GameMode.NORMAL;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            prototype = null;
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderGenericGhost(SpriteBatch batch) {
        if (prototype == null) return;

        // Conveyor ghost handled separately
        if (prototype instanceof Conveyor) {
            renderConveyorGhost(batch);
            return;
        }

        int px = game.tileX - prototype.width / 2;
        int py = game.tileY - prototype.height / 2;

        boolean valid = buildings.canPlace(px, py, prototype.width, prototype.height);

        // Miner-specific validation
        if (prototype instanceof BasicMiner) {
            valid &= oreCheck(px, py, prototype.width, prototype.height);
        }

        batch.setColor(valid ? 0f : 1f, valid ? 1f : 0f, 0f, 0.5f);
        batch.draw(
            prototype.getSprite(),
            px * 16,
            py * 16,
            prototype.width * 16,
            prototype.height * 16
        );
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void updateConveyorPlacement(boolean leftJustPressed, boolean leftJustReleased) {
        int tx = game.tileX;
        int ty = game.tileY;

        // Start drag only on a genuinely fresh press — not just "button
        // currently held" — so a click-and-hold carried over from selecting
        // the sidebar entry can't be reused to start a drag at that spot.
        if (!draggingConveyor && leftJustPressed) {
            draggingConveyor = true;
            dragStartX = tx;
            dragStartY = ty;
            dragDirectionLocked = false;
            cachedPath = null;
            cachedEndX = -1;
            cachedEndY = -1;
        }

        if (draggingConveyor && !dragDirectionLocked) {
            int dx = Math.abs(tx - dragStartX);
            int dy = Math.abs(ty - dragStartY);

            if (dx > 0 || dy > 0) {
                horizontalFirst = dx >= dy;
                dragDirectionLocked = true;
            }
        }

        if (draggingConveyor && dragDirectionLocked && (tx != cachedEndX || ty != cachedEndY)) {
            cachedPath = conveyorHelper.computePath(dragStartX, dragStartY, tx, ty, horizontalFirst);
            cachedEndX = tx;
            cachedEndY = ty;
        }

        if (draggingConveyor && leftJustReleased) {
            draggingConveyor = false;

            if (cachedPath != null) {
                for (int i = 0; i < cachedPath.size(); i++) {
                    Vector2 p = cachedPath.get(i);
                    Conveyor.Direction dir = conveyorHelper.getDirectionForIndex(cachedPath, i);
                    buildings.place(new Conveyor((int)p.x, (int)p.y, dir));
                }
            }

            cachedPath = null;
            cachedEndX = -1;
            cachedEndY = -1;
            prototype = null;
            game.mode = GameMode.NORMAL;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            draggingConveyor = false;
            cachedPath = null;
            cachedEndX = -1;
            cachedEndY = -1;
            prototype = null;
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderConveyorGhost(SpriteBatch batch) {
        if (!draggingConveyor || cachedPath == null) return;

        for (int i = 0; i < cachedPath.size(); i++) {
            Vector2 p = cachedPath.get(i);
            Conveyor.Direction dir = conveyorHelper.getDirectionForIndex(cachedPath, i);

            batch.setColor(1f, 1f, 1f, 0.5f);
            batch.draw(getConveyorSprite(dir), p.x * 16, p.y * 16, 16, 16);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private TextureRegion getConveyorSprite(Conveyor.Direction dir) {
        return switch (dir) {
            case UP -> Assets.conveyorUp;
            case DOWN -> Assets.conveyorDown;
            case LEFT -> Assets.conveyorLeft;
            case RIGHT -> Assets.conveyorRight;
        };
    }

    private boolean oreCheck(int x, int y, int w, int h) {
        TileType oreType = null;
        int oreCount = 0;

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                TileType t = TileType.fromId(game.engine.getTile(x + dx, y + dy));

                if (t == TileType.COAL || t == TileType.IRON || t == TileType.COPPER) {
                    if (oreType == null) oreType = t;
                    if (t != oreType) return false;  // must be uniform ore
                    oreCount++;
                }
            }
        }

        return oreCount >= 1;  // must have at least one ore tile
    }
}
