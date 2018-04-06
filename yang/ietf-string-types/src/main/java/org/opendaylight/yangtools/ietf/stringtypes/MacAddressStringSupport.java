/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.hexValue;

import com.google.common.annotations.Beta;
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
public final class MacAddressStringSupport extends AbstractCanonicalValueSupport<MacAddressString> {
    private static final MacAddressStringSupport INSTANCE = new MacAddressStringSupport();

    public MacAddressStringSupport() {
        super(MacAddressString.class);
    }

    public static MacAddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Variant<MacAddressString, CanonicalValueViolation> fromCanonicalString(final String str) {
        checkArgument(str.length() == 17, "Malformed string \"%s\"", str);
        return Variant.ofFirst(new MacAddressString(hexValue(str.charAt(0)) << 28 | hexValue(str.charAt(1)) << 24
            | hexValue(str.charAt(3)) << 20 | hexValue(str.charAt(4)) << 16 | hexValue(str.charAt(6)) << 12
            | hexValue(str.charAt(7)) << 8 | hexValue(str.charAt(9)) << 4 | hexValue(str.charAt(10)),
            (short) (hexValue(str.charAt(12)) << 12 | hexValue(str.charAt(13)) << 8 | hexValue(str.charAt(15)) << 4
                    | hexValue(str.charAt(16)))));
    }

    @Override
    public Variant<MacAddressString, CanonicalValueViolation> fromString(final String str) {
        checkArgument(str.length() == 17, "Malformed string \"%s\"", str);
        for (int i = 2; i < 17; i += 3) {
            checkColon(str, i);
        }
        return fromCanonicalString(str);
    }

    private static void checkColon(final String str, final int offset) {
        final char ch = str.charAt(offset);
        checkArgument(ch == ':', "Invalid character '%s' at offset %s", ch, offset);
    }
}
