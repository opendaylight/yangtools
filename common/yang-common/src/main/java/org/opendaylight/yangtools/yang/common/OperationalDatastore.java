/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link DatastoreIdentity.Operational}.
 */
@NonNullByDefault
record OperationalDatastore(QName value) implements DatastoreIdentity.Operational {
    OperationalDatastore {
        requireNonNull(value);
    }

    @Override
    public int hashCode() {
        return DatastoreIdentityMethods.hashCodeImpl(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return DatastoreIdentityMethods.equalsImpl(this, obj);
    }

    @Override
    public String toString() {
        return DatastoreIdentityMethods.toStringImpl(this);
    }

    @java.io.Serial
    private Object writeReplace() {
        return new DIv1(this);
    }
}
