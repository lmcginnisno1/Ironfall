package dev.lmcginnisno1.ironfall.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.lmcginnisno1.ironfall.game.GameMode;
import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.selection.SelectionManager;
import dev.lmcginnisno1.ironfall.render.OverlayRenderer;

public class InputController {

    private final IronfallGame game;
    private final SelectionManager selection;
    private final OverlayRenderer overlay;

    public InputController(IronfallGame game, SelectionManager selection, OverlayRenderer overlay) {
        this.game = game;
        this.selection = selection;
        this.overlay = overlay;
    }

    public void update() {
        game.screenToTile(Gdx.input.getX(), Gdx.input.getY());

        switch (game.mode) {
            case NORMAL -> handleNormalMode();
            case SELECTING_SINGLE -> handleSingleSelectMode();
            case DELETE_MODE -> handleDeleteMode();
            case PLACING_MINER, PLACING_CORE, PLACING_CONVEYOR -> handlePlacementMode();
        }
    }

    private void handleNormalMode() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.mode = GameMode.PLACING_MINER;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            game.mode = GameMode.PLACING_CONVEYOR;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            game.mode = GameMode.DELETE_MODE;
            selection.clearSelection();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            game.showGrid = !game.showGrid;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handleLeftClick();
        }
    }

    private void handleLeftClick() {
        int sx = Gdx.input.getX();
        int sy = Gdx.input.getY();

        OverlayRenderer.UIResult ui = overlay.hitTest(sx, sy);

        if (ui.type() != OverlayRenderer.UIResultType.NONE) {
            overlay.handleUIAction(ui);
            return;
        }

        selection.trySelectAt(game.tileX, game.tileY);
    }

    private void handleSingleSelectMode() {
        // Delete selected building
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            selection.deleteSelected();
            game.mode = GameMode.NORMAL;
            return;
        }

        // Handle continuous slider dragging if mouse is held down
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (overlay.isDraggingSlider()) {
                overlay.updateActiveDrag(Gdx.input.getX());
                return; // Consume input so we don't accidentally select something else
            }
        } else {
            // Mouse is released, stop dragging
            overlay.stopDragging();
        }

        // Handle initial click-down events
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            int mx = Gdx.input.getX();
            int my = Gdx.input.getY();

            // Check if they clicked the core sliders/sell button
            OverlayRenderer.UIResult ui = overlay.hitTest(mx, my);
            if (ui.type() != OverlayRenderer.UIResultType.NONE) {
                overlay.handleUIAction(ui);
                return; // Click was handled by UI
            }

            // Clicked outside UI: select another building or clear selection
            selection.trySelectAt(game.tileX, game.tileY);
            if (selection.getSelected() == null) {
                game.mode = GameMode.NORMAL;
            }
        }

        // Deselect single selection on Right Click
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            selection.clearSelection();
            game.mode = GameMode.NORMAL;
        }
    }

    private void handleDeleteMode() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            selection.startDrag(game.tileX, game.tileY);
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            selection.updateDrag(game.tileX, game.tileY);
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            selection.endDrag();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            selection.deleteMulti();
            game.mode = GameMode.NORMAL;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            selection.clearSelection();
            game.mode = GameMode.NORMAL;
        }
    }

    private void handlePlacementMode() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.mode = GameMode.NORMAL;
        }
    }
}
