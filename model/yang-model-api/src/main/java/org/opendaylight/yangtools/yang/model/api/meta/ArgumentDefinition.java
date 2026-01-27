/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Definition of an argument to a YANG statement.
 */
public sealed interface ArgumentDefinition<A> extends Immutable permits DefaultArgumentDefinition {

    static <A> @NonNull ArgumentDefinition<A> of(final @NonNull Class<? extends A> declaredRepresentation,
            final @NonNull QName argumentName, final boolean yinElement) {
        return new DefaultArgumentDefinition<>(declaredRepresentation, argumentName, yinElement);
    }

    static <A> @NonNull ArgumentDefinition<A> of(final @NonNull Class<? extends A> declaredRepresentation,
            final @NonNull QNameModule module, final @NonNull String argumentName) {
        return of(declaredRepresentation, module, argumentName, false);
    }

    static <A> @NonNull ArgumentDefinition<A> of(final @NonNull Class<? extends A> declaredRepresentation,
            final @NonNull QNameModule module, final @NonNull String argumentName, final boolean yinElement) {
        return of(declaredRepresentation, QName.create(module, argumentName).intern(), yinElement);
    }

    /**
     * {@return name of the argument}
     */
    @NonNull QName argumentName();

    /**
     * {@return {@code true} if the argument should be encoded as a YIN element, {@code false} if it should be encoded
     * as a YIN attribute}
     */
    boolean yinElement();

    /**
     * {@return the class representing the declared version of the statement associated with this definition}
     */
    @NonNull Class<? extends A> declaredRepresentation();

    /**
     * {@return a human-friendly string representation of {link #argumentName()}}
     * @since 15.0.0
     */
    default @NonNull String humanName() {
        final var argumentName = argumentName();
        return YangConstants.RFC6020_YIN_MODULE.equals(argumentName.getModule()) ? argumentName.getLocalName()
            : argumentName.toString();
    }

    /**
     * {@return a plain argument name}
     * @since 15.0.0
     */
    default @NonNull String simpleName() {
        return argumentName().getLocalName();
    }
}
