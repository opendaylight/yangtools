/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static org.opendaylight.yangtools.ietf.stringtypes.HexUtils.appendHexByte;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;

@Beta
@NonNullByDefault
@ThreadSafe
public class MacAddressString extends AbstractNetworkUnsigned64String<MacAddressString> {
    private static final long serialVersionUID = 1L;

    protected MacAddressString(final long longBits) {
        super(longBits);
    }

    @Override
    public final MacAddressStringSupport support() {
        return MacAddressStringSupport.getInstance();
    }

    @Override
    public final String toCanonicalString() {
        final StringBuilder sb = new StringBuilder(17);
        return appendHexByte(appendHexByte(appendHexByte(appendHexByte(appendHexByte(
            appendHexByte(sb, third()).append(':'), fourth()).append(':'), fifth()).append(':'), sixth()).append(':'),
            seventh()).append(':'), eigth()).toString();
    }
}
