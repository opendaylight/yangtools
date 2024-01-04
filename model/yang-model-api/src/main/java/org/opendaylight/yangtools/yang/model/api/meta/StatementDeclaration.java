/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

/**
 * A {@link StatementSourceReference} which acts as its own {@link DeclarationReference}.
 */
public abstract class StatementDeclaration extends StatementSourceReference implements DeclarationReference {
    @Override
    public final StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public final DeclarationReference declarationReference() {
        return this;
    }
}
