package dev.lmcginnisno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
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

    // --- Core panel layout constants ---
    // Rows are always drawn for every ItemType (fixed count, fixed order),
    // never derived from the inventory HashMap's entrySet(). That keeps the
    // panel height stable and guarantees draw order and hit-test order can
    // never disagree about which row belongs to which resource.
    private static final int PANEL_WIDTH = 260;
    private static final int ROW_SPACING = 38;
    private static final int SLIDER_WIDTH = 100;
    private static final int SLIDER_HEIGHT = 6;
    private static final int SLIDER_X_OFFSET = 24;   // relative to panel x, matches drawn trackX
    private static final int TRACK_Y_OFFSET = 24;    // below each row's text baseline
    private static final int FIRST_ROW_Y_OFFSET = 30; // below panel title baseline
    private static final int BUTTON_SPACING = 32;
    private static final int BUTTON_HEIGHT = 26;

    // Panel origin in screen space, recomputed each draw from the Core's
    // world position. Both drawCoreInventory and hitTest read this same
    // pair of fields, so they can never disagree about where the panel is.
    private int panelX, panelY;

    private int sellButtonX, sellButtonY, sellButtonW, sellButtonH;
    private int sellAllButtonX, sellAllButtonY, sellAllButtonW, sellAllButtonH;

    private ItemType draggingSliderItem = null;

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

    // ---------------------------------------------------------------
    // Core inventory / sell panel
    // ---------------------------------------------------------------

    /** Slider track rectangle (screen space) for the given item's fixed row. */
    private Rectangle getSliderRect(ItemType type) {
        int rowIndex = type.ordinal();
        int drawY = panelY - FIRST_ROW_Y_OFFSET - rowIndex * ROW_SPACING;
        int trackY = drawY - TRACK_Y_OFFSET;
        return new Rectangle(panelX + SLIDER_X_OFFSET, trackY, SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    private void drawCoreInventory(SpriteBatch batch, Core core) {
        float worldX = (core.x + core.width  / 2f) * TileEngine.TILE_SIZE;
        float worldY = (core.y + core.height / 2f) * TileEngine.TILE_SIZE;

        Vector3 screen = game.engine.getCamera().project(new Vector3(worldX, worldY, 0));

        panelX = (int) screen.x + 40;
        panelY = (int) screen.y + 40;

        ItemType[] types = ItemType.values();
        int panelHeight = (types.length * ROW_SPACING) + 115;

        int panelLeft = panelX - 10;
        int panelBottom = panelY - panelHeight + 10;

        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(Assets.whitePixel, panelLeft, panelBottom, PANEL_WIDTH, panelHeight);
        batch.setColor(1f, 1f, 1f, 1.0f);

        font.draw(batch, "CORE STORAGE", panelX, panelY);

        int totalExpectedPayout = 0;
        int totalItemsInCore = 0;
        int totalPossibleEarnings = 0;
        int lastDrawY = panelY - FIRST_ROW_Y_OFFSET;

        for (ItemType type : types) {
            int amount = core.getInventory().getOrDefault(type, 0);

            totalItemsInCore += amount;
            totalPossibleEarnings += (amount * core.getPriceForType(type));

            int sellAmount = Math.min(core.getSaleQuantity(type), amount);
            int keepAmount = amount - sellAmount;
            int unitPrice = core.getPriceForType(type);
            int expectedPayout = sellAmount * unitPrice;
            totalExpectedPayout += expectedPayout;

            float percentage = amount > 0 ? (float) sellAmount / amount : 0f;

            int rowIndex = type.ordinal();
            int drawY = panelY - FIRST_ROW_Y_OFFSET - rowIndex * ROW_SPACING;
            lastDrawY = drawY;

            TextureRegion icon = game.engine.getRegion(type.row, type.col);
            batch.draw(icon, panelX, drawY - 16, 16, 16);

            String text = type.name + ": " + keepAmount + " kept";
            font.draw(batch, text, panelX + 24, drawY);

            Rectangle track = getSliderRect(type);
            batch.setColor(0.3f, 0.3f, 0.3f, 1f);
            batch.draw(Assets.whitePixel, track.x, track.y, track.width, track.height);

            int handleSize = 10;
            float handleX = track.x + (percentage * (track.width - handleSize));
            float handleY = track.y - (handleSize / 2f) + (track.height / 2f);
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(Assets.whitePixel, handleX, handleY, handleSize, handleSize);

            String visualFeedback;
            if (sellAmount > 0) {
                int displayPercent = Math.round(percentage * 100);
                visualFeedback = displayPercent + "% (" + sellAmount + " = " + expectedPayout + "c)";
            } else {
                visualFeedback = "0%";
            }
            font.draw(batch, visualFeedback, track.x + track.width + 10, track.y + 8);
        }

        // --- BUTTON 1: CONFIRM TRANSACTION (Slider Selection) ---
        this.sellButtonX = panelX + 10;
        this.sellButtonY = lastDrawY - 20 - BUTTON_SPACING; // one row below the last item row
        this.sellButtonW = PANEL_WIDTH - 40;
        this.sellButtonH = BUTTON_HEIGHT;

        batch.setColor(totalExpectedPayout > 0 ? 0.1f : 0.4f, totalExpectedPayout > 0 ? 0.7f : 0.4f, totalExpectedPayout > 0 ? 0.3f : 0.4f, 1f);
        batch.draw(Assets.whitePixel, sellButtonX, sellButtonY, sellButtonW, sellButtonH);
        batch.setColor(1f, 1f, 1f, 1f);

        String buttonText = "SELL SELECTED (" + totalExpectedPayout + "c)";
        font.draw(batch, buttonText, sellButtonX + 12, sellButtonY + 18);

        // --- BUTTON 2: SELL ALL (100% of Stored Inventory) ---
        this.sellAllButtonX = panelX + 10;
        this.sellAllButtonY = sellButtonY - BUTTON_SPACING;
        this.sellAllButtonW = PANEL_WIDTH - 40;
        this.sellAllButtonH = BUTTON_HEIGHT;

        batch.setColor(totalItemsInCore > 0 ? 0.85f : 0.4f, totalItemsInCore > 0 ? 0.65f : 0.4f, totalItemsInCore > 0 ? 0.1f : 0.4f, 1f);
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

        int posX = (int) (hudWidth - counterWidth - 16);
        int posY = (int) (hudHeight - counterHeight - 16);

        batch.setColor(0f, 0f, 0f, 0.7f);
        batch.draw(Assets.whitePixel, posX, posY, counterWidth, counterHeight);
        batch.setColor(1f, 1f, 1f, 1f);

        font.setColor(1f, 0.85f, 0.2f, 1f);
        font.draw(batch, "CREDITS: " + game.credits + "c", posX + 12, posY + 20);
        font.setColor(1f, 1f, 1f, 1f);
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

    public record UIResult(
        UIResultType type,
        BuildCategory category,
        BuildEntry entry,
        ItemType sliderItem,
        float sliderValue
    ) {
        public static UIResult none() {
            return new UIResult(UIResultType.NONE, null, null, null, 0f);
        }
    }

    public UIResult hitTest(int sx, int sy) {
        int sidebarWidth = 120;

        int hudSy = (int)(game.hudCamera.viewportHeight - sy);

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

        Building sel = selection.getSelected();
        if (sel instanceof Core core) {
            int rawMouseY = hudSy;

            if (sx >= sellButtonX && sx <= sellButtonX + sellButtonW &&
                rawMouseY >= sellButtonY && rawMouseY <= sellButtonY + sellButtonH) {
                return new UIResult(UIResultType.CORE_SELL_CLICK, null, null, null, 0f);
            }

            if (sx >= sellAllButtonX && sx <= sellAllButtonX + sellAllButtonW &&
                rawMouseY >= sellAllButtonY && rawMouseY <= sellAllButtonY + sellAllButtonH) {
                return new UIResult(UIResultType.CORE_SELL_ALL_CLICK, null, null, null, 0f);
            }

            for (ItemType type : ItemType.values()) {
                Rectangle track = getSliderRect(type);

                // Slightly generous vertical hitbox around the thin visual track.
                float hitTop = track.y + track.height + 8;
                float hitBottom = track.y - 8;

                if (sx >= track.x && sx <= track.x + track.width &&
                    rawMouseY >= hitBottom && rawMouseY <= hitTop) {

                    float relativeX = (sx - track.x) / track.width;
                    return new UIResult(UIResultType.CORE_SLIDER_DRAG, null, null, type, relativeX);
                }
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
                    draggingSliderItem = result.sliderItem;
                    core.setSalePercentage(result.sliderItem, result.sliderValue);
                }
            }
            case CORE_SELL_CLICK -> {
                Building sel = selection.getSelected();
                if (sel instanceof Core core) {
                    int payout = core.sellSelectedItems();
                    if (payout > 0) {
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

    public void updateActiveDrag(int sx) {
        if (draggingSliderItem == null) return;
        Building sel = selection.getSelected();
        if (sel instanceof Core core) {
            Rectangle track = getSliderRect(draggingSliderItem);
            float relativeX = (sx - track.x) / track.width;
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
