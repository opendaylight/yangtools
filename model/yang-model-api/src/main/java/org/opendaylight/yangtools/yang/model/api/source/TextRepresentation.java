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
 */
@NonNullByDefault
public sealed interface TextRepresentation<S extends SourceRepresentation> extends SourceRepresentation
    permits YangTextSource, YinTextSource {
    // Nothing else
}
