package dev.lmcginninsno1.ironfall.tiles;

public enum TileType {

    EMPTY(0, 7, -1, "empty"),
    DIRT(12, 16, 0, "dirt"),
    SAND(14, 16, 1, "sand"),
    STONE(2, 9, 10, "stone"),
    COAL(10, 10, 11, "coal"),
    IRON(10, 11, 12, "iron"),
    COPPER(11, 11, 13, "copper"),
    COAL_ORE(10, 14, 21, "coal_ore"),
    IRON_ORE(10, 15, 22, "iron_ore"),
    COPPER_ORE(11, 15, 23, "copper_ore");

    public final int row;
    public final int col;
    public final int id;
    public final String name;

    TileType(int row, int col, int id, String name) {
        this.row = row;
        this.col = col;
        this.id = id;
        this.name = name;
    }

    public static TileType fromId(int id) {
        for (TileType t : values()) {
            if (t.id == id) return t;
        }
        return EMPTY;
    }
}
