package dev.lmcginninsno1.ironfall.buildings;

import dev.lmcginninsno1.ironfall.Assets;

public class Core extends Building {

    public Core(int x, int y) {
        super(x, y, 4, 4, Assets.core);
    }

    @Override
    public void update(float delta) {
        // Later: accept items, store resources, power radius, etc.
    }
}
