package solutions.common;

import solutions.day12.Day12;

import java.util.List;

/**
 * Represents points on 2D grid where point locations range
 * from (0,0) to (+infinity, +infinity).
 *
 * @param x
 * @param y
 */
public record Point(int x, int y) {
    public static Point ORIGIN = new Point(0, 0);
    /**
     * @param translationVector
     * @return This {@link Point} translated by translationVector
     */
    public Point translateBy(Point translationVector) {
        return new Point(x + translationVector.x, y + translationVector.y);
    }

    public Point translateBy(Point translationVector, int times) {
        return new Point(x + translationVector.x * times, y + translationVector.y * times);
    }

    public Point difference(Point translationVector) {
        return new Point(x - translationVector.x, y - translationVector.y);
    }

    public Point reflectAboutOrigin() {
        return new Point(-x, -y);
    }

    public <T> boolean isInsideGrid(List<List<T>> grid) {
        return !grid.isEmpty() && 0 <= x && x < grid.get(0).size() && 0 <= y && y < grid.size();
    }

    public <T> T indexGrid(List<List<T>> grid) {
        assert isInsideGrid(grid) :
                "We can only index into the grid when the location is inside of it.";
        return grid.get(y).get(x);
    }

    public <T> T indexGridAndSet(List<List<T>> grid, T value) {
        assert isInsideGrid(grid) :
                "We can only index into the grid when the location is inside of it.";
        return grid.get(y).set(x, value);
    }

    /**
     * @return List of neighbour positions directly adjacent to this
     * on a 2D grid. Note that cells that are adjacent but diagonally
     * are not included.
     */
    public List<Point> getNeighbours() {
        return List.of(this.translateBy(NORTH_UNIT_VECTOR),
                       this.translateBy(EAST_UNIT_VECTOR),
                       this.translateBy(SOUTH_UNIT_VECTOR),
                       this.translateBy(WEST_UNIT_VECTOR));
    }

    public List<Point> getNineNeighbours() {
        return List.of(this.translateBy(NORTH_UNIT_VECTOR),
                       this.translateBy(NORTH_EAST_VECTOR),
                       this.translateBy(EAST_UNIT_VECTOR),
                       this.translateBy(SOUTH_EAST_VECTOR),
                       this.translateBy(SOUTH_UNIT_VECTOR),
                       this.translateBy(SOUTH_WEST_VECTOR),
                       this.translateBy(WEST_UNIT_VECTOR),
                       this.translateBy(NORTH_WEST_VECTOR)
                       );
    }

    public Point translateInDirection(Direction direction) {
        return switch (direction) {
            case UP -> this.translateBy(NORTH_UNIT_VECTOR);
            case DOWN -> this.translateBy(SOUTH_UNIT_VECTOR);
            case LEFT -> this.translateBy(WEST_UNIT_VECTOR);
            case RIGHT -> this.translateBy(EAST_UNIT_VECTOR);
        };
    }

    public <T> List<Point> getNeighbourLocationsInsideGrid(List<List<T>> grid) {
        return getNeighbours().stream().filter(n -> n.isInsideGrid(grid)).toList();
    }

    public <T> List<T> getNeighboursInsideGrid(List<List<T>> grid) {
        return getNeighbours().stream().filter(n -> n.isInsideGrid(grid)).map(n -> n.indexGrid(grid)).toList();
    }


    private static final Point NORTH_UNIT_VECTOR = new Point(0, -1);
    private static final Point NORTH_EAST_VECTOR = new Point(1, -1);
    private static final Point EAST_UNIT_VECTOR = new Point(1, 0);
    private static final Point SOUTH_EAST_VECTOR = new Point(1, 1);
    private static final Point SOUTH_UNIT_VECTOR = new Point(0, 1);
    private static final Point SOUTH_WEST_VECTOR = new Point(-1, 1);
    private static final Point WEST_UNIT_VECTOR = new Point(-1, 0);
    private static final Point NORTH_WEST_VECTOR = new Point(-1, -1);

    public <T> List<T> getNineNeighboursInsideGrid(List<List<T>> grid) {
        return getNineNeighbours().stream().filter(n -> n.isInsideGrid(grid)).map(n -> n.indexGrid(grid)).toList();
    }

    public Point moveInDirection(Direction direction) {
        return this.moveInDirection(direction, 1);
    }
    public Point moveInDirection(Direction direction, int times) {
        return switch (direction) {
            case UP -> this.translateBy(NORTH_UNIT_VECTOR);
            case DOWN -> this.translateBy(SOUTH_UNIT_VECTOR);
            case LEFT -> this.translateBy(WEST_UNIT_VECTOR);
            case RIGHT -> this.translateBy(EAST_UNIT_VECTOR);
        };
    }

    public record BigPoint(long x, long y) {
    }
}
