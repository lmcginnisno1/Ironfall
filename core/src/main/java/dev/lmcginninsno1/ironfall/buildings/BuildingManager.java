package dev.lmcginninsno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;

public class BuildingManager {

    // List for update/render order
    private final ArrayList<Building> buildings = new ArrayList<>();

    // Fast tile lookup: (x,y) → Building
    private final HashMap<Long, Building> grid = new HashMap<>();

    // Packs x,y into a single long key
    private long key(int x, int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    public boolean canPlace(int x, int y, int w, int h) {
        for (int ix = x; ix < x + w; ix++) {
            for (int iy = y; iy < y + h; iy++) {
                if (grid.containsKey(key(ix, iy))) return false;
            }
        }
        return true;
    }

    public void place(Building b) {
        if (!canPlace(b.x, b.y, b.width, b.height)) return;

        // Add to list
        buildings.add(b);

        // Register each tile in the footprint
        for (int ix = b.x; ix < b.x + b.width; ix++) {
            for (int iy = b.y; iy < b.y + b.height; iy++) {
                grid.put(key(ix, iy), b);
            }
        }
    }

    public Building getAt(int x, int y) {
        return grid.get(key(x, y));
    }

    public void remove(Building b) {
        // Prevent removing the Core
        if (b instanceof Core) {
            return;
        }

        buildings.remove(b);

        for (int ix = b.x; ix < b.x + b.width; ix++) {
            for (int iy = b.y; iy < b.y + b.height; iy++) {
                grid.remove(key(ix, iy));
            }
        }
    }

    public void update(float delta) {
        for (Building b : buildings) b.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (Building b : buildings) b.render(batch);
    }

    public void getAllInRect(Rectangle rect, Array<Building> out) {
        for (Building b : buildings) {
            if (rect.contains(b.x, b.y)) {
                out.add(b);
            }
        }
    }

    public ArrayList<Building> getBuildings() {
        return buildings;
    }
}
