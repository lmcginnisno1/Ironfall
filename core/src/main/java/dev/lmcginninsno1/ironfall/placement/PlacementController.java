package dev.lmcginninsno1.ironfall.placement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import dev.lmcginninsno1.ironfall.*;
import dev.lmcginninsno1.ironfall.buildings.*;

import java.util.ArrayList;

public class PlacementController {

    private final IronfallGame game;
    private final TileEngine engine;
    private final BuildingManager buildings;

    private final ConveyorPathHelper conveyorHelper;

    private boolean draggingConveyor = false;
    private int dragStartX, dragStartY;

    private boolean prevLeftDown = false;
    private boolean dragDirectionLocked = false;
    private boolean horizontalFirst = true;

    public PlacementController(IronfallGame game, TileEngine engine, BuildingManager buildings) {
        this.game = game;
        this.engine = engine;
        this.buildings = buildings;
        this.conveyorHelper = new ConveyorPathHelper(buildings);
    }

    public void update() {

        boolean leftDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean leftJustPressed = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        boolean leftJustReleased = prevLeftDown && !leftDown;
        prevLeftDown = leftDown;

        switch (game.mode) {
            case PLACING_MINER -> updateMinerPlacement(leftJustPressed);
            case PLACING_CORE -> updateCorePlacement(leftJustPressed);
            case PLACING_CONVEYOR -> updateConveyorPlacement(leftJustPressed, leftJustReleased);
        }
    }

    public void render(SpriteBatch batch) {
        switch (game.mode) {
            case PLACING_MINER -> renderMinerGhost(batch);
            case PLACING_CORE -> renderCoreGhost(batch);
            case PLACING_CONVEYOR -> renderConveyorGhost(batch);
        }
    }

    private void updateMinerPlacement(boolean leftJustPressed) {
        int px = game.tileX - 1;
        int py = game.tileY - 1;

        boolean valid = buildings.canPlace(px, py, 2, 2) &&
            oreCheck(px, py, 2, 2);

        if (leftJustPressed && valid) {
            buildings.place(new BasicMiner(px, py, engine));
            game.mode = GameMode.NORMAL;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderMinerGhost(SpriteBatch batch) {
        int px = game.tileX - 1;
        int py = game.tileY - 1;

        boolean valid = buildings.canPlace(px, py, 2, 2) &&
            oreCheck(px, py, 2, 2);

        batch.setColor(valid ? 0f : 1f, valid ? 1f : 0f, 0f, 0.5f);
        batch.draw(Assets.basicMiner, px * 16, py * 16, 32, 32);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void updateCorePlacement(boolean leftJustPressed) {
        int px = game.tileX - 2;
        int py = game.tileY - 2;

        boolean valid = buildings.canPlace(px, py, 4, 4);

        if (leftJustPressed && valid) {
            buildings.place(new Core(px, py));
            game.mode = GameMode.NORMAL;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderCoreGhost(SpriteBatch batch) {
        int px = game.tileX - 2;
        int py = game.tileY - 2;

        boolean valid = buildings.canPlace(px, py, 4, 4);

        batch.setColor(valid ? 0f : 1f, valid ? 1f : 0f, 0f, 0.5f);
        batch.draw(Assets.core, px * 16, py * 16, 64, 64);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void updateConveyorPlacement(boolean leftJustPressed, boolean leftJustReleased) {
        int tx = game.tileX;
        int ty = game.tileY;

        if (leftJustPressed) {
            draggingConveyor = true;
            dragStartX = tx;
            dragStartY = ty;
            dragDirectionLocked = false;
        }

        if (draggingConveyor && !dragDirectionLocked) {
            int dx = Math.abs(tx - dragStartX);
            int dy = Math.abs(ty - dragStartY);

            if (dx > 0 || dy > 0) {
                horizontalFirst = dx >= dy;
                dragDirectionLocked = true;
            }
        }

        if (draggingConveyor && leftJustReleased) {
            draggingConveyor = false;

            ArrayList<Vector2> path =
                conveyorHelper.computePath(dragStartX, dragStartY, tx, ty, horizontalFirst);

            for (int i = 0; i < path.size(); i++) {
                Vector2 p = path.get(i);
                Conveyor.Direction dir = conveyorHelper.getDirectionForIndex(path, i);
                buildings.place(new Conveyor((int)p.x, (int)p.y, dir));
            }

            game.mode = GameMode.NORMAL;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            draggingConveyor = false;
            game.mode = GameMode.NORMAL;
        }
    }

    private void renderConveyorGhost(SpriteBatch batch) {
        if (!draggingConveyor) return;

        int tx = game.tileX;
        int ty = game.tileY;

        ArrayList<Vector2> path =
            conveyorHelper.computePath(dragStartX, dragStartY, tx, ty, horizontalFirst);

        for (int i = 0; i < path.size(); i++) {
            Vector2 p = path.get(i);
            Conveyor.Direction dir = conveyorHelper.getDirectionForIndex(path, i);

            batch.setColor(1f, 1f, 1f, 0.5f);
            batch.draw(getConveyorSprite(dir), p.x * 16, p.y * 16, 16, 16);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private com.badlogic.gdx.graphics.g2d.TextureRegion getConveyorSprite(Conveyor.Direction dir) {
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
                TileType t = TileType.fromId(engine.getTile(x + dx, y + dy));

                if (t == TileType.COAL || t == TileType.IRON || t == TileType.COPPER) {
                    if (oreType == null) oreType = t;
                    if (t != oreType) return false;
                    oreCount++;
                }
            }
        }

        return oreCount >= 1;
    }
}
