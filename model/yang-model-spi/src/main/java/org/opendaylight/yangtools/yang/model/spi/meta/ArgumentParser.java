/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.ModelStatement;

/**
 * An entity capable of parsing {@link DeclaredStatement#rawArgument()} into its corresponding
 * {@link ModelStatement#argument()}.
 *
 * @param <T> argument type
 */
@NonNullByDefault
@FunctionalInterface
public interface ArgumentParser<@NonNull T> {
    /**
     * An {@link ArgumentParser} whose {@link #parseArgument(String)} reports only {@link ArgumentSyntaxException}.
     *
     * @param <T> argument type
     */
    @FunctionalInterface
    interface SyntaxOnly<@NonNull T> extends ArgumentParser<T> {
        @Override
        T parseArgument(String rawArgument) throws ArgumentSyntaxException;
    }

    /**
     * Parse a string corresponding to its corresponding type.
     *
     * @param rawArgument the
     * @return an argument instance
     * @throws ArgumentSyntaxException when the string is not syntactically valid
     * @throws ArgumentBindingException when the the argument expressed by the string cannot be semantically bound
     */
    T parseArgument(String rawArgument) throws ArgumentSyntaxException, ArgumentBindingException;
}
