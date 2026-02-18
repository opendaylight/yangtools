/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoRef;

@NonNullByDefault
record ReactorSourceImpl(SourceInfoRef infoRef, StatementStreamSource.Factory streamFactory) implements ReactorSource {
    ReactorSourceImpl {
        requireNonNull(infoRef);
        requireNonNull(streamFactory);
    }

    @Override
    public SourceIdentifier sourceId() {
        return infoRef.ref().correctId();
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
        return MoreObjects.toStringHelper(ReactorSource.class).add("ref", infoRef).toString();
    }
}
