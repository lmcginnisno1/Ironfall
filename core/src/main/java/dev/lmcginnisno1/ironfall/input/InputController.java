package dev.lmcginnisno1.ironfall.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.lmcginnisno1.ironfall.game.GameMode;
import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.selection.SelectionManager;

public class InputController {

    private final IronfallGame game;
    private final SelectionManager selection;

    public InputController(IronfallGame game, SelectionManager selection) {
        this.game = game;
        this.selection = selection;
    }

    public void update() {
        // Always update tile coords
        game.screenToTile(Gdx.input.getX(), Gdx.input.getY());

        switch (game.mode) {

            case NORMAL -> handleNormalMode();
            case SELECTING_SINGLE -> handleSingleSelectMode();
            case DELETE_MODE -> handleDeleteMode();
            case PLACING_MINER, PLACING_CORE, PLACING_CONVEYOR -> handlePlacementMode();
        }
    }

    private void handleNormalMode() {
        // Enter miner placement
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.mode = GameMode.PLACING_MINER;
            return;
        }

        // Enter conveyor placement
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            game.mode = GameMode.PLACING_CONVEYOR;
            return;
        }

        // Enter delete mode
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            game.mode = GameMode.DELETE_MODE;
            selection.clearSelection();
            return;
        }

        // Toggle grid
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            game.showGrid = !game.showGrid;
        }

        // Try selecting a building
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            selection.trySelectAt(game.tileX, game.tileY);
        }
    }

    private void handleSingleSelectMode() {
        // Delete selected
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            selection.deleteSelected();
            game.mode = GameMode.NORMAL;
            return;
        }

        // Cancel selection
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            selection.clearSelection();
            game.mode = GameMode.NORMAL;
        }
    }

    private void handleDeleteMode() {
        // Start drag-select
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            selection.startDrag(game.tileX, game.tileY);
        }

        // Update drag-select
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            selection.updateDrag(game.tileX, game.tileY);
        }

        // End drag-select
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            selection.endDrag();
        }

        // Delete all selected
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            selection.deleteMulti();
            game.mode = GameMode.NORMAL;
            return;
        }

        // Cancel
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            selection.clearSelection();
            game.mode = GameMode.NORMAL;
        }
    }

    private void handlePlacementMode() {
        // Right-click cancels any placement mode
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            game.mode = GameMode.NORMAL;
        }
    }
}
