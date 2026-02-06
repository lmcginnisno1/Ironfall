package dev.lmcginninsno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import dev.lmcginninsno1.ironfall.game.GameMode;
import dev.lmcginninsno1.ironfall.IronfallGame;
import dev.lmcginninsno1.ironfall.buildings.Core;
import dev.lmcginninsno1.ironfall.selection.SelectionManager;
import dev.lmcginninsno1.ironfall.buildings.Building;

import static dev.lmcginninsno1.ironfall.render.TextUtil.drawOutlined;

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
        float x = 10;
        float y = game.screenHeight - 10;

        drawOutlined(font, batch, "Mode: " + game.mode, x, y);
        y -= 20;

        drawOutlined(font, batch, "Tile: (" + game.tileX + ", " + game.tileY + ")", x, y);
        y -= 20;

        Building sel = selection.getSelected();
        if (sel != null) {
            drawOutlined(font, batch, "Selected: " + sel.getClass().getSimpleName(), x, y);
            y -= 20;
        }

        int multiCount = selection.getMultiSelection().size;
        if (multiCount > 0) {
            drawOutlined(font, batch, "Selected (multi): " + multiCount, x, y);
            y -= 20;
        }

        if (game.mode == GameMode.SELECTING_SINGLE && sel instanceof Core core) {
            drawOutlined(font, batch, core.getInventoryString(), x, y);
        }
    }
}
