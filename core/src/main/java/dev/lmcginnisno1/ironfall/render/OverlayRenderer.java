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

    private int coreMenuX, coreMenuY, coreMenuWidth, coreMenuHeight;
    private int sellButtonX, sellButtonY, sellButtonW, sellButtonH;
    private ItemType draggingSliderItem = null;
    private int sellAllButtonX, sellAllButtonY, sellAllButtonW, sellAllButtonH;

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

        drawHUD(batch);

        batch.setProjectionMatrix(game.engine.getCamera().combined);
    }

    private void drawCoreInventory(SpriteBatch batch, Core core) {
        // Locate Core position on screen
        float worldX = (core.x + core.width  / 2f) * TileEngine.TILE_SIZE;
        float worldY = (core.y + core.height / 2f) * TileEngine.TILE_SIZE;

        Vector3 screen = game.engine.getCamera().project(new Vector3(worldX, worldY, 0));

        int panelWidth = 260;
        Map<ItemType, Integer> inv = core.getInventory();

        // Increased vertical space (+115 instead of +80) to accommodate the extra button smoothly!
        int panelHeight = (inv.size() * 38) + 115;

        int x = (int)screen.x + 40;
        int y = (int)screen.y + 40;

        // Save menu boundaries for mouse hit-testing
        this.coreMenuX = x - 10;
        this.coreMenuY = y - panelHeight + 10;
        this.coreMenuWidth = panelWidth;
        this.coreMenuHeight = panelHeight;

        // Draw Panel Background
        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(Assets.whitePixel, coreMenuX, coreMenuY, coreMenuWidth, coreMenuHeight);
        batch.setColor(1f, 1f, 1f, 1.0f);

        // Render Title
        font.draw(batch, "CORE STORAGE", x, y);

        int drawY = y - 30;
        int sliderWidth = 100;
        int sliderHeight = 6;

        int totalExpectedPayout = 0;
        int totalItemsInCore = 0;
        int totalPossibleEarnings = 0;

        for (var entry : inv.entrySet()) {
            ItemType type = entry.getKey();
            int amount = entry.getValue();

            // Compute "Sell All" stats for the whole inventory
            totalItemsInCore += amount;
            totalPossibleEarnings += (amount * core.getPriceForType(type));

            // Slider specific math
            int sellAmount = core.getSaleQuantity(type);
            sellAmount = Math.min(sellAmount, amount);

            int keepAmount = amount - sellAmount;
            int unitPrice = core.getPriceForType(type);
            int expectedPayout = sellAmount * unitPrice;
            totalExpectedPayout += expectedPayout;

            float percentage = amount > 0 ? (float) sellAmount / amount : 0f;

            // 1. Draw Item Icon
            TextureRegion icon = game.engine.getRegion(type.row, type.col);
            batch.draw(icon, x, drawY - 16, 16, 16);

            // 2. Display Keep Amount vs. Dragged Sell Amount
            String text = type.name + ": " + keepAmount + " kept";
            font.draw(batch, text, x + 24, drawY);

            // 3. Draw Slider Track (Grey background bar)
            int trackX = x + 24;
            int trackY = drawY - 24;
            batch.setColor(0.3f, 0.3f, 0.3f, 1f);
            batch.draw(Assets.whitePixel, trackX, trackY, sliderWidth, sliderHeight);

            // 4. Draw Slider Handle (White handle shifted by percentage)
            int handleSize = 10;
            int handleX = (int) (trackX + (percentage * (sliderWidth - handleSize)));
            int handleY = trackY - (handleSize / 2) + (sliderHeight / 2);
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(Assets.whitePixel, handleX, handleY, handleSize, handleSize);

            // 5. Rich Info Row
            String visualFeedback;
            if (sellAmount > 0) {
                int displayPercent = Math.round(percentage * 100);
                visualFeedback = displayPercent + "% (" + sellAmount + " = " + expectedPayout + "c)";
            } else {
                visualFeedback = "0%";
            }
            font.draw(batch, visualFeedback, trackX + sliderWidth + 10, trackY + 8);

            drawY -= 38; // Move down for next row
        }

        // --- BUTTON 1: CONFIRM TRANSACTION (Slider Selection) ---
        this.sellButtonX = x + 10;
        this.sellButtonY = drawY - 20;
        this.sellButtonW = panelWidth - 40;
        this.sellButtonH = 26;

        if (totalExpectedPayout > 0) {
            batch.setColor(0.1f, 0.7f, 0.3f, 1f); // Active Green
        } else {
            batch.setColor(0.4f, 0.4f, 0.4f, 1f); // Disabled Grey
        }
        batch.draw(Assets.whitePixel, sellButtonX, sellButtonY, sellButtonW, sellButtonH);
        batch.setColor(1f, 1f, 1f, 1f);

        String buttonText = totalExpectedPayout > 0
            ? "SELL SELECTED (" + totalExpectedPayout + "c)"
            : "SELL SELECTED (0c)";
        font.draw(batch, buttonText, sellButtonX + 12, sellButtonY + 18);

        // --- BUTTON 2: SELL ALL (100% of Stored Inventory) ---
        this.sellAllButtonX = x + 10;
        this.sellAllButtonY = sellButtonY - 32; // Stacked directly below Button 1
        this.sellAllButtonW = panelWidth - 40;
        this.sellAllButtonH = 26;

        if (totalItemsInCore > 0) {
            batch.setColor(0.85f, 0.65f, 0.1f, 1f); // Active Gold
        } else {
            batch.setColor(0.4f, 0.4f, 0.4f, 1f); // Disabled Grey
        }
        batch.draw(Assets.whitePixel, sellAllButtonX, sellAllButtonY, sellAllButtonW, sellAllButtonH);
        batch.setColor(1f, 1f, 1f, 1f);

        String sellAllText = totalItemsInCore > 0
            ? "SELL ALL " + totalItemsInCore + " (+" + totalPossibleEarnings + "c)"
            : "SELL ALL (EMPTY)";
        font.draw(batch, sellAllText, sellAllButtonX + 12, sellAllButtonY + 18);
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

    private void drawHUD(SpriteBatch batch) {
        float hudWidth = game.hudCamera.viewportWidth;
        float hudHeight = game.hudCamera.viewportHeight;

        int counterWidth = 130;
        int counterHeight = 30;

        // Position it nicely in the top-right corner, offsetting from screen edges
        int posX = (int) (hudWidth - counterWidth - 16);
        int posY = (int) (hudHeight - counterHeight - 16);

        // 1. Draw a semi-transparent dark background block (Matches sidebar panel style)
        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(Assets.whitePixel, posX, posY, counterWidth, counterHeight);
        batch.setColor(1f, 1f, 1f, 1f); // Reset batch color

        // 2. Draw a bright gold currency text indicator inside the box
        font.setColor(1f, 0.85f, 0.2f, 1f); // Gold/Yellow color
        font.draw(batch, "CREDITS: " + game.credits + "c", posX + 12, posY + 20);
        font.setColor(1f, 1f, 1f, 1f); // Reset font color back to default white
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
        ENTRY_CLICK,
        CORE_SLIDER_DRAG,
        CORE_SELL_CLICK,
        CORE_SELL_ALL_CLICK
    }

    // Expanded record to hold custom slider details
    public record UIResult(
        UIResultType type,
        BuildCategory category,
        BuildEntry entry,
        ItemType sliderItem, // Which item's slider is dragged
        float sliderValue    // New drag value
    ) {
        public static UIResult none() {
            return new UIResult(UIResultType.NONE, null, null, null, 0f);
        }
    }

    public UIResult hitTest(int sx, int sy) {
        // Matches the updated sidebar width
        int sidebarWidth = 120;

        // Convert screen Y → HUD Y (since Gdx is Y-down but rendering is Y-up)
        int hudSy = (int)(game.hudCamera.viewportHeight - sy);

        // 1. Sidebar checks (Your original logic)
        if (sx <= sidebarWidth) {
            float hudH = game.hudCamera.viewportHeight;
            int buttonY = (int)(hudH - 60);

            for (BuildCategory cat : BuildCategory.values()) {
                if (hudSy > buttonY - 40 && hudSy < buttonY) {
                    return new UIResult(UIResultType.CATEGORY_CLICK, cat, null, null, 0f);
                }

                if (cat == openCategory) {
                    int entryY = buttonY - 40;
                    for (BuildEntry entry : entries.get(openCategory)) {
                        if (hudSy > entryY - 32 && hudSy < entryY) {
                            return new UIResult(UIResultType.ENTRY_CLICK, openCategory, entry, null, 0f);
                        }
                        entryY -= 40;
                    }
                }
                buttonY -= 80;
            }
            return UIResult.none();
        }

        // 2. Core Menu checks
        Building sel = selection.getSelected();
        if (sel instanceof Core core) {
            int rawMouseY = (int)(game.hudCamera.viewportHeight - sy);

            // A. Check "SELL SELECTED" (Confirm Transaction) Button
            if (sx >= sellButtonX && sx <= sellButtonX + sellButtonW &&
                rawMouseY >= sellButtonY && rawMouseY <= sellButtonY + sellButtonH) {
                return new UIResult(UIResultType.CORE_SELL_CLICK, null, null, null, 0f);
            }

            // B. Check "SELL ALL" Button
            if (sx >= sellAllButtonX && sx <= sellAllButtonX + sellAllButtonW &&
                rawMouseY >= sellAllButtonY && rawMouseY <= sellAllButtonY + sellAllButtonH) {
                return new UIResult(UIResultType.CORE_SELL_ALL_CLICK, null, null, null, 0f);
            }

            // C. Check if starting a drag on any item slider
            int sliderXStart = coreMenuX + 24; // Matches rendering trackX
            int sliderWidth = 100;

            int checkY = coreMenuY + coreMenuHeight - 40; // Starts from top down
            for (var entry : core.getInventory().entrySet()) {
                ItemType type = entry.getKey();

                // Define bounding box for slider track row
                int sliderYBottom = checkY - 28;
                int sliderYTop = checkY - 14;

                if (sx >= sliderXStart && sx <= sliderXStart + sliderWidth &&
                    rawMouseY >= sliderYBottom && rawMouseY <= sliderYTop) {

                    float relativeX = (float)(sx - sliderXStart) / sliderWidth;
                    return new UIResult(UIResultType.CORE_SLIDER_DRAG, null, null, type, relativeX);
                }
                checkY -= 38;
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
            case CORE_SLIDER_DRAG -> {
                Building sel = selection.getSelected();
                if (sel instanceof Core core) {
                    // Set both the active drag item and value
                    draggingSliderItem = result.sliderItem;
                    core.setSalePercentage(result.sliderItem, result.sliderValue);
                }
            }
            case CORE_SELL_CLICK -> {
                Building sel = selection.getSelected();
                if (sel instanceof Core core) {
                    int payout = core.sellSelectedItems();
                    if (payout > 0) {
                        // Deposit the money directly into your main game wallet!
                        game.credits += payout;
                        System.out.println("Sold materials! Added " + payout + "c. Wallet: " + game.credits + "c");
                    }
                }
            }
            case CORE_SELL_ALL_CLICK -> {
                Building sel = selection.getSelected();
                if (sel instanceof Core core) {
                    int payout = core.sellAllItems();
                    if (payout > 0) {
                        game.credits += payout;
                        System.out.println("Sold ALL stored materials! Added " + payout + "c. Wallet: " + game.credits + "c");
                    }
                }
            }
        }
    }

    // Helper method to update a slider value when mouse is held down
    public void updateActiveDrag(int sx) {
        if (draggingSliderItem == null) return;
        Building sel = selection.getSelected();
        if (sel instanceof Core core) {
            int sliderXStart = coreMenuX + 24;
            int sliderWidth = 100;
            float relativeX = (float)(sx - sliderXStart) / sliderWidth;
            core.setSalePercentage(draggingSliderItem, relativeX);
        }
    }

    public void stopDragging() {
        draggingSliderItem = null;
    }

    public boolean isDraggingSlider() {
        return draggingSliderItem != null;
    }
}
