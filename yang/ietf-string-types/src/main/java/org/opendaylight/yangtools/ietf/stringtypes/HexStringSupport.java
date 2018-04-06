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
import org.opendaylight.yangtools.concepts.Variant;
import org.opendaylight.yangtools.yang.common.AbstractCanonicalValueSupport;
import org.opendaylight.yangtools.yang.common.CanonicalValueViolation;

@Beta
@NonNullByDefault
@ThreadSafe
public final class HexStringSupport extends AbstractCanonicalValueSupport<HexString> {
    private static final HexStringSupport INSTANCE = new HexStringSupport();

    private HexStringSupport() {
        super(HexString.class);
    }

    public static HexStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Variant<HexString, CanonicalValueViolation> fromCanonicalString(final String str) {
        if (str.isEmpty()) {
            return Variant.ofFirst(HexString.empty());
        }

        final int strlen = str.length();
        final byte[] bytes = new byte[(strlen + 1) / 3];
        for (int i = 0; i < strlen; i += 3) {
            bytes[i] = (byte) (hexValue(str.charAt(i)) << 4 | hexValue(str.charAt(i + 1)));
        }
        return Variant.ofFirst(HexString.valueOf(bytes));
    }

    @Override
    public Variant<HexString, CanonicalValueViolation> fromString(final String str) {
        final int strlen = str.length();
        for (int i = 2; i < strlen; i += 3) {
            checkColon(str, i);
        }
        return fromCanonicalString(str);
    }

    private static void checkColon(final String str, final int offset) {
        final char ch = str.charAt(offset);
        checkArgument(ch == ':', "Invalid character '%s' at offset %s", ch, offset);
    }
}
