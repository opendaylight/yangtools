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
public class EagerDerivedString extends CachingDerivedString<EagerDerivedString> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    protected EagerDerivedString(final String str) {
        super(str);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final EagerDerivedString o) {
        return toCanonicalString().compareTo(o.toCanonicalString());
    }

    @Override
    public final CanonicalValueSupport<EagerDerivedString> support() {
        return DerivedStringTest.EAGER_SUPPORT;
    }

    @Override
    public final int hashCode() {
        return toCanonicalString().hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof String) {
            return toCanonicalString().equals(obj);
        }

        return obj instanceof DerivedString<?> ds && toCanonicalString().equals(ds.toCanonicalString());
    }

    @Override
    protected final String computeCanonicalString() {
        throw new UnsupportedOperationException();
    }
}
