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
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
record RestrictedTypeImpl(JavaTypeName name, Restrictions restrictions) implements RestrictedType {
    RestrictedTypeImpl {
        requireNonNull(name);
        // FIXME: require non-empty?
        requireNonNull(restrictions);
    }

    @Override
    public Restrictions restrictions() {
        return restrictions;
    }

    @Override
    public RestrictedType withRestrictions(final Restrictions newRestrictions) {
        return new RestrictedTypeImpl(name(), newRestrictions);
    }

    @Override
    public ConcreteType withoutRestrictions() {
        return new ConcreteTypeImpl(name());
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(RestrictedType.class)
            .add("name", name)
            .add("restrictions", restrictions)
            .toString();
    }

}
