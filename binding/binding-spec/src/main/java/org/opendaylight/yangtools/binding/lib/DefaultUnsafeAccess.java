/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.binding.meta.UnsafeScalarTypeObjectFactory;

/**
 * Default implementation of {@link UnsafeAccess}. Hidden on purpose: we give these out paired with
 * {@link DefaultSTORegistrar}.
 */
// TODO: value record when we have JEP-401 available
@NonNullByDefault
record DefaultUnsafeAccess(UnsafeAccessState state) implements UnsafeAccess {
    DefaultUnsafeAccess {
        requireNonNull(state);
    }

    @Override
    public <V, T extends ScalarTypeObject<V>>
            @Nullable UnsafeScalarTypeObjectFactory<V, T> lookupUnsafeScalarTypeObjectFactory(final T typeObj) {
        @SuppressWarnings("unchecked")
        final var ret = (UnsafeScalarTypeObjectFactory<V, T>) state.lookupSTO(typeObj.getClass());
        return ret;
    }

    @Override
    public String toString() {
        return state.computeToString(UnsafeAccess.class);
    }
}
