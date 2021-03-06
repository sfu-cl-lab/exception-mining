package edu.cmu.tetrad.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Returns one line at a time, with a method to determine whether another
 * line is available. Blank lines and lines beginning with the given comment
 * marker are skipped.
 *
 * @author Joseph Ramsey
 */
public final class Lineizer {

    /**
     * The character sequence being tokenized.
     */
    private final BufferedReader reader;

    /**
     * Stores the line read by hasMoreLines, until it is retrieved by nextLine,
     * at which point it is null.
     */
    private String tempLine = null;

    /**
     * The comment marker.
     */
    private String commentMarker;

    /**
     * The line number of the line most recently read.
     */
    private int lineNumber = 0;

    /**
     * Constructs a tokenizer for the given input line, using the given Pattern
     * as delimiter.
     */
    public Lineizer(Reader reader, String commentMarker) {
        if (reader == null) {
            throw new NullPointerException();
        }

        if (commentMarker == null) {
            throw new NullPointerException();
        }

        this.reader = new BufferedReader(reader);
        this.commentMarker = commentMarker;
    }

    /**
     * Returns true iff more tokens exist in the line.
     */
    public final boolean hasMoreLines() {
        if (tempLine == null) {
            try {
                tempLine = readLine();
                return tempLine != null;
            }
            catch (IOException e) {
                return false;
            }
        }
        else {
            return true;
        }
    }

    /**
     * Return the next token in the line.
     */
    public final String nextLine() {
        lineNumber++;

        if (tempLine == null) {
            try {
                return readLine();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            String line = tempLine;
            tempLine = null;
            return line;
        }
    }

    private String readLine() throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            if ("".equals(line)) {
                continue;
            }

            if (line.startsWith(commentMarker)) {
                continue;
            }

            return line;
        }

        return null;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
