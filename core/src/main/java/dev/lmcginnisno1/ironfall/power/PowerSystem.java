package dev.lmcginnisno1.ironfall.power;

import dev.lmcginnisno1.ironfall.buildings.Building;
import dev.lmcginnisno1.ironfall.buildings.BuildingManager;
import dev.lmcginnisno1.ironfall.buildings.PowerNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups power blocks into grids. Two power blocks share a grid when they
 * touch (share an edge), or when one is a {@link PowerNode} and the other is
 * within its laser range. Grids are rebuilt only when buildings are added or
 * removed; totals are recomputed every frame.
 */
public class PowerSystem {

    /** A node-to-building laser, kept for rendering. */
    public record Link(Building from, Building to) {}

    private final BuildingManager buildings;

    private final List<PowerGrid> grids = new ArrayList<>();
    private final List<Link> links = new ArrayList<>();
    private boolean dirty = true;

    public PowerSystem(BuildingManager buildings) {
        this.buildings = buildings;
    }

    /** Call whenever a building is placed or removed. */
    public void markDirty() {
        dirty = true;
    }

    public List<Link> getLinks() {
        if (dirty) rebuild();
        return links;
    }

    public void update() {
        if (dirty) rebuild();

        for (PowerGrid grid : grids) {
            grid.produced = 0f;
            grid.demand = 0f;
            for (PowerBlock b : grid.members) {
                grid.produced += b.powerProduction();
                grid.demand += b.powerDemand();
            }
            for (PowerBlock b : grid.members) {
                b.setPowerGrid(grid);
            }
        }
    }

    // ---------------------------------------------------------------
    // Grid building (union-find over power blocks)
    // ---------------------------------------------------------------

    private void rebuild() {
        dirty = false;
        grids.clear();
        links.clear();

        List<Building> blocks = new ArrayList<>();
        for (Building b : buildings.getBuildings()) {
            if (b instanceof PowerBlock) blocks.add(b);
        }

        Map<Building, Integer> index = new HashMap<>();
        for (int i = 0; i < blocks.size(); i++) index.put(blocks.get(i), i);

        int[] parent = new int[blocks.size()];
        for (int i = 0; i < parent.length; i++) parent[i] = i;

        // Touching power blocks conduct to each other.
        for (int i = 0; i < blocks.size(); i++) {
            Building b = blocks.get(i);
            for (int[] t : edgeNeighbours(b)) {
                Integer j = index.get(buildings.getAt(t[0], t[1]));
                if (j != null) union(parent, i, j);
            }
        }

        // Nodes link to every power block within range.
        for (int i = 0; i < blocks.size(); i++) {
            if (!(blocks.get(i) instanceof PowerNode node)) continue;
            for (int j = 0; j < blocks.size(); j++) {
                Building other = blocks.get(j);
                if (i == j || !node.inRange(other)) continue;

                // Draw node-to-node lasers once, not in both directions.
                boolean duplicate = other instanceof PowerNode otherNode && otherNode.inRange(node) && j < i;
                if (!duplicate) links.add(new Link(node, other));
                union(parent, i, j);
            }
        }

        Map<Integer, PowerGrid> byRoot = new HashMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            PowerGrid grid = byRoot.computeIfAbsent(find(parent, i), k -> {
                PowerGrid g = new PowerGrid();
                grids.add(g);
                return g;
            });
            grid.members.add((PowerBlock) blocks.get(i));
        }
    }

    /** Tiles directly outside each edge of a building's footprint (no diagonals). */
    private static List<int[]> edgeNeighbours(Building b) {
        List<int[]> out = new ArrayList<>();
        for (int x = b.x; x < b.x + b.width; x++) {
            out.add(new int[]{x, b.y - 1});
            out.add(new int[]{x, b.y + b.height});
        }
        for (int y = b.y; y < b.y + b.height; y++) {
            out.add(new int[]{b.x - 1, y});
            out.add(new int[]{b.x + b.width, y});
        }
        return out;
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        parent[find(parent, a)] = find(parent, b);
    }
}
