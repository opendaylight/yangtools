/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.status;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement;

abstract class AbstractStatusStatement extends AbstractDeclaredStatement<Status> implements StatusStatement {
    private final @NonNull Status argument;

    AbstractStatusStatement(final Status argument) {
        this.argument = requireNonNull(argument);
    }

    @Override
    public final @NonNull Status argument() {
        return argument;
    }

    @Override
    public final @NonNull String rawArgument() {
        return argument.getArgumentString();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return YangStmtMapping.STATUS;
    }
}
