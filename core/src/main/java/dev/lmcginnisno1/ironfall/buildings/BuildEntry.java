package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.function.Supplier;

public class BuildEntry {
    public final String name;
    public final TextureRegion icon;
    public final int cost;
    public final Supplier<Building> factory;
    public final boolean perTile; // price shown as "/tile" (drag-placed conveyors and conduits)

    public BuildEntry(String name, TextureRegion icon, int cost, Supplier<Building> factory) {
        this(name, icon, cost, factory, false);
    }

    public BuildEntry(String name, TextureRegion icon, int cost, Supplier<Building> factory, boolean perTile) {
        this.name = name;
        this.icon = icon;
        this.cost = cost;
        this.factory = factory;
        this.perTile = perTile;
    }
}
