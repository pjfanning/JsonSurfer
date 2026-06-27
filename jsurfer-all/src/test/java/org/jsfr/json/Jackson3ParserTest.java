/*
 * MIT License
 *
 * Copyright (c) 2019 WANG Lingsong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jsfr.json;

import tools.jackson.databind.ObjectMapper;
import org.jsfr.json.provider.Jackson3Provider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class Jackson3ParserTest extends JsonSurferTest {

    @Before
    public void setUp() throws Exception {
        provider = new Jackson3Provider();
        surfer = new JsonSurfer(Jackson3Parser.INSTANCE, provider);
    }

    @Test
    public void testNonBlockingParser() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        SurfingConfiguration config = surfer.configBuilder()
                .bind("$['foo','bar']", mockListener)
                .build();
        byte[] part1 = "{\"foo\": 12".getBytes("UTF-8");
        byte[] part2 = "34, \"bar\": \"ab".getBytes("UTF-8");
        byte[] part3 = "cd\"}".getBytes("UTF-8");

        NonBlockingParser nonBlockingParser = surfer.createNonBlockingParser(config);
        assertTrue(nonBlockingParser.feed(part1, 0, part1.length));
        assertTrue(nonBlockingParser.feed(part2, 0, part2.length));
        assertTrue(nonBlockingParser.feed(part3, 0, part3.length));
        nonBlockingParser.endOfInput();
        assertFalse(nonBlockingParser.feed(part1, 0, 100));
        verify(mockListener).onValue(eq(provider.primitive(1234L)), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("abcd")), any(ParsingContext.class));
    }

    public void testParsingLongIntegers() throws Exception {
        Collector collector = surfer.collector(read("sample_big_integer.json"));
        ValueBox<Object> box = collector.collectOne("$.value");
        collector.exec();
        BigInteger expected = new BigInteger("3452453534534534534543534543545");
        assertEquals(expected, box.get());
    }

}
