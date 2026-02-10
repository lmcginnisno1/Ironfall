package dev.lmcginnisno1.ironfall.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TextUtil {
    public static void drawOutlined(BitmapFont font, SpriteBatch batch, String text, float x, float y) {
        font.setColor(Color.BLACK);
        font.draw(batch, text, x - 1, y);
        font.draw(batch, text, x + 1, y);
        font.draw(batch, text, x, y - 1);
        font.draw(batch, text, x, y + 1);

        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, text, x, y);
    }
}
