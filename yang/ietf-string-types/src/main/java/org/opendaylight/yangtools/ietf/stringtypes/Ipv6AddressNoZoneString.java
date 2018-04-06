/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import com.google.common.annotations.Beta;
import java.util.Optional;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
@ThreadSafe
public class Ipv6AddressNoZoneString extends Ipv6AddressString {
    private static final long serialVersionUID = 1L;

    protected Ipv6AddressNoZoneString(final int intBits0, final int intBits1, final int intBits2,
            final int intBits3) {
        super(intBits0, intBits1, intBits2, intBits3);
    }

    protected Ipv6AddressNoZoneString(final AbstractIpv6Address<?> other) {
        super(other);
    }

    @Override
    public final Optional<String> getZone() {
        return Optional.empty();
    }
}