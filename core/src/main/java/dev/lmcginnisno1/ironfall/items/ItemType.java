package dev.lmcginnisno1.ironfall.items;

public enum ItemType {
    COAL(10, 14, "coal"),
    IRON(10, 15, "iron"),
    COPPER(11, 15, "copper"),
    IRON_INGOT(10, 16, "iron_ingot"),
    COPPER_INGOT(11, 16, "copper_ingot");

    public final int row;
    public final int col;
    public final String name;

    ItemType(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }

    public static ItemType oreTypeFromId(int id) {
        return switch (id) {
            case 11 -> ItemType.COAL;
            case 12 -> ItemType.IRON;
            case 13 -> ItemType.COPPER;
            case 14 -> ItemType.IRON_INGOT;
            case 15 -> ItemType.COPPER_INGOT;
            default -> null;
        };
    }
}
