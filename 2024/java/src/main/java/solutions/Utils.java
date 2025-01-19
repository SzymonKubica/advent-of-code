package solutions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private Utils() {}
}
