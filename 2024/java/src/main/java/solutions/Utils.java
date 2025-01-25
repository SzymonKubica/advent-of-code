package solutions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static  Stream<String> readInputAsStream(String inputFile) {
        try {
            var reader = new BufferedReader(new FileReader(inputFile));
            return reader.lines();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Given a list of generic objects, it returns a string that has
     * prints each object on a separate line. This is very useful for validating
     * if the input has been parsed correctly when printing internal representation
     * objects to the console.
     */
    public static <T> String toStringLineByLine(List<T> objects) {
        return objects.stream().map(Objects::toString).collect(Collectors.joining("\n"));
    }

    private Utils() {}
}


