package dev.lmcginninsno1.ironfall;

public enum TileType {

    EMPTY(0, 7),
    STONE(2, 9),
    COAL(10, 10),
    IRON(10, 11),
    COPPER(11, 11),
    DIRT(12, 16),
    SAND(14, 16);

    public final int row;
    public final int col;

    TileType(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
