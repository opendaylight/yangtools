/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class Ipv4AddressStringTest {
    @Test
    public void basicTests() {
        checkNoZoneString("0.0.0.0", 0);
        checkNoZoneString("1.2.3.4", 0x01020304);
        checkNoZoneString("255.255.255.255", 0xFFFFFFFF);
    }

    @Test
    public void basicZoneTests() {
        checkString("0.0.0.0", "a", 0);
        checkString("1.2.3.4", "b", 0x01020304);
        checkString("255.255.255.255", "9a", 0xFFFFFFFF);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyString() {
        Ipv4AddressStringSupport.getInstance().fromString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyIpv6String() {
        Ipv4AddressStringSupport.getInstance().fromString("::");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShortString() {
        Ipv4AddressStringSupport.getInstance().fromString("1.2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLongString() {
        Ipv4AddressStringSupport.getInstance().fromString("1.2.3.4.5");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwordfishString() {
        Ipv4AddressStringSupport.getInstance().fromString("10.1.3255");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyZone() {
        Ipv4AddressStringSupport.getInstance().fromString("0.0.0.0%");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadZone() {
        Ipv4AddressStringSupport.getInstance().fromString("0.0.0.0%+");
    }

    private static void checkString(final String addr, final String zone, final int bits) {
        final String canonical = addr + '%' + zone;
        final Ipv4AddressString val = Ipv4AddressStringSupport.getInstance().fromString(canonical).getFirst();
        assertEquals(bits, val.getIntBits());
        assertEquals(canonical, val.toString());
        assertTrue(val.equals(val));
        assertEquals(0, val.compareTo(val));
        assertSame(Ipv4AddressStringSupport.getInstance(), val.validator());
        assertSame(val, Ipv4AddressStringSupport.getInstance().validateRepresentation(val));
        assertSame(val, Ipv4AddressStringSupport.getInstance().validateRepresentation(val, canonical));
        assertEquals(Optional.of(zone), val.getZone());

        final Ipv4AddressString second = Ipv4AddressStringSupport.getInstance().fromString(canonical).getFirst();
        assertTrue(val.equals(second));
        assertEquals(0, val.compareTo(second));
    }

    private static void checkNoZoneString(final String canonical, final int bits) {
        final Ipv4AddressString val = Ipv4AddressStringSupport.getInstance().fromString(canonical).getFirst();
        assertEquals(bits, val.getIntBits());
        assertEquals(canonical, val.toString());
        assertTrue(val.equals(val));
        assertEquals(0, val.compareTo(val));
        assertSame(Ipv4AddressNoZoneStringValidator.getInstance(), val.validator());
        assertEquals(bits, val.hashCode());
        assertSame(val, Ipv4AddressNoZoneStringValidator.getInstance().validateRepresentation(val));
        assertSame(val, Ipv4AddressNoZoneStringValidator.getInstance().validateRepresentation(val, canonical));
        assertEquals(Optional.empty(), val.getZone());

        final Ipv4AddressString second = Ipv4AddressStringSupport.getInstance().fromString(canonical).getFirst();
        assertTrue(val.equals(second));
        assertEquals(0, val.compareTo(second));
    }
}
