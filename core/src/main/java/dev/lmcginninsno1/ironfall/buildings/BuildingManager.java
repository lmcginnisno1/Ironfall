package dev.lmcginninsno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;

public class BuildingManager {
    private final ArrayList<Building> buildings = new ArrayList<>();

    public void add(Building b) {
        buildings.add(b);
    }

    public void update(float delta) {
        for (Building b : buildings) b.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (Building b : buildings) b.render(batch);
    }

    public boolean isOccupied(int x, int y, int w, int h) {
        for (Building b : buildings) {
            // Check overlap between the new footprint and existing building footprint
            if (x < b.x + b.width &&
                x + w > b.x &&
                y < b.y + b.height &&
                y + h > b.y) {
                return true;
            }
        }
        return false;
    }
}
