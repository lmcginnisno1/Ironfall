package dev.lmcginnisno1.ironfall.buildings;

import dev.lmcginnisno1.ironfall.game.Balance;

/**
 * Small water tank that buildings embed to get the cooling speed bonus.
 * The owner forwards {@link WaterAcceptor#acceptWater} to {@link #accept},
 * and calls {@link #use} each frame it's working.
 */
final class Coolant {

    private float water = 0f;
    private boolean cooledLastFrame = false;

    float accept(float amount) {
        float taken = Math.max(0f, Math.min(amount, Balance.COOLANT_BUFFER - water));
        water += taken;
        return taken;
    }

    /**
     * Uses water for {@code delta} seconds of work and returns the speed
     * multiplier: 1 with no water, up to 1 + COOLANT_SPEED_BONUS when fully cooled.
     */
    float use(float delta) {
        float need = Balance.COOLANT_USE * delta;
        if (need <= 0f) return 1f;
        float got = Math.min(water, need);
        water -= got;
        cooledLastFrame = got > 0f;
        return 1f + Balance.COOLANT_SPEED_BONUS * (got / need);
    }

    /** Marks a frame where the owner wasn't working, so the info line reads correctly. */
    void idle() {
        cooledLastFrame = water > 0f;
    }

    String infoLine() {
        int bonus = Math.round(Balance.COOLANT_SPEED_BONUS * 100f);
        return cooledLastFrame
            ? "Cooling: on (+" + bonus + "% speed)"
            : "Cooling: off (pipe in water for +" + bonus + "%)";
    }

    float water() {
        return water;
    }

    void setWater(float amount) {
        water = Math.max(0f, Math.min(amount, Balance.COOLANT_BUFFER));
    }
}
