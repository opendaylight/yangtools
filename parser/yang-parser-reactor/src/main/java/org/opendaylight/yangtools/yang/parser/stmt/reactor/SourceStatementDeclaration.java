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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A {@link StatementDeclaration} when we only have a {@link SourceIdentifier}.
 */
@NonNullByDefault
final class SourceStatementDeclaration extends StatementSourceReference implements DeclarationReference {
    private final SourceIdentifier sourceId;

    SourceStatementDeclaration(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    @Override
    public StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public @NonNull DeclarationReference declarationReference() {
        return this;
    }

    @Override
    public String toHumanReadable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return toHumanReadable();
    }
}
