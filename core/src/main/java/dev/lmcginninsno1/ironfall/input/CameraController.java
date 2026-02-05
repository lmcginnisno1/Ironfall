package dev.lmcginninsno1.ironfall.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;

import java.util.function.BooleanSupplier;

public class CameraController {

    private final OrthographicCamera camera;

    private boolean dragging = false;
    private int lastX, lastY;

    public CameraController(OrthographicCamera camera, BooleanSupplier dragEnabled) {
        this.camera = camera;

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

        float moveSpeed = 1200f;
        float speed = moveSpeed * delta * camera.zoom;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;

        camera.update();
    }

    private void handleScroll(float amountY) {
        float oldZoom = camera.zoom;

        float zoomSpeed = 0.1f;
        camera.zoom += amountY * zoomSpeed;
        camera.zoom = Math.max(0.1f, Math.min(camera.zoom, 1f));

        zoomTowardCursor(oldZoom);
    }

    private void zoomTowardCursor(float oldZoom) {
        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();

        float worldBeforeX = (mx - camera.viewportWidth / 2f) * oldZoom + camera.position.x;
        float worldBeforeY = (camera.viewportHeight / 2f - my) * oldZoom + camera.position.y;

        float worldAfterX = (mx - camera.viewportWidth / 2f) * camera.zoom + camera.position.x;
        float worldAfterY = (camera.viewportHeight / 2f - my) * camera.zoom + camera.position.y;

        camera.position.x += (worldBeforeX - worldAfterX);
        camera.position.y += (worldBeforeY - worldAfterY);
    }
}
