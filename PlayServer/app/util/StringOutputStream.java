package util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author arshsab
 * @since 07 2014
 */

public class StringOutputStream extends OutputStream {
    private final StringBuilder sb = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        if (b != -1)
            sb.append((char) b);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}