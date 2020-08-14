/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;

abstract class IRStatementL extends IRStatement {
    private final @NonNull ImmutableList<IRStatement> statements;

    IRStatementL(final IRKeyword keyword, final ImmutableList<IRStatement> statements) {
        super(keyword);
        this.statements = requireNonNull(statements);
    }

    @Override
    public final ImmutableList<IRStatement> statements() {
        return statements;
    }
}
