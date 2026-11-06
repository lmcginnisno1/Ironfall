package dev.lmcginnisno1.ironfall.save;

import dev.lmcginnisno1.ironfall.items.ItemType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/** Shared read/write helpers for building state. Item types are stored by enum name so reordering ItemType is safe. */
public final class SaveIO {

    private SaveIO() {}

    /** Writes a possibly-null item type. */
    public static void writeItemType(DataOutputStream out, ItemType type) throws IOException {
        out.writeUTF(type == null ? "" : type.name());
    }

    /** Reads an item type written by {@link #writeItemType}. Unknown or empty names come back as null. */
    public static ItemType readItemType(DataInputStream in) throws IOException {
        String name = in.readUTF();
        if (name.isEmpty()) return null;
        try {
            return ItemType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void writeItemCounts(DataOutputStream out, Map<ItemType, Integer> counts) throws IOException {
        out.writeInt(counts.size());
        for (Map.Entry<ItemType, Integer> e : counts.entrySet()) {
            writeItemType(out, e.getKey());
            out.writeInt(e.getValue());
        }
    }

    /** Clears {@code counts} and fills it from the stream, skipping unknown item types. */
    public static void readItemCounts(DataInputStream in, Map<ItemType, Integer> counts) throws IOException {
        counts.clear();
        int n = in.readInt();
        for (int i = 0; i < n; i++) {
            ItemType type = readItemType(in);
            int amount = in.readInt();
            if (type != null) counts.put(type, amount);
        }
    }
}
