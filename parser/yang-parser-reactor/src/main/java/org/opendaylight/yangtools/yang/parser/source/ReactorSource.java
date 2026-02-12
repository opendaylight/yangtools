/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.parser.source.BuildSource.Stage;

/**
 * A {@link Stage} when we have acquired {@link SourceInfo} from the source representation.
 */
@NonNullByDefault
public record ReactorSource(
        SourceInfo sourceInfo,
        StatementStreamSource.Factory streamFactory) implements BuildSource.Stage {
    public ReactorSource {
        requireNonNull(sourceInfo);
        requireNonNull(streamFactory);
    }

    @Override
    public SourceIdentifier sourceId() {
        // Note: unlike source.sourceId(), this is guaranteed to be canonical
        return sourceInfo.sourceId();
    }

    // Note: equality overridden to identity for predictable use as a Map key
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
    }
}
