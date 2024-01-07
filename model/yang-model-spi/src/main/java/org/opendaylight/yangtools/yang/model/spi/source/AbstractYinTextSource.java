/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YinTextSource;

/**
 * Abstract base class for implementing {@link YinTextSource}s.
 */
@NonNullByDefault
public abstract class AbstractYinTextSource extends YinTextSource {
    private final SourceIdentifier sourceId;

    protected AbstractYinTextSource(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
    }
}
