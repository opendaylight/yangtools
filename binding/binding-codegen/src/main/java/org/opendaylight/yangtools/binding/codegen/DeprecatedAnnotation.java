/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.DEPRECATED;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link BlockFragment} emmitting a {@code @Deprecated} annotation.
 */
record DeprecatedAnnotation(@NonNull String deprecated, boolean forRemoval) implements BlockFragment {
    DeprecatedAnnotation {
        requireNonNull(deprecated);
    }

    @NonNullByDefault
    DeprecatedAnnotation(final GeneratedClass javaType, final boolean forRemoval) {
        this(javaType.getReferenceString(DEPRECATED), forRemoval);
    }

    static @Nullable DeprecatedAnnotation of(final @NonNull GeneratedClass javaType,
            final @NonNull EffectiveStatement<?, ?> statement) {
        return statement instanceof WithStatus withStatus ? of(javaType, withStatus) : null;
    }

    static @Nullable DeprecatedAnnotation of(final @NonNull GeneratedClass javaType,
            final @NonNull WithStatus withStatus) {
        return switch (withStatus.getStatus()) {
            case CURRENT -> null;
            case DEPRECATED -> new DeprecatedAnnotation(javaType, false);
            case OBSOLETE ->  new DeprecatedAnnotation(javaType, true);
        };
    }

    @Override
    public void appendTo(final BlockBuilder bb) {
        bb.at().str(deprecated);
        if (forRemoval) {
            bb.str("(forRemoval = true)");
        }
        bb.newLine();
    }
}
