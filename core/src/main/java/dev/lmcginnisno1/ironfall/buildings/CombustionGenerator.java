package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.game.Balance;
import dev.lmcginnisno1.ironfall.items.Item;
import dev.lmcginnisno1.ironfall.items.ItemType;
import dev.lmcginnisno1.ironfall.power.PowerBlock;
import dev.lmcginnisno1.ironfall.power.PowerGrid;
import dev.lmcginnisno1.ironfall.tiles.Assets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 2x2 generator fed with coal from belts or miners. Burns one coal at a time
 * and produces power while burning, but only starts a new piece of coal when
 * something on its grid needs power or a battery has room, so an idle grid
 * doesn't waste fuel.
 */
public class CombustionGenerator extends Building implements ItemAcceptor, PowerBlock {

    public static final int COST = Balance.GENERATOR_COST;

    private int coal = 0;
    private float burnTimer = 0f; // seconds left on the current piece of coal
    private PowerGrid grid;

    public CombustionGenerator(int x, int y) {
        super(x, y, 2, 2, Assets.combustionGenerator, COST);
    }

    @Override
    public boolean acceptItem(Item item, int fromX, int fromY) {
        if (item.type() != ItemType.COAL || coal >= Balance.GENERATOR_MAX_COAL) return false;
        coal++;
        return true;
    }

    @Override
    public void update(float delta) {
        if (burnTimer > 0f) burnTimer -= delta;

        if (burnTimer <= 0f) {
            boolean wanted = grid != null && grid.wantsPower();
            if (coal > 0 && wanted) {
                coal--;
                // Carry over any overshoot so the burn rate stays exact.
                burnTimer += Balance.GENERATOR_COAL_BURN_TIME;
            } else {
                burnTimer = 0f;
            }
        }
    }

    public boolean isBurning() {
        return burnTimer > 0f;
    }

    @Override
    public float powerProduction() {
        return isBurning() ? Balance.GENERATOR_OUTPUT : 0f;
    }

    @Override
    public void setPowerGrid(PowerGrid grid) {
        this.grid = grid;
    }

    @Override
    public List<String> infoLines() {
        String state;
        if (isBurning()) state = "Burning: " + (int) Balance.GENERATOR_OUTPUT + " power/s";
        else if (coal == 0) state = "Idle: needs coal";
        else state = "Idle: nothing needs power";

        return List.of(
            "COMBUSTION GENERATOR",
            state,
            "Coal: " + coal + "/" + Balance.GENERATOR_MAX_COAL,
            PowerGrid.describe(grid)
        );
    }

    @Override
    public Building copyAt(int x, int y) {
        return new CombustionGenerator(x, y);
    }

    @Override
    public BuildingType type() {
        return BuildingType.COMBUSTION_GENERATOR;
    }

    @Override
    public void writeState(DataOutputStream out) throws IOException {
        out.writeInt(coal);
        out.writeFloat(burnTimer);
    }

    @Override
    public void readState(DataInputStream in) throws IOException {
        coal = in.readInt();
        burnTimer = in.readFloat();
    }
}
