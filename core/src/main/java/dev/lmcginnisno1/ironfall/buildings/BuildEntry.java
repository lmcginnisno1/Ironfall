package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.function.Supplier;

public class BuildEntry {
    public final String name;
    public final TextureRegion icon;
    public final Supplier<Building> factory;

    public BuildEntry(String name, TextureRegion icon, Supplier<Building> factory) {
        this.name = name;
        this.icon = icon;
        this.factory = factory;
    }
}
