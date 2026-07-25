/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An attached {@link Override} annotation.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public final class OverrideAnnotation implements AttachedAnnotation.ToMethod {
    private static final JavaTypeName TYPE = JavaTypeName.create(Override.class);

    /**
     * The singleton {@link OverrideAnnotation} instance.
     */
    public static final OverrideAnnotation INSTANCE = new OverrideAnnotation();

    private OverrideAnnotation() {
        // hidden on purpose
    }

    @Override
    public JavaTypeName type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return OverrideAnnotation.class.getSimpleName();
    }
}
