/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.kloudmake.host;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: yannick
 * Date: 16/09/12
 * Time: 21:43
 * To change this template use DataFile | Settings | DataFile Templates.
 */
class PipedStream extends PipedOutputStream {
    private boolean closed;
    private final CountDownLatch countDownLatch;

    PipedStream(PipedInputStream snk, CountDownLatch countDownLatch) throws IOException {
        super(snk);
        this.countDownLatch = countDownLatch;
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        countDownLatch.countDown();
        super.close();
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }
}
