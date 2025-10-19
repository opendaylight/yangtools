/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class LazyDerivedString extends CachingDerivedString<LazyDerivedString> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final String str;

    protected LazyDerivedString(final String str) {
        this.str = str;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final LazyDerivedString o) {
        return str.compareTo(o.str);
    }

    @Override
    public final CanonicalValueSupport<LazyDerivedString> support() {
        return DerivedStringTest.LAZY_SUPPORT;
    }

    @Override
    public final int hashCode() {
        return str.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof String) {
            return str.equals(obj);
        }

        return obj instanceof DerivedString<?> ds && str.equals(ds.toCanonicalString());
    }

    @Override
    protected final String computeCanonicalString() {
        return str;
    }
}
