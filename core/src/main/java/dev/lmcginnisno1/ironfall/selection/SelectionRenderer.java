package dev.lmcginnisno1.ironfall.selection;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import dev.lmcginnisno1.ironfall.tiles.Assets;
import dev.lmcginnisno1.ironfall.buildings.Building;

public class SelectionRenderer {

    private final SelectionManager selection;

    public SelectionRenderer(SelectionManager selection) {
        this.selection = selection;
    }

    public void render(SpriteBatch batch) {
        Building selected = selection.getSelected();
        if (selected != null) {
            drawOutline(batch, selected.x, selected.y, selected.width, selected.height, Color.YELLOW);
        }

        for (Building b : selection.getMultiSelection()) {
            drawOutline(batch, b.x, b.y, b.width, b.height, Color.YELLOW);
        }

        if (selection.isDragging()) {
            var rect = selection.getSelectionRect();

            float x = rect.x * 16;
            float y = rect.y * 16;
            float w = rect.width * 16;
            float h = rect.height * 16;

            // Fill
            batch.setColor(0f, 0.6f, 1f, 0.25f);
            batch.draw(Assets.whitePixel, x, y, w, h);

            // Border
            batch.setColor(0f, 0.6f, 1f, 0.9f);
            batch.draw(Assets.whitePixel, x, y, w, 2);
            batch.draw(Assets.whitePixel, x, y + h - 2, w, 2);
            batch.draw(Assets.whitePixel, x, y, 2, h);
            batch.draw(Assets.whitePixel, x + w - 2, y, 2, h);

            // Highlight buildings inside drag rectangle (yellow)
            for (Building b : selection.getMultiSelection()) {
                drawOutline(batch, b.x, b.y, b.width, b.height, Color.YELLOW);
            }

            batch.setColor(Color.WHITE);
        }
    }

    // Draws a simple rectangular outline around a building footprint
    private void drawOutline(SpriteBatch batch, int tx, int ty, int w, int h, Color color) {

        float x = tx * 16;
        float y = ty * 16;
        float width = w * 16;
        float height = h * 16;

        batch.setColor(color);

        // bottom
        batch.draw(Assets.whitePixel, x, y, width, 2);
        // top
        batch.draw(Assets.whitePixel, x, y + height - 2, width, 2);
        // left
        batch.draw(Assets.whitePixel, x, y, 2, height);
        // right
        batch.draw(Assets.whitePixel, x + width - 2, y, 2, height);

        batch.setColor(Color.WHITE);
    }
}
