/**
 * Copyright (c) 2025 El Mhadder Mohamed Rida. All rights reserved.
 * This code is licensed under the [MIT License](https://opensource.org/licenses/MIT).
 */
package io.github.emmrida.chat4us.util;

import java.io.PrintStream;

/**
 * Used to redirect System.out and System.err to both a log file and log text area.
 *
 * @author El Mhadder Mohamed Rida
 */
public class TeePrintStream extends PrintStream {
    private final PrintStream second;

    /**
     * Instantiates a new tee print stream.
     *
     * @param main the main print stream
     * @param second the second print stream
     */
    public TeePrintStream(PrintStream main, PrintStream second) {
        super(main);
        this.second = second;
    }

    /**
     * Overrides the write function
     *
     * @param buf the buf
     * @param off the off
     * @param len the len
     */
    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        second.write(buf, off, len);
    }

    /**
     * Overrides the write function.
     *
     * @param b the b
     */
    @Override
    public void write(int b) {
    	super.write(b);
        second.write(b);
    }

    /**
     * Overrides the flush function.
     */
    @Override
    public void flush() {
        super.flush();
        second.flush();
    }

    /**
     * Overrides the close function.
     */
    @Override
    public void close() {
        super.close();
        second.close();
    }
}
