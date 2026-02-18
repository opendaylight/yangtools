/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

@NonNullByDefault
record ModuleRef(SourceIdentifier correctId) implements SourceRef.ToModule {
    ModuleRef {
        requireNonNull(correctId);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return toStringImpl(SourceRef.ToModule.class, this);
    }

    // visible for SubmoduleRef
    static final <T extends SourceRef> String toStringImpl(final Class<T> clazz, final T self) {
        return MoreObjects.toStringHelper(clazz)
            .add("hash", Integer.toHexString(self.hashCode()))
            .add("correctId", self.correctId())
            .toString();
    }
}
