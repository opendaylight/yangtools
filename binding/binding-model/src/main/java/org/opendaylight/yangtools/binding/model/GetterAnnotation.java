/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An annotation attached to a {@link GetterMethod}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
@SuppressWarnings("deprecation")
public sealed interface GetterAnnotation permits OverrideAnnotation, RoutingContextAnnotation {
    /**
     * {@return the {@link TypeName} of this annotation}
     */
    TypeName type();

    /**
     * {@return {@code true} if this annotation is repeatable}, {@code false} if it is not
     */
    default boolean repeatable() {
        return false;
    }
}
