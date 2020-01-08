/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.description;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;

abstract class AbstractDescriptionEffectiveStatement
        extends AbstractDeclaredEffectiveStatement<String, DescriptionStatement>
        implements DescriptionEffectiveStatement {
    private final @NonNull DescriptionStatement declared;

    AbstractDescriptionEffectiveStatement(final DescriptionStatement declared) {
        this.declared = requireNonNull(declared);
    }

    @Override
    public final @NonNull DescriptionStatement getDeclared() {
        return declared;
    }
}
