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
