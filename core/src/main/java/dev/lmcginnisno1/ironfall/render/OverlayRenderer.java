package dev.lmcginnisno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.buildings.*;
import dev.lmcginnisno1.ironfall.items.ItemType;
import dev.lmcginnisno1.ironfall.selection.SelectionManager;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;

import java.util.List;
import java.util.Map;

public class OverlayRenderer {

    private final IronfallGame game;
    private final SelectionManager selection;
    private final BitmapFont font;

    private BuildCategory openCategory = null;

    private final Map<BuildCategory, List<BuildEntry>> entries;

    public OverlayRenderer(IronfallGame game, SelectionManager selection, BitmapFont font) {
        this.game = game;
        this.selection = selection;
        this.font = font;

        this.entries = Map.of(
            BuildCategory.MINING, List.of(
                new BuildEntry("Basic Miner", Assets.basicMiner, () -> new BasicMiner(0,0, game.engine))
            ),
            BuildCategory.TRANSPORT, List.of(
                new BuildEntry("Conveyor", Assets.conveyorUp, () -> new Conveyor(0,0, Conveyor.Direction.UP))
            )
        );
    }

    public void render(SpriteBatch batch) {

        batch.setProjectionMatrix(game.hudCamera.combined);

        drawSidebar(batch);

        Building sel = selection.getSelected();
        if (sel instanceof Core core) {
            drawCoreInventory(batch, core);
        }

        batch.setProjectionMatrix(game.engine.getCamera().combined);
    }

    private void drawCoreInventory(SpriteBatch batch, Core core) {

        float worldX = (core.x + core.width  / 2f) * TileEngine.TILE_SIZE;
        float worldY = (core.y + core.height / 2f) * TileEngine.TILE_SIZE;

        Vector3 screen = game.engine.getCamera().project(new Vector3(worldX, worldY, 0));

        int x = (int)screen.x + 40;
        int y = (int)screen.y + 40;

        Map<ItemType, Integer> inv = core.getInventory();

        int panelWidth = 160;
        int panelHeight = inv.size() * 28 + 20;

        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(Assets.whitePixel, x - 10, y - panelHeight + 10, panelWidth, panelHeight);
        batch.setColor(1f, 1f, 1f, 1f);

        int drawY = y;

        for (var entry : inv.entrySet()) {
            ItemType type = entry.getKey();
            int amount = entry.getValue();

            TextureRegion icon = game.engine.getRegion(type.row, type.col);

            batch.draw(icon, x, drawY - 16, 16, 16);
            font.draw(batch, type.name + ": " + amount, x + 24, drawY);

            drawY -= 28;
        }
    }

    private void drawSidebar(SpriteBatch batch) {
        int panelWidth = 120;
        int x = 0;
        int y = game.screenHeight;

        batch.setColor(0f,0f,0f,0.6f);
        batch.draw(Assets.whitePixel, x, 0, panelWidth, game.screenHeight);
        batch.setColor(1f,1f,1f,1f);

        int buttonY = y - 60;

        for (BuildCategory cat : BuildCategory.values()) {
            boolean open = (cat == openCategory);

            font.draw(batch, cat.name(), x + 20, buttonY);

            if (open) drawCategoryContents(batch, cat, x + 10, buttonY - 40);

            buttonY -= 80;
        }
    }

    private void drawCategoryContents(SpriteBatch batch, BuildCategory cat, int x, int startY) {
        int y = startY;

        for (BuildEntry entry : entries.get(cat)) {
            batch.draw(entry.icon, x, y - 32, 32, 32);
            font.draw(batch, entry.name, x + 40, y - 8);
            y -= 40;
        }
    }

    public enum UIResultType {
        NONE,
        CATEGORY_CLICK,
        ENTRY_CLICK
    }

    public record UIResult(UIResultType type, BuildCategory category, BuildEntry entry) {
        public static UIResult none() {
                return new UIResult(UIResultType.NONE, null, null);
            }
        }

    public UIResult hitTest(int sx, int sy) {
        int panelWidth = 120;

        // Convert screen Y → HUD Y
        sy = (int)(game.hudCamera.viewportHeight - sy);

        if (sx > panelWidth) return UIResult.none();

        float hudH = game.hudCamera.viewportHeight;

        int buttonY = (int)(hudH - 60);

        for (BuildCategory cat : BuildCategory.values()) {
            if (sy > buttonY - 40 && sy < buttonY) {
                return new UIResult(UIResultType.CATEGORY_CLICK, cat, null);
            }
            buttonY -= 80;
        }

        if (openCategory != null) {
            int entryY = (int)(hudH - 100);

            for (BuildEntry entry : entries.get(openCategory)) {
                if (sy > entryY - 32 && sy < entryY) {
                    return new UIResult(UIResultType.ENTRY_CLICK, openCategory, entry);
                }
                entryY -= 40;
            }
        }

        return UIResult.none();
    }

    public void handleUIAction(UIResult result) {
        switch (result.type) {
            case CATEGORY_CLICK -> {
                if (openCategory == result.category) openCategory = null;
                else openCategory = result.category;
            }
            case ENTRY_CLICK -> {
                game.placementController.setPrototype(result.entry.factory.get());
            }
        }
    }
}
