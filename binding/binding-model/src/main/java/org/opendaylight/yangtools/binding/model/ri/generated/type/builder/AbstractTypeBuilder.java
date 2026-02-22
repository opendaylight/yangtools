/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;

/**
 * Abstract base class for {@link TypeBuilder}s.
 */
@Beta
@NonNullByDefault
public abstract sealed class AbstractTypeBuilder implements TypeBuilder
        permits EnumTypeObjectArchetypeBuilder, AbstractGeneratedTypeBuilder, AnnotationTypeBuilderImpl {
    private final JavaTypeName typeName;

    AbstractTypeBuilder(final JavaTypeName typeName) {
        this.typeName = requireNonNull(typeName);
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final int hashCode() {
        return typeName.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof AbstractTypeBuilder other && typeName.equals(other.typeName());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("typeName", typeName);
    }

    static final void addToStringAttribute(final ToStringHelper helper, final String name,
            final @Nullable Collection<?> value) {
        if (value != null && !value.isEmpty()) {
            helper.add(name, value);
        }
    }
}
