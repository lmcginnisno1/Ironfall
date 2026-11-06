package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.power.PowerGrid;
import dev.lmcginnisno1.ironfall.power.PowerStorage;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 1x1 battery. Charges from its grid's surplus and gives it back when the
 * grid falls short (see PowerSystem). Connects like any power building: by
 * touching another one, or through a power node.
 */
public class Battery extends Building implements PowerStorage {

    public static final int COST = Balance.BATTERY_COST;

    // Charge window inside the sprite, in pixels from the bottom-left of the 16x16 tile.
    private static final int BAR_X = 5, BAR_Y = 3, BAR_W = 6, BAR_H = 9;

    private float stored = 0f;
    private PowerGrid grid;

    public Battery(int x, int y) {
        super(x, y, 1, 1, Assets.battery, COST);
    }

    @Override
    public float capacity() {
        return Balance.BATTERY_CAPACITY;
    }

    @Override
    public float stored() {
        return stored;
    }

    @Override
    public void setStored(float amount) {
        stored = Math.max(0f, Math.min(amount, capacity()));
    }

    @Override
    public void setPowerGrid(PowerGrid grid) {
        this.grid = grid;
    }

    @Override
    public void render(SpriteBatch batch) {
        super.render(batch);

        // Charge level: fills the window from the bottom, red when low, green when high.
        float fill = stored / capacity();
        int h = Math.round(BAR_H * fill);
        if (h <= 0) return;
        if (fill < 0.25f) batch.setColor(0.9f, 0.3f, 0.2f, 1f);
        else if (fill < 0.6f) batch.setColor(0.95f, 0.75f, 0.2f, 1f);
        else batch.setColor(0.35f, 0.85f, 0.35f, 1f);
        batch.draw(Assets.whitePixel, x * 16 + BAR_X, y * 16 + BAR_Y, BAR_W, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public List<String> infoLines() {
        return List.of(
            "BATTERY",
            "Charge: " + Math.round(stored) + "/" + Math.round(capacity()),
            PowerGrid.describe(grid)
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new Battery(x, y);
    }

    @Override
    public BuildingType type() {
        return BuildingType.BATTERY;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeFloat(stored);
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        setStored(in.readFloat());
    }
}
