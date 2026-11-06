package dev.lmcginnisno1.ironfall.placement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.buildings.*;
import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.game.GameMode;
import dev.lmcginnisno1.ironfall.power.PowerBlock;
import dev.lmcginnisno1.ironfall.render.DrawUtil;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileType;

import java.util.ArrayList;

public class PlacementController {

    private final IronfallGame game;
    private final BuildingManager buildings;

    private final ConveyorPathHelper conveyorHelper;
    private Building prototype = null;

    // Active rotation state (index into ROTATION_ORDER below)
    public int currentRotation = 0;

    // Rotation cycle for the Q/E hotkeys. Deliberately NOT Direction.values()
    // -- Conveyor.Direction is declared { UP, DOWN, LEFT, RIGHT }, which is
    // not a valid 90-degree-per-step sequence (UP -> DOWN is a 180 degree
    // flip, not a turn). Indexing values() directly produced a glitchy
    // flip/turn/flip rotation. This array is a genuine clockwise sequence:
    // 0: East, 1: South, 2: West, 3: North.
    // 0: North, 1: East, 2: South, 3: West -- clockwise, starting at UP so
    // the default (unrotated) placement is vertical, matching expectation,
    // rather than starting sideways.
    private static final Conveyor.Direction[] ROTATION_ORDER = {
        Conveyor.Direction.UP,
        Conveyor.Direction.RIGHT,
        Conveyor.Direction.DOWN,
        Conveyor.Direction.LEFT
    };

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
        this.currentRotation = 0; // Reset orientation when picking up a new item
        game.mode = GameMode.PLACING_GENERIC;
    }

    /** Drops whatever is being placed, including a half-finished conveyor drag. */
    public void cancel() {
        prototype = null;
        draggingConveyor = false;
        cachedPath = null;
        cachedEndX = -1;
        cachedEndY = -1;
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
            // Hotkeys Q/E to rotate current placement candidate
            if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                rotateLeft();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                rotateRight();
            }

            updateGenericPlacement(leftJustPressed, leftJustReleased);
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

        // Conveyors and conduits are drag-placed along a path
        if (isPathBuilding()) {
            updateConveyorPlacement(leftJustPressed, leftJustReleased);
            return;
        }

        // Splitter is also special: rotatable, and its footprint swaps
        // width/height with rotation (the first non-square building).
        if (prototype instanceof Splitter) {
            updateSplitterPlacement(leftJustPressed);
            return;
        }

        int px = game.tileX - prototype.width / 2;
        int py = game.tileY - prototype.height / 2;

        boolean valid = buildings.canPlace(px, py, prototype.width, prototype.height);
        boolean canAfford = game.credits >= prototype.cost;
        valid &= canAfford;

        // Miners must sit on a single type of ore
        if (prototype.requiresOre()) {
            valid &= oreCheck(px, py, prototype.width, prototype.height);
        }
        valid &= terrainOk(prototype, px, py, prototype.width, prototype.height);

        if (leftJustPressed && valid) {
            game.credits -= prototype.cost;
            buildings.place(prototype.copyAt(px, py));
        }

        if (cancelRequested()) {
            prototype = null;
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderGenericGhost(SpriteBatch batch) {
        if (prototype == null) return;

        // Conveyor/conduit ghost handled separately
        if (isPathBuilding()) {
            renderConveyorGhost(batch);
            return;
        }

        // Splitter ghost handled separately (rotatable, swapping footprint)
        if (prototype instanceof Splitter) {
            renderSplitterGhost(batch);
            return;
        }

        int px = game.tileX - prototype.width / 2;
        int py = game.tileY - prototype.height / 2;

        boolean valid = buildings.canPlace(px, py, prototype.width, prototype.height);
        valid &= game.credits >= prototype.cost;

        // Miners must sit on a single type of ore
        if (prototype.requiresOre()) {
            valid &= oreCheck(px, py, prototype.width, prototype.height);
        }
        valid &= terrainOk(prototype, px, py, prototype.width, prototype.height);

        if (prototype instanceof PowerNode) {
            renderNodeRangePreview(batch, px, py);
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

    /** Shows a node's range and the power buildings it would link to if placed at (nx, ny). */
    private void renderNodeRangePreview(SpriteBatch batch, int nx, int ny) {
        float cx = (nx + 0.5f) * 16, cy = (ny + 0.5f) * 16;

        batch.setColor(1f, 0.85f, 0.2f, 0.5f);
        DrawUtil.dottedCircle(batch, cx, cy, Balance.POWER_NODE_RANGE * 16, 64, 2f);

        batch.setColor(1f, 0.85f, 0.2f, 0.6f);
        for (Building b : buildings.getBuildings()) {
            if (b instanceof PowerBlock && PowerNode.inRange(nx, ny, b.x, b.y, b.width, b.height)) {
                DrawUtil.line(batch, cx, cy, DrawUtil.centerX(b), DrawUtil.centerY(b), 1.5f);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void updateConveyorPlacement(boolean leftJustPressed, boolean leftJustReleased) {
        int tx = game.tileX;
        int ty = game.tileY;

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

            if (cachedPath != null && !cachedPath.isEmpty()) {
                // Tiles that are occupied or on water are skipped, and only placed tiles are paid for.
                int placeable = 0;
                for (Vector2 p : cachedPath) {
                    if (pathTileOk((int) p.x, (int) p.y)) placeable++;
                }
                int totalCost = placeable * prototype.cost;

                if (placeable > 0 && game.credits >= totalCost) {
                    game.credits -= totalCost;

                    for (int i = 0; i < cachedPath.size(); i++) {
                        Vector2 p = cachedPath.get(i);
                        if (!pathTileOk((int) p.x, (int) p.y)) continue;
                        Conveyor.Direction dir = conveyorHelper.getDirectionForIndex(cachedPath, i, prototype.getClass());
                        buildings.place(pathPiece((int) p.x, (int) p.y, dir));
                    }
                }
            } else {
                // Single-click placement using current manual rotation
                Conveyor.Direction dir = ROTATION_ORDER[currentRotation];
                if (pathTileOk(tx, ty) && game.credits >= prototype.cost) {
                    game.credits -= prototype.cost;
                    buildings.place(pathPiece(tx, ty, dir));
                }
            }

            cachedPath = null;
            cachedEndX = -1;
            cachedEndY = -1;
        }

        if (cancelRequested()) {
            draggingConveyor = false;
            cachedPath = null;
            cachedEndX = -1;
            cachedEndY = -1;
            prototype = null;
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderConveyorGhost(SpriteBatch batch) {
        if (!draggingConveyor) {
            int tx = game.tileX;
            int ty = game.tileY;

            boolean valid = pathTileOk(tx, ty) && game.credits >= prototype.cost;
            batch.setColor(valid ? 1f : 1f, valid ? 1f : 0.3f, valid ? 1f : 0.3f, 0.5f);

            Conveyor.Direction dir = ROTATION_ORDER[currentRotation];
            batch.draw(pathSprite(dir), tx * 16, ty * 16, 16, 16);
            batch.setColor(1f, 1f, 1f, 1f);
            return;
        }

        if (cachedPath == null) return;

        int placeable = 0;
        for (Vector2 p : cachedPath) {
            if (pathTileOk((int) p.x, (int) p.y)) placeable++;
        }
        boolean canAfford = game.credits >= placeable * prototype.cost;

        for (int i = 0; i < cachedPath.size(); i++) {
            Vector2 p = cachedPath.get(i);
            Conveyor.Direction dir = conveyorHelper.getDirectionForIndex(cachedPath, i, prototype.getClass());
            boolean ok = canAfford && pathTileOk((int) p.x, (int) p.y);

            batch.setColor(1f, ok ? 1f : 0.3f, ok ? 1f : 0.3f, 0.5f);
            batch.draw(pathSprite(dir), p.x * 16, p.y * 16, 16, 16);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void updateSplitterPlacement(boolean leftJustPressed) {
        Conveyor.Direction facing = ROTATION_ORDER[currentRotation];

        // Cursor represents the back (input) tile -- convert to the actual
        // footprint origin, which depends on facing (see Splitter's own
        // originXFor/originYFor, the single source of truth for this math).
        int cx = game.tileX, cy = game.tileY;
        int ox = Splitter.originXFor(cx, cy, facing);
        int oy = Splitter.originYFor(cx, cy, facing);
        int w = Splitter.widthFor(facing);
        int h = Splitter.heightFor(facing);

        boolean valid = buildings.canPlace(ox, oy, w, h) && game.credits >= Splitter.COST
            && terrainOk(prototype, ox, oy, w, h);

        if (leftJustPressed && valid) {
            game.credits -= Splitter.COST;
            buildings.place(new Splitter(ox, oy, facing));
        }

        if (cancelRequested()) {
            prototype = null;
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderSplitterGhost(SpriteBatch batch) {
        Conveyor.Direction facing = ROTATION_ORDER[currentRotation];

        int cx = game.tileX, cy = game.tileY;
        int ox = Splitter.originXFor(cx, cy, facing);
        int oy = Splitter.originYFor(cx, cy, facing);
        int w = Splitter.widthFor(facing);
        int h = Splitter.heightFor(facing);

        boolean valid = buildings.canPlace(ox, oy, w, h) && game.credits >= Splitter.COST
            && terrainOk(prototype, ox, oy, w, h);

        batch.setColor(valid ? 1f : 1f, valid ? 1f : 0.3f, valid ? 1f : 0.3f, 0.6f);
        batch.draw(getSplitterSprite(facing), ox * 16, oy * 16, w * 16, h * 16);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private TextureRegion getSplitterSprite(Conveyor.Direction dir) {
        return switch (dir) {
            case UP -> Assets.splitterUp;
            case RIGHT -> Assets.splitterRight;
            case DOWN -> Assets.splitterDown;
            case LEFT -> Assets.splitterLeft;
        };
    }

    private TextureRegion getConveyorSprite(Conveyor.Direction dir) {
        return switch (dir) {
            case UP -> Assets.conveyorUp;
            case DOWN -> Assets.conveyorDown;
            case LEFT -> Assets.conveyorLeft;
            case RIGHT -> Assets.conveyorRight;
        };
    }

    /** Conveyors and conduits: placed by dragging a path, one tile per piece. */
    private boolean isPathBuilding() {
        return prototype instanceof Conveyor || prototype instanceof Conduit;
    }

    private Building pathPiece(int x, int y, Conveyor.Direction dir) {
        return prototype instanceof Conduit ? new Conduit(x, y, dir) : new Conveyor(x, y, dir);
    }

    private TextureRegion pathSprite(Conveyor.Direction dir) {
        return prototype instanceof Conduit ? Conduit.spriteFor(dir) : getConveyorSprite(dir);
    }

    private boolean pathTileOk(int x, int y) {
        return buildings.canPlace(x, y, 1, 1) && terrainOk(prototype, x, y, 1, 1);
    }

    /** Water blocks building, except for buildings that need it (pumps), which must cover at least one water tile. */
    private boolean terrainOk(Building proto, int x, int y, int w, int h) {
        int water = 0;
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                if (game.engine.getTile(x + dx, y + dy) == TileType.WATER.id) water++;
            }
        }
        return proto.requiresWater() ? water > 0 : water == 0;
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

    public void rotateLeft() {
        currentRotation = (currentRotation + 3) % 4; // Counter-clockwise
    }

    public void rotateRight() {
        currentRotation = (currentRotation + 1) % 4; // Clockwise
    }

    /** True on the frame the player asks to cancel placement, via either input. */
    private boolean cancelRequested() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)
            || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }
}
