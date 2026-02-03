/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ArgumentFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

/**
 * Internal support tuple: the {@link ArgumentFactory} and {@link StatementSupport} that fit together.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
record ReactorSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>(
        @NonNull ArgumentFactory<A> argumentFactory,
        @NonNull StatementSupport<A, D, E> statementSupport) {
    ReactorSupport {
        requireNonNull(argumentFactory);
        requireNonNull(statementSupport);
    }

    ReactorSupport(final @NonNull StatementSupport<A, D, E> statementSupport) {
        this(statementSupport, statementSupport);
    }
}
