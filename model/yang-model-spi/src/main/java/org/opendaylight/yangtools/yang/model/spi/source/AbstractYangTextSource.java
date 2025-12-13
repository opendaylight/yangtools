/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;

/**
 * Abstract base class for implementing {@link YangTextSource}s with {@link Delegator}.
 */
@NonNullByDefault
abstract class AbstractYangTextSource<T> extends YangTextSource implements Delegator<T>, SourceInfo.Extractor {
    private final SourceIdentifier sourceId;
    private final T delegate;

    AbstractYangTextSource(final SourceIdentifier sourceId, final T delegate) {
        this.sourceId = requireNonNull(sourceId);
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public final T getDelegate() {
        return delegate;
    }

    @Override
    public final SourceInfo extractSourceInfo() throws ExtractorException {
        try (var is = openBufferedStream()) {
            return XMLReaderSourceInfoExtractor.extractSourceInfo(sourceId, is);
        } catch (IOException e) {
            throw new ExtractorException(null, "Failed to read text", e);
        }
    }
}
