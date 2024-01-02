/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.model.spi.source.SourceIdentifier;

public class ReactorException extends Exception {
    private static final long serialVersionUID = 1L;

    private final ModelProcessingPhase phase;
    private final SourceIdentifier sourceIdentifier;

    public ReactorException(final ModelProcessingPhase phase, final String message, final SourceIdentifier sourceId,
            final Throwable cause) {
        super(message, cause);
        this.phase = requireNonNull(phase);
        sourceIdentifier = sourceId;
    }

    public ReactorException(final ModelProcessingPhase phase, final String message, final SourceIdentifier sourceId) {
        super(message);
        this.phase = requireNonNull(phase);
        sourceIdentifier = sourceId;
    }

    public final ModelProcessingPhase getPhase() {
        return phase;
    }

    public final SourceIdentifier getSourceIdentifier() {
        return sourceIdentifier;
    }
}
