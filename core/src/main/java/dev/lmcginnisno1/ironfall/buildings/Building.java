package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.lmcginnisno1.ironfall.items.Item;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class Building {

    public final int x;      // tile position
    public final int y;

    public final int width;  // footprint in tiles
    public final int height;

    // Credit cost to place one instance of this building (per-tile for
    // Conveyor, since it's placed in multi-tile drags). Used both to charge
    // on placement and to determine the refund when it's deleted.
    public final int cost;

    protected TextureRegion sprite;
    protected BuildingManager world;

    public Building(int x, int y, int width, int height, TextureRegion sprite, int cost) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
        this.cost = cost;
    }

    public void update(float delta) {}

    public void render(SpriteBatch batch) {
        batch.draw(sprite, x * 16, y * 16, width * 16, height * 16);
    }

    public void setWorld(BuildingManager world) {
        this.world = world;
    }

    public TextureRegion getSprite() {
        return sprite;
    }

    public abstract Building copyAt(int x, int y);

    // --- Item transfer ---

    /**
     * Tries to hand an item to whatever building occupies tile (tx, ty).
     * The sender tile passed on is the tile of this building closest to
     * (tx, ty), which is the touching tile when (tx, ty) is a direct neighbour.
     */
    protected boolean pushItem(Item item, int tx, int ty) {
        Building target = world.getAt(tx, ty);
        if (target == null || target == this || !(target instanceof ItemAcceptor acceptor)) return false;

        int fromX = Math.max(x, Math.min(tx, x + width - 1));
        int fromY = Math.max(y, Math.min(ty, y + height - 1));
        return acceptor.acceptItem(item, fromX, fromY);
    }

    /** Offers an item to each tile touching an edge of this building (no diagonals) until one takes it. */
    protected boolean pushItemToAnyNeighbour(Item item) {
        for (int tx = x; tx < x + width; tx++) {
            if (pushItem(item, tx, y - 1) || pushItem(item, tx, y + height)) return true;
        }
        for (int ty = y; ty < y + height; ty++) {
            if (pushItem(item, x - 1, ty) || pushItem(item, x + width, ty)) return true;
        }
        return false;
    }

    // --- Water transfer ---

    /** Offers water to the building at (tx, ty). Returns how much it took. */
    protected float pushWater(float amount, int tx, int ty) {
        if (amount <= 0f) return 0f;
        Building target = world.getAt(tx, ty);
        if (target == null || target == this || !(target instanceof WaterAcceptor acceptor)) return 0f;

        int fromX = Math.max(x, Math.min(tx, x + width - 1));
        int fromY = Math.max(y, Math.min(ty, y + height - 1));
        return acceptor.acceptWater(amount, fromX, fromY);
    }

    /** Offers water around every edge until it's all taken. Returns the total taken. */
    protected float pushWaterToNeighbours(float amount) {
        float left = amount;
        for (int tx = x; tx < x + width && left > 0f; tx++) {
            left -= pushWater(left, tx, y - 1);
            left -= pushWater(left, tx, y + height);
        }
        for (int ty = y; ty < y + height && left > 0f; ty++) {
            left -= pushWater(left, x - 1, ty);
            left -= pushWater(left, x + width, ty);
        }
        return amount - left;
    }

    // --- Placement and UI ---

    /** True for buildings that must sit on at least one tile of a single ore type (miners). */
    public boolean requiresOre() {
        return false;
    }

    /** True for buildings that must cover at least one water tile (pumps). Everything else can't touch water. */
    public boolean requiresWater() {
        return false;
    }

    /** Status lines shown in a small panel when this building is selected. Empty means no panel. */
    public List<String> infoLines() {
        return List.of();
    }

    // --- Saving ---

    public abstract BuildingType type();

    /** Direction this building faces, or null if it doesn't rotate. Saved alongside the position. */
    public Conveyor.Direction facing() {
        return null;
    }

    /** Writes runtime state (buffers, timers, items in transit). Position and facing are saved separately. */
    public void writeState(DataOutputStream out) throws IOException {}

    /** Reads back exactly what {@link #writeState} wrote. */
    public void readState(DataInputStream in) throws IOException {}
}
