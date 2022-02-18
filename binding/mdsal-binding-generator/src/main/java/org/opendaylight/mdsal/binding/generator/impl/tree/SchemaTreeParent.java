/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.tree;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A parent containing a number of {@link SchemaTreeChild} objects.
 *
 * @param <S> Concrete {@link EffectiveStatement} type
 */
// FIXME: S extends SchemaTreeAwareStatement ... once AugmentEffectiveStatement implements that
public interface SchemaTreeParent<S extends EffectiveStatement<?, ?>> {
    /*
     * Immutable view of children of this object along the {@code schema tree} child axis.
     *
     * @return Immutable view of this objects children along the {@code schema tree} child axis.
     */
    @NonNull List<SchemaTreeChild<?, ?>> schemaTreeChildren();
}
