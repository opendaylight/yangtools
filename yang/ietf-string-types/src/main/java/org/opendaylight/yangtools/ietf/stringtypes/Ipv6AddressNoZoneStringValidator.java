/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringValidator;

@Beta
@NonNullByDefault
@ThreadSafe
public final class Ipv6AddressNoZoneStringValidator
        extends AbstractDerivedStringValidator<Ipv6AddressString, Ipv6AddressNoZoneString> {

    private static final Ipv6AddressNoZoneStringValidator INSTANCE = new Ipv6AddressNoZoneStringValidator();

    private Ipv6AddressNoZoneStringValidator() {
        super(Ipv6AddressStringSupport.getInstance(), Ipv6AddressNoZoneString.class);
    }

    public static Ipv6AddressNoZoneStringValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected Ipv6AddressNoZoneString validate(final Ipv6AddressString value) {
        return new Ipv6AddressNoZoneString(value);
    }

    @Override
    protected Ipv6AddressNoZoneString validate(final Ipv6AddressString value, final String canonicalString) {
        return validate(value);
    }
}
