/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;

/**
 * A synchronous transformation function from one {@link SourceRepresentation} to another.
 *
 * @param <I> input {@link SourceRepresentation}
 * @param <O> output {@link SourceRepresentation}
 * @see YangTextToIRSourceTransformer
 */
@Beta
@NonNullByDefault
public interface SourceTransformer<I extends SourceRepresentation, O extends SourceRepresentation> {
    /**
     * {@return the input representation class}
     */
    Class<I> inputRepresentation();

    /**
     * {@return the output representation class}
     */
    Class<O> outputRepresentation();

    /**
     * {@return an instance output SourceRepresentation}
     * @param input the input {@link SourceRepresentation}
     * @throws ExtractorException if the input representation violates linkages
     * @throws SourceSyntaxException if the input representation is not syntactically valid
     */
    O transformSource(I input) throws ExtractorException, SourceSyntaxException;
}
