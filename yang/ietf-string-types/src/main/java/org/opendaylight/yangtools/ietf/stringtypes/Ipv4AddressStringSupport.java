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
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.concepts.Variant;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

@Beta
@MetaInfServices(value = CanonicalValueSupport.class)
@NonNullByDefault
@ThreadSafe
public final class Ipv4AddressStringSupport extends AbstractCanonicalValueSupport<Ipv4AddressString> {
    @RegEx
    private static final String LETTER_NUMBER_REGEX = "[\\p{N}\\p{L}]+";
    private static final Pattern LETTER_NUMBER_PATTERN = Pattern.compile(LETTER_NUMBER_REGEX);
    private static final Ipv4AddressStringSupport INSTANCE = new Ipv4AddressStringSupport();

    public Ipv4AddressStringSupport() {
        super(Ipv4AddressString.class);
    }

    public static Ipv4AddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Variant<Ipv4AddressString, CanonicalValueViolation> fromString(final String str) {
        final InetAddress address;
        final String zone;
        final int percent = str.indexOf('%');
        if (percent != -1) {
            zone = str.substring(percent + 1);
            checkArgument(LETTER_NUMBER_PATTERN.matcher(zone).matches(), "Value \"%s\" has invalid zone", str);
            address = InetAddresses.forString(str.substring(0, percent));
        } else {
            address = InetAddresses.forString(str);
            zone = "";
        }

        checkArgument(address instanceof Inet4Address, "Value \"%s\" is not a valid ipv4-address", str);
        return Variant.ofFirst(Ipv4AddressString.valueOf((Inet4Address) address, zone));
    }
}
