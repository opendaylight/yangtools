/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Common base class for forwarding implementations of {@link DeclaredStatement}.
 */
@Beta
public abstract class ForwardingDeclaredStatement<A, D extends DeclaredStatement<A>>
        extends ForwardingModelStatement<A, D> implements DeclaredStatement<A> {
    @Override
    public String rawArgument() {
        return delegate().rawArgument();
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return delegate().declaredSubstatements();
    }

    @Override
    public Optional<DeclarationReference> declarationReference() {
        return delegate().declarationReference();
    }

    @Override
    protected abstract @NonNull D delegate();
}
