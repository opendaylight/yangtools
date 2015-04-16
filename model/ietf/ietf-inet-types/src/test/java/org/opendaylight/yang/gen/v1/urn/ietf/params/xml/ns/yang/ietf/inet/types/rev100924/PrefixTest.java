/**
 * (C)2015 Brocade Communications Systems, Inc and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrefixTest {

    @Test
    public void testIpv4Prefix() throws Exception {
        testPrefixv4("192.168.0.1/32","192.168.0.1/32");
        testPrefixv4("192.168.0.1/24","192.168.0.0/24");
    }

    @Test
    public void testIpv6Prefix() throws Exception {
        /* basic test */
        testPrefixv6("::/0","::/0");
        /* canonicalization */
        testPrefixv6("deadbeef::/0","::/0");
        /* another canonicalization */
        testPrefixv6("2001:db8:85a3:0:0:8a2e:370:7334/32","2001:db8::/32");
    }

    private void testPrefixv4(final String ipv4String, final String ipv4NormString) {
        assertEquals(new Ipv4Prefix(ipv4String).getValue(), ipv4NormString);
    }

    private void testPrefixv6(final String ipv6String, final String ipv6NormString) {
        assertEquals(new Ipv6Prefix(ipv6String).getValue(), ipv6NormString);
    }

}
