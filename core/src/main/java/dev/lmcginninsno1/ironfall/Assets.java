package dev.lmcginninsno1.ironfall;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
    public static TextureRegion conveyorUp;
    public static TextureRegion conveyorDown;
    public static TextureRegion conveyorLeft;
    public static TextureRegion conveyorRight;
    public static TextureRegion core;
    public static TextureRegion basicMiner;
    public static TextureRegion whitePixel;


    public static void load() {
        conveyorUp = new TextureRegion(new Texture("buildings/conveyor_straight_up.png"));
        conveyorDown = new TextureRegion(new Texture("buildings/conveyor_straight_down.png"));
        conveyorLeft = new TextureRegion(new Texture("buildings/conveyor_straight_left.png"));
        conveyorRight = new TextureRegion(new Texture("buildings/conveyor_straight_right.png"));
        core = new TextureRegion(new Texture("buildings/core.png"));
        basicMiner = new TextureRegion(new Texture("buildings/miner.png"));

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        whitePixel = new TextureRegion(new Texture(pm));
        pm.dispose();
    }

    public static void dispose(){
        conveyorUp.getTexture().dispose();
        conveyorDown.getTexture().dispose();
        conveyorLeft.getTexture().dispose();
        conveyorRight.getTexture().dispose();
        core.getTexture().dispose();
        whitePixel.getTexture().dispose();
    }
}
