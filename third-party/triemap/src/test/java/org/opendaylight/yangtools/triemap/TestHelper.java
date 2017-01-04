/*
 * (C) Copyright 2016 Pantheon Technologies, s.r.o. and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opendaylight.yangtools.triemap;

import org.junit.Assert;

public class TestHelper {

    public static void assertEquals (final long expected, final long found) {
        Assert.assertEquals (expected, found);
    }

    public static void assertEquals (final int expected, final int found) {
        Assert.assertEquals (expected, found);
    }

    public static void assertEquals (final Object expected, final Object found) {
        Assert.assertEquals (expected, found);
    }

    public static void assertTrue (final boolean found) {
        Assert.assertTrue (found);
    }

    public static void assertFalse (final boolean found) {
        Assert.assertFalse (found);
    }

}
