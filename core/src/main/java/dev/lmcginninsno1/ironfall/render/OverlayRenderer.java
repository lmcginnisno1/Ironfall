package dev.lmcginninsno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Color;
import dev.lmcginninsno1.ironfall.IronfallGame;
import dev.lmcginninsno1.ironfall.selection.SelectionManager;
import dev.lmcginninsno1.ironfall.buildings.Building;

public class OverlayRenderer {

    private final IronfallGame game;
    private final SelectionManager selection;
    private final BitmapFont font;

    public OverlayRenderer(IronfallGame game, SelectionManager selection, BitmapFont font) {
        this.game = game;
        this.selection = selection;
        this.font = font;
    }

    public void render(SpriteBatch batch) {
        batch.setColor(Color.WHITE);

        float x = 10;
        float y = game.screenHeight - 10;

        font.draw(batch, "Mode: " + game.mode, x, y);
        y -= 20;

        font.draw(batch, "Tile: (" + game.tileX + ", " + game.tileY + ")", x, y);
        y -= 20;

        Building sel = selection.getSelected();
        if (sel != null) {
            font.draw(batch, "Selected: " + sel.getClass().getSimpleName(), x, y);
            y -= 20;
        }

        int multiCount = selection.getMultiSelection().size;
        if (multiCount > 0) {
            font.draw(batch, "Selected (multi): " + multiCount, x, y);
        }
    }
}
