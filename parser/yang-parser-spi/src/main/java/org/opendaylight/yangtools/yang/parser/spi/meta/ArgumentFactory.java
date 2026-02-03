/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Support for acquiring statement arguments.
 *
 * @param <A> Argument type
 */
public sealed interface ArgumentFactory<A> permits StatementSupport {
    /**
     * Given a raw string representation of an argument, try to use a shared representation. Default implementation
     * does nothing.
     *
     * @param rawArgument Argument string
     * @return A potentially-shard instance
     */
    @NonNullByDefault
    default String internArgument(final String rawArgument) {
        return rawArgument;
    }

    /**
     * Parses textual representation of argument in object representation.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param value String representation of value, as was present in text source.
     * @return Parsed value
     * @throws SourceException when an inconsistency is detected.
     */
    @NonNull A parseArgumentValue(@NonNull StmtContext<?, ?, ?> ctx, String value);

    /**
     * Adapts the argument value to match a new module. Default implementation returns original value stored in context,
     * which is appropriate for most implementations.
     *
     * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
     * @param targetModule Target module, may not be null.
     * @return Adapted argument value.
     */
    default A adaptArgumentValue(final @NonNull StmtContext<A, ?, ?> ctx, final @NonNull QNameModule targetModule) {
        return ctx.argument();
    }
}
