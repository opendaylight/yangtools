/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Abstract base class for {@link DeclaredStatement} implementations which decorate a statement with a
 * {@link DeclarationReference}.
 *
 * @param <A> Argument type ({@link Empty} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 */
public abstract class AbstractRefStatement<A, D extends DeclaredStatement<A>>
        extends ForwardingDeclaredStatement<A, D> {
    private final @NonNull DeclarationReference ref;
    private final @NonNull D delegate;

    protected AbstractRefStatement(final D delegate, final DeclarationReference ref) {
        this.delegate = requireNonNull(delegate);
        this.ref = requireNonNull(ref);
    }

    @Override
    public final Optional<DeclarationReference> declarationReference() {
        return Optional.of(ref);
    }

    @Override
    protected final D delegate() {
        return delegate;
    }
}
