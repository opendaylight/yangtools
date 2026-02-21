/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
record DefaultConcreteType(JavaTypeName name) implements ConcreteType {
    DefaultConcreteType {
        requireNonNull(name);
    }

    @Override
    public RestrictedType withRestrictions(final Restrictions restrictions) {
        return new DefaultRestrictedType(name, restrictions);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ConcreteType.class).add("name", name).toString();
    }
}
