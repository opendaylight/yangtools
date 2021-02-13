/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Abstract base class for {@link DeclaredStatement} implementations which decorate a statement with
 * {@link StatementSourceReference}.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public abstract class AbstractRefStatement<A, D extends DeclaredStatement<A>>
        extends ForwardingDeclaredStatement<A, D> {
    private final StatementSourceReference ref;
    private final @NonNull D delegate;

    AbstractRefStatement(final D delegate, final StatementSourceReference ref) {
        this.delegate = requireNonNull(delegate);
        this.ref = requireNonNull(ref);
        verify(delegate.statementOrigin().equals(ref.statementOrigin()));
    }

    @Override
    public final StatementSourceReference getStatementSourceReference() {
        return ref;
    }

    @Override
    protected final D delegate() {
        return delegate;
    }
}
