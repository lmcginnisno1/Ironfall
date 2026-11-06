package dev.lmcginnisno1.ironfall.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import dev.lmcginnisno1.ironfall.buildings.Building;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.tiles.TileEngine;

/** Simple shapes drawn with the batch's current colour, in world pixels. */
public final class DrawUtil {

    private DrawUtil() {}

    /** A straight line of the given thickness, drawn as a rotated rectangle. */
    public static void line(SpriteBatch batch, float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1, dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 0.01f) return;
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        batch.draw(Assets.whitePixel, x1, y1 - thickness / 2f, 0f, thickness / 2f, length, thickness, 1f, 1f, angle);
    }

    /** A dotted circle, used for ranges. */
    public static void dottedCircle(SpriteBatch batch, float cx, float cy, float radius, int dots, float dotSize) {
        for (int i = 0; i < dots; i++) {
            float a = i * MathUtils.PI2 / dots;
            batch.draw(Assets.whitePixel, cx + MathUtils.cos(a) * radius - dotSize / 2f,
                cy + MathUtils.sin(a) * radius - dotSize / 2f, dotSize, dotSize);
        }
    }

    public static float centerX(Building b) {
        return (b.x + b.width / 2f) * TileEngine.TILE_SIZE;
    }

    public static float centerY(Building b) {
        return (b.y + b.height / 2f) * TileEngine.TILE_SIZE;
    }
}
