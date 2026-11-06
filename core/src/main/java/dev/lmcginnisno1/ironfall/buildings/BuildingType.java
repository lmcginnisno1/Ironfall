package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.tiles.TileEngine;

/**
 * Every placeable building type, with the stable id used in save files.
 * Never change an existing id: old saves look buildings up by it.
 */
public enum BuildingType {
    CORE("core", (x, y, facing, engine) -> new Core(x, y)),
    BASIC_MINER("basic_miner", (x, y, facing, engine) -> new BasicMiner(x, y, engine)),
    CONVEYOR("conveyor", (x, y, facing, engine) -> new Conveyor(x, y, facing)),
    SPLITTER("splitter", (x, y, facing, engine) -> new Splitter(x, y, facing)),
    SMELTER("smelter", (x, y, facing, engine) -> new Smelter(x, y)),
    COMBUSTION_GENERATOR("combustion_generator", (x, y, facing, engine) -> new CombustionGenerator(x, y)),
    POWER_NODE("power_node", (x, y, facing, engine) -> new PowerNode(x, y)),
    ELECTRIC_MINER("electric_miner", (x, y, facing, engine) -> new ElectricMiner(x, y, engine));

    @FunctionalInterface
    public interface Factory {
        Building create(int x, int y, Conveyor.Direction facing, TileEngine engine);
    }

    public final String id;
    private final Factory factory;

    BuildingType(String id, Factory factory) {
        this.id = id;
        this.factory = factory;
    }

    /** Builds a fresh instance. {@code facing} is ignored by buildings that don't rotate. */
    public Building create(int x, int y, Conveyor.Direction facing, TileEngine engine) {
        return factory.create(x, y, facing, engine);
    }

    /** Returns null for an unknown id (e.g. a save from a newer version). */
    public static BuildingType byId(String id) {
        for (BuildingType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }
}
