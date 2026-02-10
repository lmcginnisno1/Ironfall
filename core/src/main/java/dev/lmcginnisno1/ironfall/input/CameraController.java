package dev.lmcginnisno1.ironfall.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.function.BooleanSupplier;

public class CameraController {

    private final OrthographicCamera camera;

    private boolean dragging = false;
    private int lastX, lastY;
    private final int worldWidth, worldHeight;

    public CameraController(OrthographicCamera camera, BooleanSupplier dragEnabled, int worldWidth, int worldHeight) {
        this.camera = camera;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        // Register scroll + drag input
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean scrolled(float amountX, float amountY) {
                handleScroll(amountY);
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT && dragEnabled.getAsBoolean()) {
                    dragging = true;
                    lastX = screenX;
                    lastY = screenY;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    dragging = false;
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (dragging && dragEnabled.getAsBoolean()) {
                    int dx = screenX - lastX;
                    int dy = screenY - lastY;

                    camera.position.x -= dx * camera.zoom;
                    camera.position.y += dy * camera.zoom;

                    lastX = screenX;
                    lastY = screenY;
                }
                return false;
            }
        });
    }

    public void update(float delta) {
        camera.update();

        float moveSpeed = 1200f;
        float speed = moveSpeed * delta * camera.zoom;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;

        clampCamera();
    }

    private void handleScroll(float amountY) {
        float zoomSpeed = 0.1f;

        float oldZoom = camera.zoom;
        float newZoom = oldZoom + amountY * zoomSpeed;
        newZoom = Math.max(0.1f, Math.min(newZoom, 1.5f));

        zoomTowardCursor(newZoom);
    }

    private void zoomTowardCursor(float newZoom) {
        float mx = Gdx.input.getX();
        // Flip Y coordinate because libGDX screen Y goes from top to bottom
        float my = Gdx.graphics.getHeight() - Gdx.input.getY();

        // Get world position before zoom
        Vector3 before = camera.unproject(new Vector3(mx, my, 0));

        // Apply zoom
        camera.zoom = newZoom;
        camera.update();

        // Get world position after zoom
        Vector3 after = camera.unproject(new Vector3(mx, my, 0));

        // Move camera so cursor stays on same world position
        camera.position.x += (before.x - after.x);
        camera.position.y -= (before.y - after.y);

        // Clamp AFTER adjusting for zoom
        clampCamera();
    }

    private void clampCamera() {
        float halfW = camera.viewportWidth * camera.zoom * 0.5f;
        float halfH = camera.viewportHeight * camera.zoom * 0.5f;

        float maxX = worldWidth - halfW;

        float maxY = worldHeight - halfH;

        camera.position.x = Math.max(halfW, Math.min(camera.position.x, maxX));
        camera.position.y = Math.max(halfH, Math.min(camera.position.y, maxY));
    }
}
