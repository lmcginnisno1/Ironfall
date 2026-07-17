package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
}
