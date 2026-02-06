package dev.lmcginninsno1.ironfall.placement;

import com.badlogic.gdx.math.Vector2;
import dev.lmcginninsno1.ironfall.buildings.Building;
import dev.lmcginninsno1.ironfall.buildings.BuildingManager;
import dev.lmcginninsno1.ironfall.buildings.Conveyor;

import java.util.ArrayList;

public class ConveyorPathHelper {

    private final BuildingManager buildings;

    public ConveyorPathHelper(BuildingManager buildings) {
        this.buildings = buildings;
    }

    public ArrayList<Vector2> computePath(int sx, int sy, int ex, int ey, boolean horizontalFirst) {
        Building target = getBuildingAt(ex, ey);
        if (target != null) {
            Vector2 entry = getBestEntryForBuilding(target, sx, sy);
            ex = (int) entry.x;
            ey = (int) entry.y;
        }

        ArrayList<Vector2> path = new ArrayList<>();
        int x = sx;
        int y = sy;

        if (horizontalFirst) {
            while (x != ex) { path.add(new Vector2(x, y)); x += (ex > x ? 1 : -1); }
            while (y != ey) { path.add(new Vector2(x, y)); y += (ey > y ? 1 : -1); }
        } else {
            while (y != ey) { path.add(new Vector2(x, y)); y += (ey > y ? 1 : -1); }
            while (x != ex) { path.add(new Vector2(x, y)); x += (ex > x ? 1 : -1); }
        }

        path.add(new Vector2(ex, ey));
        return path;
    }

    public Conveyor.Direction getDirectionForIndex(ArrayList<Vector2> path, int i) {
        Vector2 current = path.get(i);

        if (i == path.size() - 1) {
            Building b = getAdjacentBuilding((int) current.x, (int) current.y);
            if (b != null) {
                if (current.x == b.x - 1) return Conveyor.Direction.RIGHT;
                if (current.x == b.x + b.width) return Conveyor.Direction.LEFT;
                if (current.y == b.y - 1) return Conveyor.Direction.UP;
                if (current.y == b.y + b.height) return Conveyor.Direction.DOWN;
            }
        }

        if (path.size() == 1) return Conveyor.Direction.RIGHT;
        if (i == 0) return directionFor(current, path.get(i + 1));
        if (i == path.size() - 1) return directionFor(path.get(i - 1), current);
        return directionFor(current, path.get(i + 1));
    }

    private Conveyor.Direction directionFor(Vector2 a, Vector2 b) {
        if (b.x > a.x) return Conveyor.Direction.RIGHT;
        if (b.x < a.x) return Conveyor.Direction.LEFT;
        if (b.y > a.y) return Conveyor.Direction.UP;
        return Conveyor.Direction.DOWN;
    }

    private Building getBuildingAt(int tx, int ty) {
        for (Building b : buildings.getBuildings()) {
            if (tx >= b.x && tx < b.x + b.width &&
                ty >= b.y && ty < b.y + b.height) {
                return b;
            }
        }
        return null;
    }

    private Building getAdjacentBuilding(int tx, int ty) {
        for (Building b : buildings.getBuildings()) {
            if (tx == b.x - 1 && ty >= b.y && ty < b.y + b.height) return b;
            if (tx == b.x + b.width && ty >= b.y && ty < b.y + b.height) return b;
            if (ty == b.y - 1 && tx >= b.x && tx < b.x + b.width) return b;
            if (ty == b.y + b.height && tx >= b.x && tx < b.x + b.width) return b;
        }
        return null;
    }

    private Vector2 getBestEntryForBuilding(Building b, int sx, int sy) {
        float midX = b.x + b.width / 2f;
        float midY = b.y + b.height / 2f;

        float dx = sx - midX;
        float dy = sy - midY;

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0
                ? new Vector2(b.x - 1, (int) midY)
                : new Vector2(b.x + b.width, (int) midY);
        } else {
            return dy < 0
                ? new Vector2((int) midX, b.y - 1)
                : new Vector2((int) midX, b.y + b.height);
        }
    }
}
