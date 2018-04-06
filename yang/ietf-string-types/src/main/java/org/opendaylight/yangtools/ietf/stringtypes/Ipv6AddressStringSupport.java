/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.net.InetAddresses;
import java.net.Inet6Address;
import java.net.InetAddress;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringSupport;

@Beta
@NonNullByDefault
@ThreadSafe
public final class Ipv6AddressStringSupport extends AbstractDerivedStringSupport<Ipv6AddressString> {

    private static final Ipv6AddressStringSupport INSTANCE = new Ipv6AddressStringSupport();

    private Ipv6AddressStringSupport() {
        super(Ipv6AddressString.class);
    }

    public static Ipv6AddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Ipv6AddressString fromString(final String str) {
        final InetAddress address;
        final String zone;
        final int percent = str.indexOf('%');
        if (percent != -1) {
            zone = str.substring(percent + 1);
            checkArgument(!zone.isEmpty(), "Value \"%s\" is not a valid ipv6-address", str);
            address = InetAddresses.forString(str.substring(0, percent));
        } else {
            address = InetAddresses.forString(str);
            zone = "";
        }

        checkArgument(address instanceof Inet6Address, "Value \"%s\" is not a valid ipv6-address", str);
        return Ipv6AddressString.valueOf((Inet6Address) address, zone);
    }
}
