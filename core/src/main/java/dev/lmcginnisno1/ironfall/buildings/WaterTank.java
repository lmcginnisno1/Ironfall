package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 2x2 water storage. Takes water from any side and feeds every neighbour
 * that accepts it (conduits leading away, cooled buildings), up to
 * WATER_TANK_OUTPUT_RATE per second. Useful as a buffer between pumps and
 * a cooling network.
 */
public class WaterTank extends Building implements WaterAcceptor {

    public static final int COST = Balance.WATER_TANK_COST;

    // Glass window in the sprite, in pixels from the bottom-left of the 32x32 sprite.
    private static final int WIN_X = 7, WIN_Y = 6, WIN_W = 18, WIN_H = 20;

    private float water = 0f;

    public WaterTank(int x, int y) {
        super(x, y, 2, 2, Assets.waterTank, COST);
    }

    @Override
    public float acceptWater(float amount, int fromX, int fromY) {
        float taken = Math.max(0f, Math.min(amount, Balance.WATER_TANK_CAPACITY - water));
        water += taken;
        return taken;
    }

    @Override
    public void update(float delta) {
        float out = Math.min(water, Balance.WATER_TANK_OUTPUT_RATE * delta);
        water -= pushWaterToNeighbours(out);
    }

    public float water() {
        return water;
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);
        int h = Math.round(WIN_H * water / Balance.WATER_TANK_CAPACITY);
        if (h <= 0) return;
        batch.setColor(0.25f, 0.6f, 0.95f, 0.9f);
        batch.draw(Assets.whitePixel, x * 16 + WIN_X, y * 16 + WIN_Y, WIN_W, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public List<String> infoLines() {
        return List.of(
            "WATER TANK",
            "Water: " + Math.round(water) + "/" + Math.round(Balance.WATER_TANK_CAPACITY),
            "Feeds neighbours up to " + Math.round(Balance.WATER_TANK_OUTPUT_RATE) + " water/s"
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new WaterTank(x, y);
    }

    @Override
    public BuildingType type() {
        return BuildingType.WATER_TANK;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeFloat(water);
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        water = Math.max(0f, Math.min(in.readFloat(), Balance.WATER_TANK_CAPACITY));
    }
}
