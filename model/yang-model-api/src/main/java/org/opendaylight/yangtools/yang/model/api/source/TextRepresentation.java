/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.source;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Type safety capture of the {@link SourceRepresentation} text format specializations.
 *
 * @param <S> the {@link SourceRepresentation} for which this
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface TextRepresentation<S extends SourceRepresentation> permits YangTextSource, YinTextSource {
    /**
     * {@return the {@link TextRepresentation} specialization of {@link S}}
     * @see YangTextSource#textRepresentation()
     * @see YinTextSource#textRepresentation()
     */
    Class<? extends S> textRepresentation();
}
