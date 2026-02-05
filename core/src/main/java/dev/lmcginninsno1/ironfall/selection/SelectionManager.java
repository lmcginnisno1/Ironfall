package dev.lmcginninsno1.ironfall.selection;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import dev.lmcginninsno1.ironfall.GameMode;
import dev.lmcginninsno1.ironfall.IronfallGame;
import dev.lmcginninsno1.ironfall.buildings.Building;
import dev.lmcginninsno1.ironfall.buildings.BuildingManager;

public class SelectionManager {

    private final IronfallGame game;
    private final BuildingManager buildings;

    // Single selection
    private Building selected = null;

    // Multi-selection
    private final Array<Building> multi = new Array<>();
    private final Rectangle selectRect = new Rectangle();

    // Drag state
    private boolean dragging = false;
    private int dragStartX, dragStartY;

    public SelectionManager(IronfallGame game, BuildingManager buildings) {
        this.game = game;
        this.buildings = buildings;
    }

    // Called every frame from InputController
    public void update() {}

    public void trySelectAt(int tx, int ty) {
        clearSelection();

        Building b = buildings.getAt(tx, ty);
        if (b != null) {
            selected = b;
            game.mode = GameMode.SELECTING_SINGLE;
        }
    }

    public Building getSelected() {
        return selected;
    }

    public void deleteSelected() {
        if (selected != null) {
            buildings.remove(selected);
            selected = null;
        }
    }

    public void startDrag(int tx, int ty) {
        dragging = true;
        dragStartX = tx;
        dragStartY = ty;
    }

    public void updateDrag(int tx, int ty) {
        if (!dragging) return;

        int x1 = Math.min(dragStartX, tx);
        int y1 = Math.min(dragStartY, ty);
        int x2 = Math.max(dragStartX, tx);
        int y2 = Math.max(dragStartY, ty);

        selectRect.set(x1, y1, x2 - x1 + 1, y2 - y1 + 1);

        multi.clear();
        buildings.getAllInRect(selectRect, multi);
    }

    public void endDrag() {
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public Rectangle getSelectionRect() {
        return selectRect;
    }

    public Array<Building> getMultiSelection() {
        return multi;
    }

    public void deleteMulti() {
        for (Building b : multi) {
            buildings.remove(b);
        }
        multi.clear();
    }

    public void clearSelection() {
        selected = null;
        multi.clear();
        dragging = false;
    }
}
