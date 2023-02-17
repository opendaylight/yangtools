/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A view on a YANG datastore contents. As such, this interface does not imply the complete view of the datastore, but
 * it implies it corresponds to a {@link EffectiveModelContext}. The view may be subject to filtering and, as per
 * {@link NormalizedTree}, may offer a relaxed view of elements constrained by global requirements such as
 * {@code require-instance}, which may not be directly resolvable in this view.
 *
 * <p>
 * As an explicit exclusion, any references though a {@code mount-point} may be stale.
 */
@NonNullByDefault
public non-sealed interface NormalizedDatastore extends NormalizedTree, Identifiable<QName> {
    /**
     * {@inheritDoc}
     *
     * Returned {@link QName} is guaranteed to reference an {@code identity} derived from {@code ieft-datastores}'s
     * {@code identity datastore} in the context this object is produced.
     */
    @Override
    QName getIdentifier();
}
