package dev.lmcginninsno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Core extends Building {

    public Core(int x, int y, TextureRegion sprite) {
        super(x, y, 4, 4, sprite);
    }

    @Override
    public void update(float delta) {
        // Later: accept items, store resources, power radius, etc.
    }
}
