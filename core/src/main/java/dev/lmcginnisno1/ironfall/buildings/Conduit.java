package dev.lmcginnisno1.ironfall.buildings;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 1x1 directional pipe for water: the fluid version of a conveyor. Takes
 * water from any side except its front, holds a little, and pushes it to
 * whatever it points at, up to CONDUIT_FLOW_RATE per second.
 */
public class Conduit extends Building implements WaterAcceptor {

    public static final int COST = Balance.CONDUIT_COST_PER_TILE;

    public final Conveyor.Direction direction;
    private float water = 0f;

    public Conduit(int x, int y, Conveyor.Direction direction) {
        super(x, y, 1, 1, spriteFor(direction), COST);
        this.direction = direction;
    }

    public static TextureRegion spriteFor(Conveyor.Direction dir) {
        return switch (dir) {
            case UP -> Assets.conduitUp;
            case DOWN -> Assets.conduitDown;
            case LEFT -> Assets.conduitLeft;
            case RIGHT -> Assets.conduitRight;
        };
    }

    @Override
    public float acceptWater(float amount, int fromX, int fromY) {
        // Like a conveyor, never take water back from the tile it feeds.
        if (fromX == x + Conveyor.dx(direction) && fromY == y + Conveyor.dy(direction)) return 0f;
        float taken = Math.max(0f, Math.min(amount, Balance.CONDUIT_CAPACITY - water));
        water += taken;
        return taken;
    }

    @Override
    public void update(float delta) {
        float move = Math.min(water, Balance.CONDUIT_FLOW_RATE * delta);
        water -= pushWater(move, x + Conveyor.dx(direction), y + Conveyor.dy(direction));
    }

    public float water() {
        return water;
    }

    @Override
    public void render(SpriteBatch batch) {
        // Bends draw as a corner pipe instead of a straight one.
        Conveyor.Direction in = PathShape.cornerInput(this, direction);
        TextureRegion region = in == null ? sprite : Assets.conduitCorner(in, direction);
        batch.draw(region, x * 16, y * 16, 16, 16);

        // Water in the channel, stronger the fuller it is: one strip from the
        // entry side to the centre, one from the centre to the exit side.
        float fill = water / Balance.CONDUIT_CAPACITY;
        if (fill <= 0.01f) return;
        batch.setColor(0.25f, 0.6f, 0.95f, 0.35f + 0.5f * fill);
        Conveyor.Direction entrySide = opposite(in == null ? direction : in);
        batch.draw(Assets.whitePixel, x * 16 + 6, y * 16 + 6, 4, 4); // centre, drawn once
        drawChannelHalf(batch, entrySide);
        drawChannelHalf(batch, direction);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Channel strip from the centre square out to the edge on {@code side} (no overlap with the centre). */
    private void drawChannelHalf(SpriteBatch batch, Conveyor.Direction side) {
        float px = x * 16, py = y * 16;
        switch (side) {
            case RIGHT -> batch.draw(Assets.whitePixel, px + 10, py + 6, 5, 4);
            case LEFT -> batch.draw(Assets.whitePixel, px + 1, py + 6, 5, 4);
            case UP -> batch.draw(Assets.whitePixel, px + 6, py + 10, 4, 5);
            case DOWN -> batch.draw(Assets.whitePixel, px + 6, py + 1, 4, 5);
        }
    }

    private static Conveyor.Direction opposite(Conveyor.Direction d) {
        return switch (d) {
            case UP -> Conveyor.Direction.DOWN;
            case DOWN -> Conveyor.Direction.UP;
            case LEFT -> Conveyor.Direction.RIGHT;
            case RIGHT -> Conveyor.Direction.LEFT;
        };
    }

    @Override
    public List<String> infoLines() {
        return List.of(
            "CONDUIT",
            "Water: " + Math.round(water) + "/" + Math.round(Balance.CONDUIT_CAPACITY),
            "Moves up to " + Math.round(Balance.CONDUIT_FLOW_RATE) + " water/s"
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new Conduit(x, y, direction);
    }

    @Override
    public BuildingType type() {
        return BuildingType.CONDUIT;
    }

    @Override
    public Conveyor.Direction facing() {
        return direction;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeFloat(water);
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        water = Math.max(0f, Math.min(in.readFloat(), Balance.CONDUIT_CAPACITY));
    }
}
