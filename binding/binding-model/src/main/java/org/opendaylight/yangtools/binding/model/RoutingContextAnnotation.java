/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;

/**
 * An attached {@link RoutingContext} annotation.
 *
 * @param value the {@link TypeName} of {@link RoutingContext#value()}
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public record RoutingContextAnnotation(IdentityArchetype value) implements GetterAnnotation {
    private static final TypeName TYPE = TypeName.ofClass(RoutingContext.class);

    public RoutingContextAnnotation {
        requireNonNull(value);
    }

    @Override
    public TypeName type() {
        return TYPE;
    }
}
