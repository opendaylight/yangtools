/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceRef;
import org.opendaylight.yangtools.yang.parser.source.BuildSource.Stage;

/**
 * A {@link Stage} when we have acquired {@link SourceInfo} from the source representation.
 */
@NonNullByDefault
public sealed interface ReactorSource extends BuildSource.Stage permits ReactorSourceImpl {
    /**
     * {@inheritDoc}
     *
     * <p>Unlike source.sourceId(), this is guaranteed to be canonical
     */
    @Override
    default SourceIdentifier sourceId() {
        return ref().correctId();
    }

    /**
     * {@return the {@link SourceRef} identity of this source}
     */
    SourceRef ref();

    /**
     * {@return the {@link SourceInfo} of this source}
     */
    SourceInfo sourceInfo();

    /**
     * {@return the {@link StatementStreamSource.Factory} of this source}
     */
    StatementStreamSource.Factory streamFactory();

    // Note: equality overridden to identity for predictable use as a Map key
    @Override
    int hashCode();

    @Override
    boolean equals(@Nullable Object obj);
}
