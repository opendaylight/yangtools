/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement;

/**
 * An 'implementation' of an effective UnrecognizedStatement. This class is actually never instantiated and exists
 * only as an implementation-private marker for {@link StatementDefinition#getEffectiveRepresentationClass()}.
 */
final class UnrecognizedEffectiveStatement extends AbstractDeclaredEffectiveStatement<Object, UnrecognizedStatement> {
    private UnrecognizedEffectiveStatement() {
        // This should never be called
    }

    @Override
    public StatementDefinition statementDefinition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String argument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnrecognizedStatement getDeclared() {
        throw new UnsupportedOperationException();
    }
}
