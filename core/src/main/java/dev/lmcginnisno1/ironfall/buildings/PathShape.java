package dev.lmcginnisno1.ironfall.buildings;

/**
 * Decides whether a conveyor or conduit should be drawn as a 90-degree
 * corner, from its neighbours. Purely visual: movement is unchanged, since
 * items and water already travel tile centre to tile centre.
 */
final class PathShape {

    private PathShape() {}

    /**
     * Returns the travel direction of the piece feeding this one from the
     * side, if it should be drawn as a corner; null means draw it straight.
     * A piece is a corner when nothing feeds it from behind and exactly one
     * piece of the same kind feeds it from the left or right.
     */
    static Conveyor.Direction cornerInput(Building self, Conveyor.Direction dir) {
        BuildingManager world = self.world;
        if (world == null) return null;
        Class<?> kind = self.getClass();

        // Fed from behind (same kind of piece pointing at us), or backed by a
        // building like a miner or pump: keep it straight. A belt or pipe
        // behind that runs past without feeding us doesn't count.
        Building behind = world.getAt(self.x - Conveyor.dx(dir), self.y - Conveyor.dy(dir));
        if (behind != null) {
            if (kind.isInstance(behind)) {
                if (feeds(behind, self)) return null;
            } else if (!(behind instanceof Conveyor) && !(behind instanceof Conduit)) {
                return null;
            }
        }

        Conveyor.Direction found = null;
        for (Conveyor.Direction side : sidesOf(dir)) {
            Building b = world.getAt(self.x + Conveyor.dx(side), self.y + Conveyor.dy(side));
            if (!kind.isInstance(b) || !feeds(b, self)) continue;
            if (found != null) return null; // fed from both sides: a merge, draw straight
            found = b.facing();
        }
        return found;
    }

    /** True if path piece {@code from} points into {@code to}. */
    private static boolean feeds(Building from, Building to) {
        Conveyor.Direction d = from.facing();
        return d != null && from.x + Conveyor.dx(d) == to.x && from.y + Conveyor.dy(d) == to.y;
    }

    private static Conveyor.Direction[] sidesOf(Conveyor.Direction dir) {
        return switch (dir) {
            case UP, DOWN -> new Conveyor.Direction[]{Conveyor.Direction.LEFT, Conveyor.Direction.RIGHT};
            case LEFT, RIGHT -> new Conveyor.Direction[]{Conveyor.Direction.UP, Conveyor.Direction.DOWN};
        };
    }
}
