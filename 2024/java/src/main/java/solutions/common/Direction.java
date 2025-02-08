package solutions.common;

import java.util.List;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    public static Direction fromChar(char c) {
        return switch (c) {
            case '^' -> UP;
            case 'v' -> DOWN;
            case '<' -> LEFT;
            case '>' -> RIGHT;
            default -> throw new IllegalStateException("Unexpected value: " + c);
        };
    }

    public boolean isHorizontal() {
        return List.of(LEFT, RIGHT).contains(this);
    }
}
