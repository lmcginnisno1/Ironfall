package dev.lmcginninsno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Building {

    public final int x;      // tile position
    public final int y;

    public final int width;  // footprint in tiles
    public final int height;

    protected TextureRegion sprite;
    protected BuildingManager world;

    public Building(int x, int y, int width, int height, TextureRegion sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
    }

    public void update(float delta) {}

    public void render(SpriteBatch batch) {
        batch.draw(sprite, x * 16, y * 16, width * 16, height * 16);
    }

    public void setWorld(BuildingManager world) {
        this.world = world;
    }
}
