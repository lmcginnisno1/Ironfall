package dev.lmcginnisno1.ironfall.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

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

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture white = new Texture(pm);
        textures.add(white);
        whitePixel = new TextureRegion(white);
        pm.dispose();
    }

    public static void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
    }
}
