/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HostBuilderTest {

    @Test
    public void testGetDefaultInstanceIpv4() throws Exception {
        final Host host = HostBuilder.getDefaultInstance("127.0.0.1");
        assertEquals(new Host(new IpAddress(new Ipv4Address("127.0.0.1"))), host);
    }

    @Test
    public void testGetDefaultInstanceIpv6() throws Exception {
        testIpv6("1234:5678:9abc:def1:2345:6789:abcd:ef12");
    }

    private void testIpv6(final String ivp6string) {
        final Host host = HostBuilder.getDefaultInstance(ivp6string);
        assertEquals(new Host(new IpAddress(new Ipv6Address(ivp6string))), host);
    }

    @Test
    public void testGetDefaultInstanceDomain() throws Exception {
        final Host host = HostBuilder.getDefaultInstance("localhost");
        assertEquals(new Host(new DomainName("localhost")), host);
    }
}