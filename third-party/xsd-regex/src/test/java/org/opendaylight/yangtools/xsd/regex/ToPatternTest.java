/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.xsd.regex;

import static org.junit.Assert.assertEquals;
import java.util.regex.Pattern;
import org.junit.Test;

public class ToPatternTest {

    private static void testPattern(final String xsd, final String java) {
        final RegularExpression regex = new RegularExpression(xsd, "XFH");
        final Pattern pattern = regex.toPattern();

        final String expected = '^' + java + "$";
        assertEquals(expected, pattern.toString());
    }

    @Test
    public void testCaret() {
        testPattern("^", "\\^");
    }

    @Test
    public void testDot() {
        testPattern(".", ".");
    }

    @Test
    public void testDollar() {
        testPattern("$", "\\$");
    }

    @Test
    public void testDollarOneDollar() {
        testPattern("$1$", "\\$1\\$");
    }

    @Test
    public void testDollarRange() {
        testPattern("[$-%]+", "[\\$-%]+");
    }

    @Test
    public void testSimple() {
        testPattern("abc", "abc");
    }

    @Test
    public void testDotPlus() {
        testPattern(".+", ".+");
    }

    @Test
    public void testDotStar() {
        testPattern(".*", ".*");
    }

    @Test
    public void testSimpleOptional() {
        testPattern("a?", "a?");
    }

    @Test
    public void testRangeOptional() {
        testPattern("[a-z]?", "[a-z]?");
    }

}
