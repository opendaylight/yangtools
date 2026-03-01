/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractTypeBuilder;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * Common interface for all builders resulting in a {@link Type}.
 */
@NonNullByDefault
public sealed interface TypeBuilder extends Mutable
        permits AbstractTypeBuilder, AnnotationTypeBuilder, EnumTypeObjectArchetype.Builder, GeneratedTypeBuilderBase {
    /**
     * {@return the name of the type this builder produces}
     */
    JavaTypeName typeName();

    /**
     * {@return a {@link TypeRef} to the type this builder produces}
     */
    default TypeRef typeRef() {
        return TypeRef.of(typeName());
    }
}
