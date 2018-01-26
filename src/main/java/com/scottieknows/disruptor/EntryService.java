/**
 * Copyright (C) 2018 Scott Feldstein
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.scottieknows.disruptor;

import static java.lang.String.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.camel.Consume;
import org.springframework.stereotype.Service;

@Service
public class EntryService {
    private AtomicLong failures = new AtomicLong();
    private AtomicLong successes = new AtomicLong();

    @Consume(uri="direct:first")
    public String first(String payload) {
        return payload + " first";
    }

    @Consume(uri="direct:second")
    public String second(String payload) {
        Random rand = new Random();
        if ((rand.nextInt() % 3) == 0) {
            failures.incrementAndGet();
            throw new RuntimeException(format("second failed %s, %s", System.currentTimeMillis(), getPct()));
        }
        successes.incrementAndGet();
        return payload + " second";
    }

    private Object getPct() {
        long f = failures.get();
        long s = successes.get();
        return ((float) f / (f + s)) * 100;
    }

    @Consume(uri="direct:third")
    public String third(String payload) {
        return payload + " third";
    }

}
