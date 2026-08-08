/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

@Deprecated(since = "16.0.0", forRemoval = true)
@NonNullByDefault
public record TypeRefImpl(TypeName name) implements TypeRef {
    @Deprecated(since = "16.0.0", forRemoval = true)
    public TypeRefImpl {
        requireNonNull(name);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(TypeRef.class).add("name", name).toString();
    }
}
