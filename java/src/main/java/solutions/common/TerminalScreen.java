package solutions.common;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.TerminalFactory;
import com.googlecode.lanterna.terminal.TerminalResizeListener;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TerminalScreen {
    private final TerminalFactory defaultTerminalFactory;
    private com.googlecode.lanterna.terminal.Terminal terminal = null;

    public TerminalScreen(int fontSize) {
        this.defaultTerminalFactory = new DefaultTerminalFactory(System.out, System.in, Charset.forName("UTF8")).setTerminalEmulatorFontConfiguration(
                SwingTerminalFontConfiguration.getDefaultOfSize(fontSize));
        startDisplay();
    }

    public TerminalScreen() {
        this.defaultTerminalFactory = new DefaultTerminalFactory();
        startDisplay();
    }

    private void startDisplay() {
        try {
            terminal = defaultTerminalFactory.createTerminal();
            terminal.setCursorVisible(false);
            terminal.clearScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetCursorPosition() throws IOException {
        terminal.setCursorPosition(0, 0);
    }

    public void printLine(String line) throws IOException {
        printLine(line, TextColor.ANSI.BLACK_BRIGHT);
    }

    public void printLine(String line, TextColor color) throws IOException {
        printString(line, color);
        printCharacter('\n');
    }

    public void printNewLine() throws IOException {
        printLine("");
    }

    public void printString(String string) throws IOException {
        printString(string, TextColor.ANSI.BLACK_BRIGHT);
    }

    public void printString(String string, TextColor color) throws IOException {
        terminal.setForegroundColor(color);
        terminal.putString(string);
    }

    public void printCharacter(char c) throws IOException {
        printCharacter(c, TextColor.ANSI.BLACK_BRIGHT);
    }

    public void printCharacter(char c, TextColor color) throws IOException {
        terminal.setForegroundColor(color);
        terminal.putCharacter(c);
    }

    public void printLineCentered(String line) throws IOException {
        printLineCentered(line, TextColor.ANSI.BLACK_BRIGHT);
    }

    public void printLineCentered(String line, TextColor color) throws IOException {
        String margin = getCenteringMargin(line.length());
        printLine(margin + line, color);
    }

    public void printStringCentered(String string) throws IOException {
        printStringCentered(string, TextColor.ANSI.BLACK_BRIGHT);
    }

    public void printStringCentered(String string, TextColor color) throws IOException {
        String margin = getCenteringMargin(string.length());
        printString(margin + string, color);
    }

    public int getHorizontalCenteringMargin(int textWidth) throws IOException {
        return (getTerminalWidth() - textWidth) / 2;
    }

    public int getVerticalCenteringMargin(int textHeight) throws IOException {
        return (getTerminalHeight() - textHeight) / 2;
    }

    private String getCenteringMargin(int textLength) throws IOException {
        return getPaddingString((getTerminalWidth() - textLength) / 2);
    }

    private String getPaddingString(int length) {
        return IntStream.range(0, length).mapToObj(i -> " ").collect(Collectors.joining(""));
    }

    public void flushChanges() throws IOException {
        terminal.flush();
    }

    public KeyType getUserInput() throws IOException {
        return terminal.readInput().getKeyType();
    }

    public Character readCharacter() throws IOException {
        return terminal.readInput().getCharacter();
    }

    public int getTerminalWidth() throws IOException {
        return terminal.getTerminalSize().getColumns();
    }

    public int getTerminalHeight() throws IOException {
        return terminal.getTerminalSize().getRows();
    }

    public void addResizeListener(TerminalResizeListener listener) {
        terminal.addResizeListener(listener);
    }

    public void setCursorVisible(boolean isVisible) throws IOException {
        terminal.setCursorVisible(isVisible);
    }

    public void clearScreen() throws IOException {
        terminal.clearScreen();
    }
}