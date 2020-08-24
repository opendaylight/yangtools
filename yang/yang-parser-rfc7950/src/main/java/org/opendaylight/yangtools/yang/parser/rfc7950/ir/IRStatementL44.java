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

final class IRStatementL44 extends IRStatement044 {
    private final @NonNull ImmutableList<IRStatement> statements;

    IRStatementL44(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn,
            final ImmutableList<IRStatement> statements) {
        super(keyword, argument, startLine, startColumn);
        this.statements = requireNonNull(statements);
    }

    @Override
    public ImmutableList<IRStatement> statements() {
        return statements;
    }

    @Override
    byte ioType() {
        final int size = statements.size();
        if (size <= 255) {
            return IOSupport.STMT_N44_U8;
        }
        return size <= 65535 ? IOSupport.STMT_N44_U16 : IOSupport.STMT_N44_S32;
    }
}
