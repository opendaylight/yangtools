/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.Status;

/**
 * Projection of {@link Status} into generated code as a {@link Deprecated} annotation.
 *
 * @since 16.0.0
 */
@Beta
public enum DeprecatedAnnotation implements AttachedAnnotation.ToMethod {
    /**
     * {@code @Deprecated}, corresponding to {@link Status#DEPRECATED}.
     */
    DEPRECATED(false),
    /**
     * {@code @Deprecated(forRemoval = true)}, corresponding to {@link Status#OBSOLETE}.
     */
    OBSOLETE(true);

    private static final @NonNull JavaTypeName TYPE = JavaTypeName.create(Deprecated.class);

    private final boolean forRemoval;

    DeprecatedAnnotation(final boolean forRemoval) {
        this.forRemoval = forRemoval;
    }

    @Override
    public JavaTypeName type() {
        return TYPE;
    }

    /**
     * {@return the value of Deprecated#forRemoval()}
     */
    public boolean forRemoval() {
        return forRemoval;
    }

    /**
     * {@return the {@link DeprecatedAnnotation} corresponding to a {@link Status}, or {@code null}}
     * @param status the status, potentially {@code null}
     */
    public static @Nullable DeprecatedAnnotation ofStatus(final @Nullable Status status) {
        return switch (status) {
            case null -> null;
            case CURRENT -> null;
            case DEPRECATED -> DEPRECATED;
            case OBSOLETE -> OBSOLETE;
        };
    }

    @Override
    public String toString() {
        return forRemoval ? "Deprecated" : "DeprecatedForRemoval";
    }
}
