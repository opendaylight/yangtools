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
import org.opendaylight.yangtools.concepts.Variant;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueValidator;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

@Beta
@NonNullByDefault
@ThreadSafe
public final class Ipv6AddressNoZoneStringValidator
        extends AbstractCanonicalValueValidator<Ipv6AddressString, Ipv6AddressNoZoneString> {

    private static final Ipv6AddressNoZoneStringValidator INSTANCE = new Ipv6AddressNoZoneStringValidator();

    private Ipv6AddressNoZoneStringValidator() {
        super(Ipv6AddressStringSupport.getInstance(), Ipv6AddressNoZoneString.class);
    }

    public static Ipv6AddressNoZoneStringValidator getInstance() {
        return INSTANCE;
    }

    @Override
    protected Variant<Ipv6AddressString, CanonicalValueViolation> validate(final Ipv6AddressString value) {
        return Variant.ofFirst(new Ipv6AddressNoZoneString(value));
    }

    @Override
    protected Variant<Ipv6AddressString, CanonicalValueViolation> validate(final Ipv6AddressString value,
            final String canonicalString) {
        return validate(value);
    }
}
