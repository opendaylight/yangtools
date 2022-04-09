/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Common base class for forwarding implementations of {@link DeclaredStatement}.
 */
@Beta
public abstract class ForwardingDeclaredStatement<A, D extends DeclaredStatement<A>>
        extends ForwardingObject implements DeclaredStatement<A> {
    @Override
    public StatementDefinition statementDefinition() {
        return delegate().statementDefinition();
    }

    @Override
    public A argument() {
        return delegate().argument();
    }

    @Override
    public String rawArgument() {
        return delegate().rawArgument();
    }

    @Override
    public List<? extends DeclaredStatement<?>> declaredSubstatements() {
        return delegate().declaredSubstatements();
    }

    @Override
    public Optional<DeclarationReference> declarationReference() {
        return delegate().declarationReference();
    }

    @Override
    protected abstract @NonNull D delegate();
}
