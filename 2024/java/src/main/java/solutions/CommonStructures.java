package solutions;

import java.util.List;

/**
 * Class exposing data structures that
 * are commonly used across AoC puzzle
 * solutions, includes things like points on 2D
 * grids and so on.
 */
public class CommonStructures {
    /**
     * Represents points on 2D grid where point locations range
     * from (0,0) to (+infinity, +infinity).
     *
     * @param x
     * @param y
     */
    public record Point(int x, int y) {
        /**
         * @param translationVector
         * @return This {@link Point} translated by translationVector
         */
        public Point translateBy(Point translationVector) {
            return new Point(x + translationVector.x, y + translationVector.y);
        }

        public <T> boolean isInsideGrid(List<List<T>> grid) {
            return !grid.isEmpty()
                   && grid.stream().noneMatch(List::isEmpty)
                   && 0 <= x
                   && x < grid.get(0).size()
                   && 0 <= y
                   && y < grid.size();
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

        public <T> List<Point> getNeighboursInsideGrid(List<List<T>> grid) {
            return getNeighbours().stream().filter(n -> n.isInsideGrid(grid)).toList();
        }

        private static final Point NORTH_UNIT_VECTOR = new Point(0, 1);
        private static final Point EAST_UNIT_VECTOR = new Point(1, 0);
        private static final Point SOUTH_UNIT_VECTOR = new Point(0, -1);
        private static final Point WEST_UNIT_VECTOR = new Point(0, 1);
    }
}
