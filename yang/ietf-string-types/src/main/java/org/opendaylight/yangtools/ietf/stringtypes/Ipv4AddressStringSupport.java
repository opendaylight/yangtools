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
import java.net.Inet4Address;
import java.net.InetAddress;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringSupport;

@Beta
@NonNullByDefault
@ThreadSafe
public final class Ipv4AddressStringSupport extends AbstractDerivedStringSupport<Ipv4AddressString> {

    private static final Ipv4AddressStringSupport INSTANCE = new Ipv4AddressStringSupport();

    private Ipv4AddressStringSupport() {
        super(Ipv4AddressString.class);
    }

    public static Ipv4AddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Ipv4AddressString fromString(final String str) {
        final InetAddress address = InetAddresses.forString(str);
        checkArgument(address instanceof Inet4Address, "Value \"%s\" is not a valid ipv4-address", str);
        return Ipv4AddressString.valueOf((Inet4Address) address);
    }
}
