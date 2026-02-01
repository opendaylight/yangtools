/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;

/**
 * A {@link SourceRepresentation} that is fully materialized and is its own {@link SourceInfo.Extractor}. It also allows
 * its {@link SourceIdentifier} to be adjusted.
 *
 * @param <R> the {@link SourceRepresentation} type
 * @param <S> root statement type
 * @since 15.0.0
 */
@NonNullByDefault
public interface MaterializedSourceRepresentation<R extends SourceRepresentation, S> extends SourceInfo.Extractor {
    /*
     * @{return {@link SourceIdentifier} of this source}
     */
    SourceIdentifier sourceId();

    /**
     * {@return the root statement}
     */
    S statement();

    /**
     * {@return this representation with the specified SourceIdentifier}
     * @param newSourceId the new {@link SourceIdentifier}
     */
    R withSourceId(SourceIdentifier newSourceId);

    /**
     * {@return this source with {@link SourceIdentifier} adjusted to the one extracted from this source}
     */
    default R withExtractedSourceId() throws SourceSyntaxException {
        return withSourceId(extractSourceInfo().sourceId());
    }
}
