/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.lang.annotation.ElementType;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.RoutingContextAnnotation;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * An instance of {@link #type()} annotation. This may be applicable to
 * <ul>
 *   <li>a Java method, represented by {@link ToMethod}</li>
 * </ul>
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface AttachedAnnotation permits AttachedAnnotation.ToMethod {
    /**
     * An annotation attached to a method, e.g. when the {@link #type()} allows use with {@link ElementType#METHOD}.
     */
    sealed interface ToMethod extends AttachedAnnotation permits OverrideAnnotation, RoutingContextAnnotation {
        @Override
        default List<ElementType> targets() {
            return List.of(ElementType.METHOD);
        }
    }

    /**
     * {@return the {@link TypeName} of this annotation}
     */
    TypeName type();

    /**
     * {@return the {@link ElementType}s that are valid targets of this annotation}
     */
    List<ElementType> targets();

    /**
     * {@return {@code true} if this annotation is repeatable}, {@code false} if it is not
     */
    default boolean repeatable() {
        return false;
    }
}
