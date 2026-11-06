package dev.lmcginnisno1.ironfall.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import dev.lmcginnisno1.ironfall.buildings.Conveyor;

public class Assets {
    public static TextureRegion conveyorUp;
    public static TextureRegion conveyorDown;
    public static TextureRegion conveyorLeft;
    public static TextureRegion conveyorRight;

    public static TextureRegion splitterUp;
    public static TextureRegion splitterDown;
    public static TextureRegion splitterLeft;
    public static TextureRegion splitterRight;

    public static TextureRegion core;
    public static TextureRegion basicMiner;
    public static TextureRegion smelter;
    public static TextureRegion combustionGenerator;
    public static TextureRegion powerNode;
    public static TextureRegion battery;
    public static TextureRegion electricMiner;
    public static TextureRegion waterPump;
    public static TextureRegion waterTank;
    public static TextureRegion conduitUp;
    public static TextureRegion conduitDown;
    public static TextureRegion conduitLeft;
    public static TextureRegion conduitRight;

    // 90-degree corner pieces, indexed [incoming travel direction][outgoing direction] by ordinal.
    private static final TextureRegion[][] conveyorCorners = new TextureRegion[4][4];
    private static final TextureRegion[][] conduitCorners = new TextureRegion[4][4];
    public static TextureRegion whitePixel;

    // Every texture we create, so dispose() can't miss one.
    private static final Array<Texture> textures = new Array<>();

    // Loads a sprite with Nearest filtering so pixel art stays crisp when the camera zooms.
    private static TextureRegion sprite(String path) {
        Texture t = new Texture(path);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(t);
        return new TextureRegion(t);
    }

    public static void load() {
        conveyorUp = sprite("buildings/conveyor/conveyor_straight_up.png");
        conveyorDown = sprite("buildings/conveyor/conveyor_straight_down.png");
        conveyorLeft = sprite("buildings/conveyor/conveyor_straight_left.png");
        conveyorRight = sprite("buildings/conveyor/conveyor_straight_right.png");

        splitterUp = sprite("buildings/splitter/splitter_up.png");
        splitterDown = sprite("buildings/splitter/splitter_down.png");
        splitterLeft = sprite("buildings/splitter/splitter_left.png");
        splitterRight = sprite("buildings/splitter/splitter_right.png");

        core = sprite("buildings/core.png");
        basicMiner = sprite("buildings/miner.png");
        smelter = sprite("buildings/smelter.png");
        combustionGenerator = sprite("buildings/combustion_generator.png");
        powerNode = sprite("buildings/power_node.png");
        battery = sprite("buildings/battery.png");
        electricMiner = sprite("buildings/electric_miner.png");
        waterPump = sprite("buildings/water_pump.png");
        waterTank = sprite("buildings/water_tank.png");
        conduitUp = sprite("buildings/conduit/conduit_up.png");
        conduitDown = sprite("buildings/conduit/conduit_down.png");
        conduitLeft = sprite("buildings/conduit/conduit_left.png");
        conduitRight = sprite("buildings/conduit/conduit_right.png");

        for (Conveyor.Direction in : Conveyor.Direction.values()) {
            for (Conveyor.Direction out : Conveyor.Direction.values()) {
                boolean inHorizontal = in == Conveyor.Direction.LEFT || in == Conveyor.Direction.RIGHT;
                boolean outHorizontal = out == Conveyor.Direction.LEFT || out == Conveyor.Direction.RIGHT;
                if (inHorizontal == outHorizontal) continue; // only 90-degree turns
                String name = in.name().toLowerCase() + "_" + out.name().toLowerCase() + ".png";
                conveyorCorners[in.ordinal()][out.ordinal()] = sprite("buildings/conveyor/conveyor_corner_" + name);
                conduitCorners[in.ordinal()][out.ordinal()] = sprite("buildings/conduit/conduit_corner_" + name);
            }
        }

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture white = new Texture(pm);
        textures.add(white);
        whitePixel = new TextureRegion(white);
        pm.dispose();
    }

    /** Corner piece for a belt entering travelling {@code in} and leaving {@code out}. */
    public static TextureRegion conveyorCorner(Conveyor.Direction in, Conveyor.Direction out) {
        return conveyorCorners[in.ordinal()][out.ordinal()];
    }

    public static TextureRegion conduitCorner(Conveyor.Direction in, Conveyor.Direction out) {
        return conduitCorners[in.ordinal()][out.ordinal()];
    }

    public static void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
    }
}
