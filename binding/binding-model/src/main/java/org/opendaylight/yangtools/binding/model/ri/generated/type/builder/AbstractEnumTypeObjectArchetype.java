/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;

@NonNullByDefault
abstract sealed class AbstractEnumTypeObjectArchetype implements EnumTypeObjectArchetype
        permits CodegenEnumTypeObjectArchetype, RuntimeEnumTypeObjectArchetype {
    private final List<AnnotationType> annotations;
    private final List<Pair> values;
    private final JavaTypeName name;

    AbstractEnumTypeObjectArchetype(final JavaTypeName name, final List<Pair> values,
            final List<AnnotationType> annotations) {
        this.name = requireNonNull(name);
        this.values = requireNonNull(values);
        this.annotations = requireNonNull(annotations);
    }

    @Override
    public final JavaTypeName name() {
        return name;
    }

    @Override
    public final List<Pair> getValues() {
        return values;
    }

    @Override
    public final List<AnnotationType> getAnnotations() {
        return annotations;
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name.equals(other.name());
    }

    @Override
    public final String toString() {
        final var helper = MoreObjects.toStringHelper(EnumTypeObjectArchetype.class).add("name", name);
        if (!values.isEmpty()) {
            helper.add("values", values);
        }
        return helper.toString();
    }
}