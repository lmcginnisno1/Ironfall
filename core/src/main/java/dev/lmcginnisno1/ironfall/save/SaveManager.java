package dev.lmcginnisno1.ironfall.save;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;

import dev.lmcginnisno1.ironfall.IronfallGame;
import dev.lmcginnisno1.ironfall.buildings.Building;
import dev.lmcginnisno1.ironfall.buildings.BuildingManager;
import dev.lmcginnisno1.ironfall.buildings.BuildingType;
import dev.lmcginnisno1.ironfall.buildings.Conveyor;
import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.game.Upgrades;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Reads and writes the single save slot.
 *
 * File layout (gzipped, big-endian):
 *   int magic, int version, long seed, int width, int height,
 *   width*height tile id bytes (row by row),
 *   int credits, int minerLevel, int beltLevel, int storageLevel,
 *   float camX, float camY, float camZoom,
 *   int buildingCount, then per building:
 *     UTF typeId, int x, int y, byte facing (-1 = none), int stateLength, state bytes
 *
 * Bump VERSION whenever the layout changes, and keep reading older versions.
 */
public class SaveManager {

    public static final int VERSION = 1;
    private static final int MAGIC = 0x4952464C; // "IRFL"

    /** Seconds between autosaves. */
    public static final float AUTOSAVE_INTERVAL = 120f;

    private final IronfallGame game;

    public SaveManager(IronfallGame game) {
        this.game = game;
    }

    /**
     * Desktop: ~/.ironfall/world.sav. Android: app-private storage.
     * Launching with -Dironfall.saveDir=&lt;folder&gt; uses that folder instead,
     * so test runs never touch a player's real save.
     */
    public FileHandle saveFile() {
        String override = System.getProperty("ironfall.saveDir");
        if (override != null && !override.isBlank()) {
            return Gdx.files.absolute(override).child("world.sav");
        }
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            return Gdx.files.external(".ironfall/world.sav");
        }
        return Gdx.files.local("world.sav");
    }

    public boolean hasSave() {
        return saveFile().exists();
    }

    // ---------------------------------------------------------------
    // Saving
    // ---------------------------------------------------------------

    public void save() throws IOException {
        FileHandle file = saveFile();
        FileHandle tmp = file.sibling(file.name() + ".tmp");

        // Write to a temp file first so a crash mid-save can't wreck the existing save.
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(tmp.write(false)))) {
            writeWorld(out);
        }
        tmp.moveTo(file);
    }

    private void writeWorld(DataOutputStream out) throws IOException {
        TileEngine engine = game.engine;
        BuildingManager buildings = game.buildingManager;
        Upgrades upgrades = buildings.getUpgrades();
        OrthographicCamera cam = engine.getCamera();

        out.writeInt(MAGIC);
        out.writeInt(VERSION);
        out.writeLong(game.worldSeed);

        int w = engine.getWidth();
        int h = engine.getHeight();
        out.writeInt(w);
        out.writeInt(h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.writeByte(engine.getTile(x, y));
            }
        }

        out.writeInt(game.credits);
        out.writeInt(upgrades.minerSpeedLevel);
        out.writeInt(upgrades.beltSpeedLevel);
        out.writeInt(upgrades.storageCapacityLevel);

        out.writeFloat(cam.position.x);
        out.writeFloat(cam.position.y);
        out.writeFloat(cam.zoom);

        List<Building> list = buildings.getBuildings();
        out.writeInt(list.size());
        for (Building b : list) {
            out.writeUTF(b.type().id);
            out.writeInt(b.x);
            out.writeInt(b.y);
            Conveyor.Direction facing = b.facing();
            out.writeByte(facing == null ? -1 : facing.ordinal());

            ByteArrayOutputStream state = new ByteArrayOutputStream();
            b.writeState(new DataOutputStream(state));
            out.writeInt(state.size());
            state.writeTo(out);
        }
    }

    // ---------------------------------------------------------------
    // Loading
    // ---------------------------------------------------------------

    private record BuildingRecord(String typeId, int x, int y, int facing, byte[] state) {}

    /**
     * Replaces the current world with the saved one. The whole file is read
     * and checked before anything is changed, so a bad file leaves the
     * current game untouched.
     */
    public void load() throws IOException {
        FileHandle file = saveFile();
        if (!file.exists()) throw new IOException("No save file at " + file.path());

        TileEngine engine = game.engine;
        int w = engine.getWidth();
        int h = engine.getHeight();

        long seed;
        byte[] tiles;
        int credits, minerLevel, beltLevel, storageLevel;
        float camX, camY, camZoom;
        List<BuildingRecord> records = new ArrayList<>();

        try (DataInputStream in = new DataInputStream(new GZIPInputStream(file.read()))) {
            if (in.readInt() != MAGIC) throw new IOException("Not an Ironfall save");

            int version = in.readInt();
            if (version > VERSION) {
                throw new IOException("Save is from a newer version of Ironfall (format " + version + ")");
            }

            seed = in.readLong();
            int savedW = in.readInt();
            int savedH = in.readInt();
            if (savedW != w || savedH != h) {
                throw new IOException("Save map size " + savedW + "x" + savedH + " doesn't match " + w + "x" + h);
            }

            tiles = new byte[w * h];
            in.readFully(tiles);

            credits = in.readInt();
            minerLevel = in.readInt();
            beltLevel = in.readInt();
            storageLevel = in.readInt();

            camX = in.readFloat();
            camY = in.readFloat();
            camZoom = in.readFloat();

            int count = in.readInt();
            if (count < 0) throw new IOException("Corrupt building count");
            for (int i = 0; i < count; i++) {
                String typeId = in.readUTF();
                int x = in.readInt();
                int y = in.readInt();
                int facing = in.readByte();
                int len = in.readInt();
                if (len < 0) throw new IOException("Corrupt building data");
                byte[] state = new byte[len];
                in.readFully(state);
                records.add(new BuildingRecord(typeId, x, y, facing, state));
            }
        }

        boolean hasCore = records.stream().anyMatch(r -> r.typeId.equals(BuildingType.CORE.id));
        if (!hasCore) throw new IOException("Save has no Core");

        // --- Everything read fine: apply it ---

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                engine.setTile(x, y, tiles[y * w + x]);
            }
        }

        BuildingManager buildings = game.buildingManager;
        buildings.clear();

        Upgrades upgrades = buildings.getUpgrades();
        upgrades.minerSpeedLevel = clampLevel(minerLevel);
        upgrades.beltSpeedLevel = clampLevel(beltLevel);
        upgrades.storageCapacityLevel = clampLevel(storageLevel);

        Conveyor.Direction[] dirs = Conveyor.Direction.values();
        for (BuildingRecord r : records) {
            BuildingType type = BuildingType.byId(r.typeId);
            if (type == null) {
                Gdx.app.error("Save", "Skipping unknown building type '" + r.typeId + "'");
                continue;
            }

            Conveyor.Direction facing = (r.facing >= 0 && r.facing < dirs.length) ? dirs[r.facing] : null;
            if (facing == null && (type == BuildingType.CONVEYOR || type == BuildingType.SPLITTER || type == BuildingType.CONDUIT)) {
                Gdx.app.error("Save", "Skipping " + r.typeId + " at " + r.x + "," + r.y + " with no direction");
                continue;
            }

            Building b = type.create(r.x, r.y, facing, engine);
            try {
                b.readState(new DataInputStream(new ByteArrayInputStream(r.state)));
            } catch (IOException e) {
                // Keep the building but with fresh state rather than dropping it.
                Gdx.app.error("Save", "Couldn't read state of " + r.typeId + " at " + r.x + "," + r.y, e);
                b = type.create(r.x, r.y, facing, engine);
            }
            buildings.place(b);
        }

        game.worldSeed = seed;
        game.credits = credits;

        OrthographicCamera cam = engine.getCamera();
        cam.position.set(camX, camY, 0);
        // Same zoom range CameraController allows when scrolling.
        cam.zoom = Float.isFinite(camZoom) ? Math.max(0.1f, Math.min(camZoom, 1.5f)) : 1f;
        cam.update();
    }

    private static int clampLevel(int level) {
        return Math.max(0, Math.min(level, Balance.UPGRADE_MAX_LEVEL));
    }
}
