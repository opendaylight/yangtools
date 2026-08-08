/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An attached {@link Override} annotation.
 *
 * @since 16.0.0
 * @deprecated This annotation is used only to convey information from {@code binding-generator} to
 *             {@code binding-codegen} and is considered a private implementation detail. Most notably
 *             {@code binding-codegen} can start treating this annotation as non-existent at any time.
 */
@Deprecated(since = "16.0.0")
@NonNullByDefault
public final class OverrideAnnotation implements GetterAnnotation {
    private static final TypeName TYPE = TypeName.ofClass(Override.class);

    /**
     * The singleton {@link OverrideAnnotation} instance.
     */
    @Deprecated(since = "16.0.0")
    public static final OverrideAnnotation INSTANCE = new OverrideAnnotation();

    private OverrideAnnotation() {
        // hidden on purpose
    }

    @Deprecated(since = "16.0.0")
    @Override
    public TypeName type() {
        return TYPE;
    }

    @Deprecated(since = "16.0.0")
    @Override
    public String toString() {
        return OverrideAnnotation.class.getSimpleName();
    }
}
