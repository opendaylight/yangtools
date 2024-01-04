/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A {@link StatementDeclaration} backed by {@link IRStatement}.
 */
public final class YangIRStatementInText extends StatementDeclaration.InText {
    private final @NonNull SourceIdentifier source;
    private final IRStatement statement;

    public YangIRStatementInText(final IRStatement statement, final SourceIdentifier source) {
        this.statement = requireNonNull(statement);
        this.source = requireNonNull(source);
    }

    @Override
    public int startLine() {
        return statement.startLine();
    }

    @Override
    public int startColumn() {
        return statement.startColumn();
    }

    @Override
    protected @NonNull String file() {
        return source.toYangFilename();
    }
}
